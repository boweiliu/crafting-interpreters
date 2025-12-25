package lexing

enum class LexerState {
  DEFAULT,
  EOF,
  STRING_START,
  STRING,
  COMMENT,
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
  old: LexerState,
  curr: Char?, nxt1: Char?, nxt2: Char?
): LexerState? {

  if (old == LexerState.EOF)
    return null
  if (curr == null)
    return LexerState.EOF

  return when(old) {
    LexerState.DEFAULT -> {
      when {
        curr == '"' ->
          LexerState.STRING_START
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
        return LQuad(null, InterpreterErrorType.UNHANDLED_LEXER_STATE.toLError(stateData.state))
      if (curr == ' ' || curr == '\t' || curr == '\n' || curr == '\r')
        return LQuad()
      tryMunch2(curr, nxt1, Token.LOOKUP_2CH_TO_TOKEN) ?.let {
        return LQuad(it, null, true)
      }
      tryMunch1(curr, Token.LOOKUP_1CH_TO_TOKEN) ?.let {
        return LQuad(it)
      }
      return LQuad(null, InterpreterErrorType.UNRECOGNIZED_CHARACTER.toLError(curr))
    }
    LexerState.STRING_START -> {
        return LQuad(null, null, false, LexerState.STRING)
    }
    LexerState.STRING -> {
      stateData.builder.append(curr)
      if (curr == '"') {
        return LQuad(null, null, false, LexerState.DEFAULT)
      }
      return LQuad()
    }
    LexerState.COMMENT -> {
      if (curr == '\n') {
        return LQuad(null, null, false, LexerState.DEFAULT)
      } else {
        stateData.builder.append(curr)
        return LQuad()
      }
    }
    LexerState.NUMBER -> {
    }
    else -> {
    }
  }
  return LQuad(null, null, false, null)
}

data class LTriple<T1 : LToken?, T2 : LError?, T3: LexerState?>(
  val first: T1? = null,
  val second: T2? = null,
  val third: T3? = null,
)

// Compute how to handle a state transition (usually by dumping the stringbuilder data out).
fun computeForTransition(
  oldStateData: LexerStateData, toState: LexerState, cause: Char?
): LTriple<LToken?, LError?, LexerState?> {
  return when (oldStateData.state) {
    LexerState.EOF,
    LexerState.DEFAULT -> 
      LTriple()
    LexerState.COMMENT ->
      LTriple(LToken(TokenType.COMMENT, oldStateData.builder.toString()))
    LexerState.STRING_START -> {
      LTriple()
    }
    LexerState.STRING -> {
      val lexeme = oldStateData.builder.toString()
      val stringVal = LiteralVal.StringVal(lexeme.substring(1, lexeme.length - 1))
      LTriple(LToken(TokenType.STRING, lexeme, stringVal))
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


fun InterpreterErrorType.toLError(vararg args: Any?): LError {
  return LError(this, this.fformat(*args))
}
