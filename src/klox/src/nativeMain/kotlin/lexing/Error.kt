package lexing

data class InterpreterError(
  val locationLineNo: Int,
  val locationFileName: String,
  val message: String,
) {
  constructor(errType: InterpreterErrorType, locationLineNo: Int, locationFileName: String, message: String) :
    this(locationLineNo, locationFileName, "[E${errType.code}]: " + message)

  companion object {}
}

enum class InterpreterErrorType(val code: String) {
  UNEXPECTED_EOF("001"),
  ;

  companion object {}
}

val InterpreterErrorType.Companion.MESSAGE_TEMPLATE_MAP: Map<InterpreterErrorType, String>
  get() = mapOf()

suspend fun <T> SequenceScope<T>.test_here(cb: suspend SequenceScope<T>.() -> Unit): Unit {
  this.cb()
}

fun InterpreterError.reportToStdout(): Unit {
  println("Error: ${this.message.replace("\n","\\n")} at ${this.locationFileName}:${this.locationLineNo}")
}


