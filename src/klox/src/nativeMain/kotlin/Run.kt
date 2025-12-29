import lexing.*

fun runAll(ss: String, sourceFname: String): Pair<Unit, List<InterpreterError>> {
  val lexingBlobs = sequence<EmittableLexingBlob> {
    coRun(ss, sourceFname,
      { t -> yield(EmittableLexingBlob.Tok(t)) },
      { err -> yield(EmittableLexingBlob.Err(err)) },
    )
  }.toList<EmittableLexingBlob>()

  // split the sequence
  val ts = mutableListOf<Token>()
  val es = mutableListOf<InterpreterError>()

  lexingBlobs.forEach { it ->
    when(it) {
      is EmittableLexingBlob.Err -> { es.add(it.e) }
      is EmittableLexingBlob.Tok -> { ts.add(it.t) }
    }
  }

  return Pair(Unit, es)
}

fun runLexer(ss: String, sourceFname: String): Pair<Sequence<Token>, List<InterpreterError>> {
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
