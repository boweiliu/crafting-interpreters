import kotlin.system.exitProcess
import okio.*
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath

fun main(args: Array<String>): Unit {
  var retCode: Int;
  if (args.size > 1) {
    println("Usage: klox [script]")
    retCode = 64
  } else if (args.size == 1 ) {
    retCode = runFile(args[0])
  } else {
    retCode = runPrompt()
  }
  exitProcess(retCode)
  return
}

fun runFile(fname: String): Int {
  val lines: List<String> = sequence<String> {
    FileSystem.SYSTEM.source(fname.toPath()).use { fileSource ->
      fileSource.buffer().use { bufferedFileSource ->
        while (true) {
          val line: String = bufferedFileSource.readUtf8Line() ?: break
          // println(line)
          yield(line)
        }
      }
    }
  }.toList<String>()

  val fileContents: String = lines.joinToString("\n")
  val errors = run(fileContents, fname)

  if (errors.size > 0) {
    // Report all the errors
    errors.forEach { it.reportToStdout() }
    return 65
  } else {
    return 0
  }
}

fun runPrompt(): Int {
  while (true) {
    print("> ")
    val ln: String? = readlnOrNull()
    if (ln == null) {
      print("\n")
      return 0
    }
    val errors = run(ln, "<stdout>")
    errors.forEach { it.reportToStdout() }
  }
}

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

suspend fun SequenceScope<InterpreterError>.test_here(cb: suspend SequenceScope<InterpreterError>.() -> Unit): Unit {
  this.cb()
}
