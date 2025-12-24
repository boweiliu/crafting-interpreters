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
  run(fileContents)

  return 0
}

fun runPrompt(): Int {
  while (true) {
    print("> ")
    val ln: String? = readlnOrNull()
    if (ln == null) {
      print("\n")
      return 0
    }
    run(ln)
  }
}

fun run(ln: String): Unit {
  // TODO: start scanning

  println(ln)
}
