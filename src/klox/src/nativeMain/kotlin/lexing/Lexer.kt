
data class InterpreterError(
  val locationLineNo: Int,
  val locationFile: String,
  val message: String,
)

fun InterpreterError.reportToStdout(): Unit {
  println("Error: ${this.message} at ${this.locationFile}:${this.locationLineNo}")
}

fun run(ss: String, sourceFname: String): List<InterpreterError> {
  return sequence<InterpreterError> {
    // TODO: start scanning

    // test yielding data/errors
    this.test_here({ yield(InterpreterError(-1, sourceFname, ss)) })
  }.toList<InterpreterError>()
}

/*
suspend fun SequenceScope<InterpreterError>.test_here(cb: suspend SequenceScope<InterpreterError>.() -> Unit): Unit {
  this.cb()
}
*/
