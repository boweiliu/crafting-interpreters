import lexing.*

fun runAll(ss: String, sourceFname: String = "<unnamed>"): Pair<Unit, List<InterpreterError>> {
  val errsAcc: MutableList<InterpreterError> = mutableListOf()
  val (tokenStream, ) = runLexer(ss, sourceFname, errsAcc)
  val (congealedStream, ) = runCongealer(tokenStream, sourceFname, errsAcc)

  congealedStream.toList()
  return Pair(Unit, errsAcc.toList())
}

/*
fun runLexer(
  ss: String, sourceFname: String = "<unnamed>",
  errsAcc: MutableList<in InterpreterError> = mutableListOf()
): Pair<Sequence<Token>, List<InterpreterError>> {
*/

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<Token>, List<InterpreterError>> {
  return Pair(inputTokens, errsAcc.toList<InterpreterError>())
}

