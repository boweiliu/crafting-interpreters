import lexing.*

fun runAll(ss: String, sourceFname: String = "<unnamed>"): Pair<Unit, List<InterpreterError>> {
  val errsAcc: MutableList<InterpreterError> = mutableListOf()
  val (tokenStream, ) = runLexer(ss, sourceFname, errsAcc)
  val (congealedStream, ) = runCongealer(tokenStream, sourceFname, errsAcc)

  congealedStream.toList()
  return Pair(Unit, errsAcc.toList())
}

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<Token>, List<InterpreterError>> {
  return Pair(inputTokens, errsAcc.toList<InterpreterError>())
}

fun runLexer(
  ss: String, sourceFname: String = "<unnamed>",
  errsAcc: MutableList<in InterpreterError> = mutableListOf()
): Pair<Sequence<Token>, List<InterpreterError>> {

  val lexingBlobs = sequence<EmittableLexingBlob> {
    coRun(ss, sourceFname,
      { t -> yield(EmittableLexingBlob.Tok(t)) },
      { err -> yield(EmittableLexingBlob.Err(err)) },
    )
  }

  // split the sequence
  val ts = mutableListOf<Token>()
  val es = mutableListOf<InterpreterError>()
  val tokens = sequence<Token> {
    lexingBlobs.forEach { it ->
      when(it) {
        is EmittableLexingBlob.Err -> { es.add(it.e) }
        is EmittableLexingBlob.Tok -> { ts.add(it.t); yield(it.t) }
      }
    }
  }

  return Pair(tokens, es)
}
