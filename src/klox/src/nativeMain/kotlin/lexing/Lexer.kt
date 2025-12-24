package lexing

fun run(ss: String, sourceFname: String): List<InterpreterError> {
  return sequence<InterpreterError> {
    // TODO: start scanning

    // test yielding data/errors
    this.test_here({ yield(InterpreterError(-1, sourceFname, ss)) })
  }.toList<InterpreterError>()
}
