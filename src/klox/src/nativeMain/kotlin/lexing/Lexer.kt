package lexing

typealias LexScope = SequenceScope<EmittableLexingBlob>

suspend fun LexScope.coRun(
  ss: String,
  sourceFname: String,
  yieldT: suspend LexScope.(t: Token) -> Unit,
  yieldE: suspend LexScope.(e: InterpreterError) -> Unit,
): Unit {
  // TODO: start scanning

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

  indexedCharacters.peekAhead3().forEach { (currPair, nxt1Pair, nxt2Pair) ->
    val (lineNo, curr) = currPair
    val (nxt1, nxt2) = Pair(nxt1Pair?.second, nxt2Pair?.second)

    // BODY goes here
    if (tryDoMunch1(curr, Token.LOOKUP_1CH_TO_TOKEN, yieldT, lineNo, sourceFname)) {
      return@forEach
    } else {
      // dont forget to error if we are confused
      yieldE(InterpreterError(lineNo, sourceFname, "Unexpected character '${curr}'"))
    }
  }

  yieldT(Token(TokenType.EOF, "", null, numLines + 1, sourceFname))
}

// Helper function to munch the simple singleton characters
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





// Helper function to iterate through a array and peek ahead at it
fun <T> Iterable<T>.peekAhead3(): List<Triple<T, T?, T?>> {
  var prev2: T? = null
  var prev: T? = null
  val ls: Iterable<T> = this

  return sequence<Triple<T, T?, T?>> {
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
  }.toList<Triple<T, T?, T?>>()
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

