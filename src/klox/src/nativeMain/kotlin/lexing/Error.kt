package lexing

data class InterpreterError(
  val locationLineNo: Int,
  val locationFileName: String,
  val message: String,
)

suspend fun <T> SequenceScope<T>.test_here(cb: suspend SequenceScope<T>.() -> Unit): Unit {
  this.cb()
}

fun InterpreterError.reportToStdout(): Unit {
  println("Error: ${this.message.replace("\n","\\n")} at ${this.locationFileName}:${this.locationLineNo}")
}


