package lexing

import platform.posix.*
import kotlinx.cinterop.*
// import platform.posix.readlink;


// fun getCurrentPid(): Int = 0

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
fun getCurrentExecutablePath(): String {
  return memScoped {
    val buflen: Int = 32768
    val len: ULong = (buflen - 1).toULong()
    val buf = ByteArray(buflen)
    
    buf.usePinned { pinned ->
      readlink("/proc/self/exe", pinned.addressOf(0), len)
    }
    buf.decodeToString()
  }
}

val EXE_PATH: String by lazy { getCurrentExecutablePath() }


// @OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
// fun getFileFrom

// hmmm. we have pointer addrs but not files/lines.
// solution: look up our exe file, then run /bin/bash -c 'addr2line -C -e ME.EXE addr'
// to look up your exe file, first find our pid and then read /proc/pid/exe
// or just read /proc/self/exe
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
fun getCurrentStacktrace(): String {
  val e = Throwable()
  val structured: Array<String> = e.getStackTrace()
  val addrs: List<String> = structured.map { it -> 
    val entries: List<String> = it.split("\\s+".toRegex())
    val (idx, fname, addr) = entries.take(3)
    val rest = entries.drop(3).joinToString(" ")
    addr
  }.toList()
  // structured.take(3).forEach { it -> 
  //   // println(it)
  //   val entries: List<String> = it.split("\\s+".toRegex())
  //   val (idx, fname, addr) = entries.take(3)
  //   val rest = entries.drop(3).joinToString(" ")
  //   // println(listOf<String>(idx, fname, addr, EXE_PATH, rest).joinToString(" | "))
  // }
  // val fullString = e.stackTraceToString()
  return addrs.drop(3).take(5).joinToString(",") ?: ""
}

data class InterpreterError(
  val locationLineNo: Int,
  val locationFileName: String,
  val message: String,
) {
  constructor(errType: InterpreterErrorType, locationLineNo: Int, locationFileName: String, message: String) :
    this(locationLineNo, locationFileName, "[${errType.sev}${errType.code}]: " + message + " @ " + getCurrentStacktrace())

  companion object {}
}

enum class InterpreterErrorType(val sev: String, val code: String) {
  UNHANDLED_LEXER_STATE         ("E", "000"),
  UNRECOGNIZED_CHARACTER        ("E", "010"),
  UNEXPECTED_EOF_BIGRAM         ("E", "020"),
  UNEXPECTED_EOF_STRING         ("E", "021"),
  UNPARSEABLE_STRING            ("E", "025"),
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
      "Illegal character while parsing number '%s': expected numbers following decimal point but did not find one",
    InterpreterErrorType.UNPARSEABLE_DOUBLE_NUMBER to
      "Could not parse float '%s', ignoring",
    InterpreterErrorType.UNPARSEABLE_INT_NUMBER to
      "Could not parse int/float '%s', ignoring",
    InterpreterErrorType.UNPARSEABLE_STRING to
      "Could not parse string '%s', ignoring",
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
    if (insertions.isEmpty())
      break
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


