import kotlin.system.exitProcess

fun main(args: Array<String>): Unit {
  println("hello from native kotlin rebuild here")
  println(args.joinToString(","))

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
  return 0
}

fun runPrompt(): Int {
  while (true) {
    val ln = readln()
    println(ln)
    if (ln.length == 0) {
      return 0
    }
  }
}
