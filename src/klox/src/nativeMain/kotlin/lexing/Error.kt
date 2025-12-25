package lexing

data class InterpreterError(
  val locationLineNo: Int,
  val locationFileName: String,
  val message: String,
) {
  constructor(errType: InterpreterErrorType, locationLineNo: Int, locationFileName: String, message: String) :
    this(locationLineNo, locationFileName, "[${errType.sev}${errType.code}]: " + message)

  companion object {}
}

enum class InterpreterErrorType(val sev: String, val code: String) {
  UNHANDLED_LEXER_STATE         ("E", "000"),
  UNRECOGNIZED_CHARACTER        ("E", "010"),
  UNEXPECTED_EOF_BIGRAM         ("E", "020"),
  UNEXPECTED_EOF_STRING         ("E", "021"),
  ILLEGAL_CHARACTER_NUMBER      ("E", "030"),
  ILLEGAL_FINAL_DECIMAL_NUMBER  ("E", "031"),
  UNPARSEABLE_DOUBLE_NUMBER     ("E", "032"),
  UNPARSEABLE_INT_NUMBER        ("E", "033"),
  PARSED_INT_AS_DOUBLE          ("W", "034"),
  NEVER                         ("E", "990"),
  ;

  companion object {}
}

val InterpreterErrorType.Companion.MESSAGE_TEMPLATE_MAP: Map<InterpreterErrorType, String>
  get() = mapOf(
    InterpreterErrorType.UNHANDLED_LEXER_STATE to
      "Unhandled lexer state [%s] (this is probably a compiler bug)",
    InterpreterErrorType.UNRECOGNIZED_CHARACTER to
      "Unexpected character '%s'",
    InterpreterErrorType.UNEXPECTED_EOF_BIGRAM to
      "Unexpected end of file while parsing bigram (this is probably a compiler bug)'",
    InterpreterErrorType.UNEXPECTED_EOF_STRING to
      "Unexpected end of file while parsing string (did you forget a '\"'?) String so far:\n%s",
    InterpreterErrorType.ILLEGAL_CHARACTER_NUMBER to
      "Illegal character while parsing number '%s': expected digit or decimal but got '%s'",
    InterpreterErrorType.ILLEGAL_FINAL_DECIMAL_NUMBER to
      "Illegal character while parsing number '%s': expected numbers following decimal point but got %s",
    InterpreterErrorType.UNPARSEABLE_DOUBLE_NUMBER to
      "Could not parse float '%s', ignoring",
    InterpreterErrorType.UNPARSEABLE_INT_NUMBER to
      "Could not parse int/float '%s', ignoring",
    InterpreterErrorType.PARSED_INT_AS_DOUBLE to
      "Warning: parsed int '%s' as double, possible precision loss",
  )

fun InterpreterErrorType.fformat(vararg args: Any?): String {
  InterpreterErrorType.MESSAGE_TEMPLATE_MAP.get(this)?.let { template: String ->
    return template.fformat(*args)
  } ?:
    return "Missing error string template for ${this.name}"
}

fun String.fformat(vararg args: Any?): String {
  var out: String = this
  val insertions: MutableList<Any?> = args.toMutableList()
  while (out.contains("%s")) {
    if (insertions.isNotEmpty())
      out = out.replace("%s", insertions.removeFirst().toString())
  }
  return out
}

suspend fun <T> SequenceScope<T>.test_here(cb: suspend SequenceScope<T>.() -> Unit): Unit {
  this.cb()
}

fun InterpreterError.reportToStdout(): Unit {
  println("Error: ${this.message.replace("\n","\\n")} at ${this.locationFileName}:${this.locationLineNo}")
}


