package lexing

enum class LexerState {
  DEFAULT,
  STRING,
  COMMENT,
  EOF,
  NUMBER,
  ALPHA,
}

data class LexerStateData(
  val state: LexerState = LexerState.DEFAULT,
  val builder: StringBuilder = StringBuilder(),
  val didError: Boolean = false,
)

// Compute the state transition when first encountering a character (but before processing it).
// aka the intermediate state or the pre-char state.
// Return null == no change.
fun getIntermediateState(
  prev: LexerState,
  curr: Char?, nxt1: Char?, nxt2: Char?
): LexerState? {

  if (prev == LexerState.EOF)
    return null
  if (curr == null)
    return LexerState.EOF

  return when(prev) {
    LexerState.DEFAULT -> {
      when {
        curr == '"' ->
          LexerState.STRING
        curr == '/' && nxt1 == '/' ->
          LexerState.COMMENT
        curr.isDigit() ->
          LexerState.NUMBER
        curr.isLetter() ->
          LexerState.ALPHA
        else ->
          null
      }
    }
    LexerState.STRING ->
      null
    LexerState.COMMENT -> {
      if (curr == '\n') LexerState.DEFAULT
      else null
    }
    LexerState.NUMBER -> {
      if (curr.isLetter() || curr == '_') null
      else if (curr.isDigit()) null
      else LexerState.DEFAULT
    }
    else -> {
      null
    }
  }
}

// Compute how to handle a character given we've already performed the pre-char state transition.
// This may result in another transition.
fun computeForChar(
  stateData: LexerStateData,
  curr: Char?, nxt1: Char?, nxt2: Char?
): LQuad<LToken?, LError?, Boolean, LexerState?> {
  when(stateData.state) {
    LexerState.EOF -> {
      return LQuad(LToken(TokenType.EOF, ""))
    }
    LexerState.DEFAULT -> {
      if (curr == null)
        return LQuad(null, InterpreterErrorType.UNHANDLED_LEXER_STATE.formatToLError())
      if (curr == ' ' || curr == '\t' || curr == '\n' || curr == '\r')
        return LQuad()
      tryMunch2(curr, nxt1, Token.LOOKUP_2CH_TO_TOKEN) ?.let {
        return LQuad(it, null, true)
      }
      tryMunch1(curr, Token.LOOKUP_1CH_TO_TOKEN) ?.let {
        return LQuad(it)
      }
      return LQuad(null, InterpreterErrorType.UNRECOGNIZED_CHARACTER.formatToLError(curr.toString()))
    }
    LexerState.STRING -> {
    }
    LexerState.COMMENT -> {
    }
    LexerState.NUMBER -> {
    }
    else -> {
    }
  }
  return LQuad(null, null, false, null)
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

fun InterpreterErrorType.formatToLError(vararg args: Any?): LError {
  return LError(this, this.fformat(*args))
}


// Compute how to handle a state transition (usually by dumping the stringbuilder data out).
fun computeForTransition(
  prev: LexerStateData, med: LexerState, cause: Char?
): Pair<LToken?, LError?> {
  return Pair(null, null)
}

data class LQuad<T1 : LToken?, T2 : LError?, T3 : Boolean, T4 : LexerState?>(
  val first: T1? = null,
  val second: T2? = null,
  val third: Boolean = false,
  val fourth: T4? = null,
)

data class LToken(
  val type: TokenType,
  val lexeme: String,
  val literal: LiteralVal? = null,
)

data class LError(
  val type: InterpreterErrorType,
  val msg: String,
)

