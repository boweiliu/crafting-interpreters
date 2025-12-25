package lexing

enum class LexerState {
  DEFAULT,
  STRING,
  COMMENT,
  NUMBER,
  ALPHA,
}

data class LexerStateData(
  val state: LexerState = LexerState.DEFAULT,
  val builder: StringBuilder = StringBuilder(),
)

data class LToken(
  val type: TokenType,
  val lexeme: String,
  val literal: LiteralVal,
)

data class LError(
  val type: InterpreterErrorType,
  val msg: String,
)

fun getIntermediateState(
  prev: LexerState,
  curr: Char?, nxt1: Char?, nxt2: Char?
): LexerState? {
  return null
}

fun computeForTransition(
  prev: LexerStateData, med: LexerState, cause: Char?
): Pair<LToken?, LError?> {
  return Pair(null, null)
}

data class Quadruple<T1, T2, T3, T4>(
  val first: T1,
  val second: T2,
  val third: T3,
  val fourth: T4,
)

fun computeForChar(
  stateData: LexerStateData,
  curr: Char?, nxt1: Char?, nxt2: Char?
): Quadruple<LToken?, LError?, Boolean, LexerState?> {
  return Quadruple(null, null, false, null)
}

