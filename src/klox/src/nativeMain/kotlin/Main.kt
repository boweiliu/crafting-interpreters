import kotlin.system.exitProcess
import okio.Path

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
  // val contents = File(fname).readText(Charsets.UTF_8)
  return 0
}

fun runPrompt(): Int {
  while (true) {
    val ln: String? = readlnOrNull()
    if (ln == null) {
      return 0
    }
  }
}
