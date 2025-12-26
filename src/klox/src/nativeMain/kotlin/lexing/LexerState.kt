package lexing

/*
The fundamental problem with lexers/stream state machines...
is that there is not a one-to-one mapping between input tokens and state machine actions.

Often the sensible thing to do is to munch 2 tokens at once (bigrams)
Other times, we see that a state transition appears to correspond with a "gap between tokens",
(e.g. 123+, seeing the "+" causes us to finish the number and emit another symbol immediately).

For this implementation, we will choose to view the mapping from the perspective
of the input tokens, which should lead to reading them approximately once each,
(giving ourselves a little leeway for peeking), rather than
operating from the perspective of the semantic state transitions.
*/

enum class LexerState {
  DEFAULT,
  STRING,
  BIGRAM,
  COMMENT,
  NUMBER,
  ALPHA,
  EOF,
}

data class LexerStateData(
  val state: LexerState = LexerState.DEFAULT,
  val builder: StringBuilder = StringBuilder(),
  // We keep parsing and just note down the error
  var didError: Boolean = false,
)

fun Char.isDigitLetterUnder(): Boolean = 
  (this.isLetter() || this.isDigit() || this == '_')

fun computeLexerActionDatas(
  old: LexerStateData,
  curr: Char?, nxt1: Char?, nxt2: Char?
): LDatas {
  when {
    old.state == LexerState.EOF -> {
      return LDatas.of(LError.BAD_STATE(old, curr))
    }
    curr == null -> {
      if (old.state == LexerState.BIGRAM ||
          old.state == LexerState.STRING) {
        return LDatas.of(
          LError.EARLY_EOF(old, curr),
          LTransition(LexerState.EOF),
          LToken(TokenType.EOF, ""),
        )
      } else 
        return LDatas.of(LTransition(LexerState.EOF), LToken(TokenType.EOF, ""))
    }
    old.state == LexerState.BIGRAM -> {
      return LDatas.of(LTransition(LexerState.DEFAULT))
    }
    old.state == LexerState.STRING -> {
      when {
        curr == '"' ->
          return LDatas.of(LUpdateC(curr), LTransition(LexerState.DEFAULT))
        else ->
          return LDatas.of(LUpdateC(curr))
      }
    }
    old.state == LexerState.COMMENT -> {
      when {
        curr == '\n' ->
          return LDatas.of(LTransition(LexerState.DEFAULT))
        else ->
          return LDatas.of(LUpdateC(curr))
      }
    }
  }
  if (old.state == LexerState.NUMBER) {
    when {
      (curr.isLetter() || curr == '_') ->
        return LDatas.of(LUpdateC(curr), LUpdateE(true), LError.NUMBER_NO_LETTER(old, curr))
      (curr.isDigit()) ->
        return LDatas.of(LUpdateC(curr))
      // (curr == '.' && nxt1?.isDigit() == true && !old.builder.contains(".")) -> 
      //   return LDatas.of(LUpdateC(curr))
      (curr == '.') -> {
        if (old.builder.contains(".")) 
          return LDatas.of(LError.NUMBER_DOUBLE_DECIMAL(old, curr), LUpdateC(curr), LUpdateE(true))
        else if (nxt1?.isDigitLetterUnder() != true) 
          return LDatas.of(LError.NUMBER_FINAL_DECIMAL(old, curr), LUpdateC(curr), LUpdateE(true))
        else
          return LDatas.of(LUpdateC(curr))
      }
    }
  }
  if (old.state == LexerState.ALPHA) {
    when {
      curr.isDigitLetterUnder() ->
        return LDatas.of(LUpdateC(curr))
    }
  }

  when {
    (curr == ' ' || curr == '\t' || curr == '\n' || curr == '\r') -> 
      return LDatas.of(LTransition(LexerState.DEFAULT))
    (curr == '"') ->
      return LDatas.of(LTransition(LexerState.STRING), LUpdateC(curr))
    (curr == '/' && nxt1 == '/') ->
      return LDatas.of(LTransition(LexerState.COMMENT), LUpdateC(curr))
    (curr.isDigit()) ->
      return LDatas.of(LTransition(LexerState.NUMBER), LUpdateC(curr))
    (curr.isLetter() || curr == '_') ->
      return LDatas.of(LTransition(LexerState.ALPHA), LUpdateC(curr))
    else ->
      return tryMunch2(curr, nxt1, Token.LOOKUP_2CH_TO_TOKEN)
        ?.let { LDatas.of(LTransition(LexerState.BIGRAM), it) }
      ?: tryMunch1(curr, Token.LOOKUP_1CH_TO_TOKEN)
        ?.let { LDatas.of(LTransition(LexerState.DEFAULT), it) }
      ?: LDatas.of(LError.UNKNOWN_CHAR(old, curr))
  }
  return LDatas()
}


// Compute how to handle a state transition (usually by dumping the stringbuilder data out).
// Should not mutate oldStateData.
fun computeForTransition(
  oldStateData: LexerStateData, toState: LexerState, cause: Char?
): LTriple<LToken?, LError?, LexerStateData?> {
  return when (oldStateData.state) {
    LexerState.BIGRAM,
    LexerState.DEFAULT -> 
      LTriple()
    LexerState.COMMENT ->
      LTriple(LToken(TokenType.COMMENT, oldStateData.builder.toString()))
    LexerState.STRING -> {
      val lexeme = oldStateData.builder.toString()

      if (lexeme.length >= 2 && lexeme[0] == '"' && lexeme[lexeme.length - 1] == '"') {
        val stringVal = LiteralVal.StringVal(lexeme.substring(1, lexeme.length - 1))
        LTriple(LToken(TokenType.STRING, lexeme, stringVal))
      } else {
        LTriple(null, InterpreterErrorType.UNPARSEABLE_STRING.toLError(lexeme))
      }
    }
    LexerState.NUMBER -> {
      if (oldStateData.didError) {
        LTriple()
      } else {
        val lexeme = oldStateData.builder.toString()
        if (lexeme.lastOrNull() == '.') 
          LTriple(null, InterpreterErrorType.ILLEGAL_FINAL_DECIMAL_NUMBER.toLError(lexeme))
        else if (lexeme.contains("."))
          lexeme.toDoubleOrNull() ?.let { LiteralVal.DoubleVal(it) } ?.let { 
            LTriple(LToken(TokenType.NUMBER, lexeme, it))
          } ?: 
            LTriple(null, InterpreterErrorType.UNPARSEABLE_DOUBLE_NUMBER.toLError(lexeme))
        else
          lexeme.toIntOrNull() ?.let { LiteralVal.IntVal(it) } ?.let { 
            LTriple(LToken(TokenType.NUMBER, lexeme, it))
          } ?: 
          lexeme.toDoubleOrNull() ?.let { LiteralVal.DoubleVal(it) } ?.let { 
            LTriple(LToken(TokenType.NUMBER, lexeme, it))
          } ?: LTriple(null, InterpreterErrorType.UNPARSEABLE_INT_NUMBER.toLError(lexeme))
      }
    }
    LexerState.ALPHA -> {
      val lexeme = oldStateData.builder.toString()
      Token.LOOKUP_ALPHA_TO_TOKEN.get(lexeme.uppercase())?.let { type ->
        LTriple(LToken(type, oldStateData.builder.toString()))
      } ?: 
        LTriple(LToken(TokenType.IDENTIFIER, oldStateData.builder.toString()))
    }
    else ->
      LTriple(null, InterpreterErrorType.UNHANDLED_LEXER_STATE.toLError(oldStateData.state))
  }
}


fun tryMunch1(curr: Char, LOOKUP_MAP: Map<Char, TokenType>): LToken? {
  LOOKUP_MAP.get(curr)?.let { type ->
    return LToken(type, curr.toString())
  } ?:
    return null
}

fun tryMunch2(curr: Char, nxt: Char?, LOOKUP_MAP: Map<Pair<Char, Char>, TokenType>): LToken? {
  if (nxt == null) return null

  LOOKUP_MAP.get(Pair(curr, nxt))?.let { type ->
    return LToken(type, curr.toString() + nxt.toString())
  } ?:
    return null
}

data class LTriple<T1 : LToken?, T2 : LError?, T3: LexerStateData?>(
  val first: T1? = null,
  val second: T2? = null,
  val third: T3? = null,
)


data class LToken(
  val type: TokenType,
  val lexeme: String,
  val literal: LiteralVal? = null,
)

data class LError(
  val type: InterpreterErrorType,
  val msg: String,
) {
  companion object {}
}


fun InterpreterErrorType.toLError(vararg args: Any?): LError {
  return LError(this, this.fformat(*args))
}

sealed class LDatum(val ty: String) {
  data class Er(val er: LError): LDatum("Er")
  data class UpC(val up: LUpdateC): LDatum("Up")
  data class UpE(val up: LUpdateE): LDatum("Up")
  data class Tr(val tr: LTransition): LDatum("Tr")
  data class To(val to: LToken): LDatum("To")
}

data class LTransition(val st: LexerState)
data class LUpdateC(val ch: Char)
data class LUpdateE(val didError: Boolean)

data class LDatas(val stuff: List<LDatum> = listOf()) {
  companion object {
    fun of(vararg args: Any?): LDatas {
      return LDatas(args.mapNotNull { dat ->
        when (dat) {
          null -> null
          is LTransition -> LDatum.Tr(dat)
          is LUpdateC    -> LDatum.UpC(dat)
          is LUpdateE    -> LDatum.UpE(dat)
          is LError      -> LDatum.Er(dat)
          is LToken      -> LDatum.To(dat)
          else -> null
        }
      }.toList())
    }
  }
}

fun LError.Companion.BAD_STATE(ll: LexerStateData, curr: Char?) =
  InterpreterErrorType.UNHANDLED_LEXER_STATE.toLError(ll.state)

fun LError.Companion.EARLY_EOF(ll: LexerStateData, curr: Char?) =
  if (ll.state == LexerState.BIGRAM)
    InterpreterErrorType.UNEXPECTED_EOF_BIGRAM.toLError()
  else if (ll.state == LexerState.STRING)
    InterpreterErrorType.UNEXPECTED_EOF_STRING.toLError(ll.builder.toString())
  else
    InterpreterErrorType.UNHANDLED_LEXER_STATE.toLError(ll.state)

fun LError.Companion.NUMBER_NO_LETTER(ll: LexerStateData, curr: Char?) =
  InterpreterErrorType.ILLEGAL_CHARACTER_NUMBER.toLError(ll.builder.toString(), curr)
fun LError.Companion.NUMBER_DOUBLE_DECIMAL(ll: LexerStateData, curr: Char?) =
  InterpreterErrorType.ILLEGAL_CHARACTER_NUMBER.toLError(ll.builder.toString(), curr)
fun LError.Companion.NUMBER_FINAL_DECIMAL(ll: LexerStateData, curr: Char?) =
  InterpreterErrorType.ILLEGAL_FINAL_DECIMAL_NUMBER.toLError(ll.builder.toString() + curr.toString(), curr)
fun LError.Companion.UNKNOWN_CHAR(ll: LexerStateData, curr: Char?) =
  InterpreterErrorType.UNRECOGNIZED_CHARACTER.toLError(curr)

