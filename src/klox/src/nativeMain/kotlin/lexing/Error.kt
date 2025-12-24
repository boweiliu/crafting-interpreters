package lexing

data class InterpreterError(
  val locationLineNo: Int,
  val locationFileName: String,
  val message: String,
)

suspend fun SequenceScope<InterpreterError>.test_here(cb: suspend SequenceScope<InterpreterError>.() -> Unit): Unit {
  this.cb()
}

fun InterpreterError.reportToStdout(): Unit {
  println("Error: ${this.message.replace("\n","\\n")} at ${this.locationFileName}:${this.locationLineNo}")
}


