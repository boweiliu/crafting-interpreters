import kotlin.system.exitProcess
import okio.*
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath

import lexing.*

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
  val (_, errors) = runAll(fileContents, fname)

  if (errors.size > 0) {
    // Report all the errors
    errors.forEach { err -> err.reportToStdout() }
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
    val (_, errors) = runAll(ln, "<stdout>")
    errors.forEach { err -> err.reportToStdout() }
  }
}
