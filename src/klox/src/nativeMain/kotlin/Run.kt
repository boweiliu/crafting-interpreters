import lexing.*
import congealing.*
import interpreting.*

fun runAll(ss: String, sourceFname: String = "<unnamed>"): Pair<Unit, List<InterpreterError>> {
  val errsAcc: MutableList<InterpreterError> = mutableListOf()
  val (tokenStream, ) = runLexer(ss, sourceFname, errsAcc)
  val (congealedStream, ) = runCongealer(tokenStream, sourceFname, errsAcc)

  runInterpreter(congealedStream)
  return Pair(Unit, errsAcc.toList())
}
