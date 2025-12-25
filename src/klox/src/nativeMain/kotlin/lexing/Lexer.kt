package lexing

typealias LexScope = SequenceScope<EmittableLexingBlob>

suspend fun LexScope.coRun(
  ss: String,
  sourceFname: String,
  yieldT: suspend LexScope.(t: Token) -> Unit,
  yieldE: suspend LexScope.(e: InterpreterError) -> Unit,
): Unit {
  val splitted = ss.split("\n")
  val numLines = splitted.size
  val isNewlineTerminated = (ss.lastOrNull() == '\n')
  val numberedLines: List<Pair<Int, String>> = ss.split("\n")
    .mapIndexed { idx, ln ->
      if (isNewlineTerminated)
        Pair(idx + 1, ln + "\n")
      else
        Pair(idx + 1, ln + if (idx < numLines - 1) "\n" else "")
    }

  val indexedCharacters: List<Pair<Int, Char>> = numberedLines
    .flatMap { (idx, ln) -> ln.toCharArray().toList().map { ch -> Pair(idx, ch) } }

  
  // theres an implicit state diagram
  var lexerState: String = "DEFAULT"
  var lexerStateBuilder: StringBuilder = StringBuilder()

  indexedCharacters.peekAhead3().forEach { (currPair, nxt1Pair, nxt2Pair) ->
    val lineNo: Int = currPair?.first ?: numLines
    val (curr, nxt1, nxt2) = Triple(currPair?.second, nxt1Pair?.second, nxt2Pair?.second)

    // BODY goes here
    when (lexerState) {
      "SKIP_ONE" -> {
        if (curr == null) {
          yieldE(InterpreterError(lineNo, sourceFname, "Unexpected end of file while parsing bigram"))
        } else {
          lexerState = "DEFAULT"
        }
      }
      "DEFAULT" -> {
        when {
          curr == null -> {
            yieldT(Token(TokenType.EOF, "", null, lineNo, sourceFname))
            return@forEach
          }
          curr == '"' -> {
            lexerState = "STRING"
            lexerStateBuilder.clear()
            lexerStateBuilder.append(curr)
            return@forEach
          }
          curr == '/' && nxt1 == '/' -> {
            lexerState = "COMMENT"
            lexerStateBuilder.clear()
            lexerStateBuilder.append(curr)
            return@forEach
          }
          curr.isDigit() -> {
            lexerState = "NUMBER"
            lexerStateBuilder.clear()
            lexerStateBuilder.append(curr)
            return@forEach
          }
          curr == ' ' || curr == '\t' || curr == '\n' || curr == '\r' -> {
            // ignore whitespace
            return@forEach
          }
          (tryDoMunch2(curr, nxt1, Token.LOOKUP_2CH_TO_TOKEN, yieldT, lineNo, sourceFname)) -> {
            lexerState = "SKIP_ONE"
            return@forEach 
          }
          (tryDoMunch1(curr, Token.LOOKUP_1CH_TO_TOKEN, yieldT, lineNo, sourceFname)) ->
            return@forEach 
          else ->
            // dont forget to error if we are confused
            yieldE(InterpreterError(lineNo, sourceFname, "Unexpected character '${curr}'"))
        }
      }
      "STRING" -> {
        lexerStateBuilder.append(curr)
        when (curr) {
          null ->
            yieldE(InterpreterError(lineNo, sourceFname, "Unexpected end of file while parsing string"))
          '"' -> {
            lexerState = "DEFAULT"
            val lexeme = lexerStateBuilder.toString()
            val stringVal = LiteralVal.StringVal(lexeme.substring(1, lexeme.length - 1))
            yieldT(Token(TokenType.STRING, lexeme, stringVal, lineNo, sourceFname))
          }
        }
      }
      "COMMENT" -> {
        when(curr) {
          null, '\n' -> {
            lexerState = "DEFAULT"
            val lexeme = lexerStateBuilder.toString()
            yieldT(Token(TokenType.COMMENT, lexeme, null, lineNo, sourceFname))
            if (curr == null) yieldT(Token(TokenType.EOF, "", null, lineNo, sourceFname))
          }
          else -> 
            lexerStateBuilder.append(curr)
        }
      }
      "NUMBER" -> {
        when {
          curr?.isLetter() == true -> {
            yieldE(InterpreterError(lineNo, sourceFname, "Unexpected character while parsing number: expected digit or terminal but got ${curr}"))
          }
          curr?.isDigit() == true ->
            lexerStateBuilder.append(curr)
          curr == '.' -> { // tricky case
            lexerStateBuilder.append(curr)
          }
          else -> {
            lexerState = "DEFAULT"
            val lexeme = lexerStateBuilder.toString()
            if (lexeme.contains(".")) {
              val doubleVal = LiteralVal.DoubleVal(lexeme.toDouble())
              yieldT(Token(TokenType.NUMBER, lexeme, doubleVal, lineNo, sourceFname))
            } else {
              val intVal = LiteralVal.IntVal(lexeme.toInt())
              yieldT(Token(TokenType.NUMBER, lexeme, intVal, lineNo, sourceFname))
            }

            if (curr == null) yieldT(Token(TokenType.EOF, "", null, lineNo, sourceFname))
          }
        }
      }
      else -> {
        yieldE(InterpreterError(lineNo, sourceFname, "Unexpected lexer state ${lexerState}"))
      }
    }
  }
}

// Helper function to munch the simple singleton characters. Remember to greedy much 2ch first!!
suspend fun LexScope.tryDoMunch1(
  curr: Char,
  CHAR_LOOKUP: Map<Char, TokenType>,
  yieldT: suspend LexScope.(t: Token) -> Unit,
  lineNo: Int,
  sourceFname: String,
): Boolean {
  val maybeTokenType = CHAR_LOOKUP.get(curr)
  CHAR_LOOKUP.get(curr)?.let { tokenType -> 
    yieldT(Token(tokenType, curr.toString(), null, lineNo, sourceFname))
    return true
  }
  return false
}

// Helper function to munch the 2character lexemes
// (tryDoMunch2(curr, nxt1, Token.LOOKUP_2CH_TO_TOKEN, yieldT, lineNo, sourceFname)) ->
suspend fun LexScope.tryDoMunch2(
  curr: Char,
  nxt1: Char?,
  CHAR_LOOKUP: Map<Pair<Char, Char>, TokenType>,
  yieldT: suspend LexScope.(t: Token) -> Unit,
  lineNo: Int,
  sourceFname: String,
): Boolean {
  if (nxt1 == null) return false

  CHAR_LOOKUP.get(Pair(curr, nxt1))?.let { tokenType ->
    yieldT(Token(tokenType, curr.toString() + nxt1.toString(), null, lineNo, sourceFname))
    return true
  }
  return false
}


// Helper function to iterate through a array and peek ahead at it.
// Always returns a final 3xnull.
fun <T> Iterable<T>.peekAhead3(): List<Triple<T?, T?, T?>> {
  var prev2: T? = null
  var prev: T? = null
  val ls: Iterable<T> = this

  return sequence<Triple<T?, T?, T?>> {
    ls.forEach { it -> 
      if (prev2 != null && prev != null) {
        yield(Triple(prev2!!, prev!!, it))
      }
      prev2 = prev
      prev = it
    }
    if (prev2 != null) {
      yield(Triple(prev2!!,prev,null))
    }
    if (prev != null) {
      yield(Triple(prev!!,null,null))
    }
     yield(Triple(null,null,null))
  }.toList()
}



fun run(ss: String, sourceFname: String): Pair<List<Token>, List<InterpreterError>> {
  val lexingBlobs = sequence<EmittableLexingBlob> {
    coRun(ss, sourceFname,
      { t -> yield(EmittableLexingBlob.Tok(t)) },
      { err -> yield(EmittableLexingBlob.Err(err)) },
    )
  }.toList<EmittableLexingBlob>()

  // split the sequence
  val ts = mutableListOf<Token>()
  val es = mutableListOf<InterpreterError>()

  lexingBlobs.forEach { it ->
    when(it) {
      is EmittableLexingBlob.Err -> { es.add(it.e) }
      is EmittableLexingBlob.Tok -> { ts.add(it.t) }
    }
  }

  return Pair(ts, es)
}

sealed interface EmittableLexingBlob {
  data class Err(val e: InterpreterError): EmittableLexingBlob
  data class Tok(val t: Token): EmittableLexingBlob
}

