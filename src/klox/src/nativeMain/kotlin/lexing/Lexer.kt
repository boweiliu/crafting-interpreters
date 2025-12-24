package lexing

fun run(ss: String, sourceFname: String): List<InterpreterError> {
  val lexingBlobs = sequence<EmittableLexingBlob> {
    // TODO: start scanning

    // test yielding data/errors
    this.test_here({
      yield(EmittableLexingBlob.Err(InterpreterError(-1, sourceFname, ss)))
    })

  }.toList<EmittableLexingBlob>()

  return listOf()
}

sealed interface EmittableLexingBlob {
  data class Err(val e: InterpreterError): EmittableLexingBlob
  data class Token(val t: Token): EmittableLexingBlob
}
