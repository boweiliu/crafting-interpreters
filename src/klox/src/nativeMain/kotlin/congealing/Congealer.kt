package congealing

import lexing.*

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<Token>, List<InterpreterError>> {
  return Pair(inputTokens, errsAcc.toList<InterpreterError>())
}

