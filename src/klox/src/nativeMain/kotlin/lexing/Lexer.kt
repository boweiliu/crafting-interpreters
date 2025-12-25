package lexing

typealias LexScope = SequenceScope<EmittableLexingBlob>

suspend fun LexScope.coRun(
  ss: String,
  sourceFname: String,
  yieldT: suspend LexScope.(t: Token) -> Unit,
  yieldE: suspend LexScope.(e: InterpreterError) -> Unit,
): Unit {
  // test yielding data/errors
  this.test_here({
    yieldE(InterpreterError(-1, sourceFname, ss))
  })

  // TODO: start scanning

  // while (true) {
    
  // }
}

// Helper function to iterate through a array and peek ahead at it
fun <T> peekAhead3(ls: Iterable<T>): List<Triple<T, T?, T?>> {
  var prev2: T? = null
  var prev: T? = null

  return sequence<Triple<T, T?, T?>> {
    ls.forEach { it -> 
      if (prev2 != null && prev != null) {
        yield(Triple(prev2!!, prev!!, it))
      }
      prev2 = prev
      prev = it
    }
    if (prev2 != null) {
      yield(Triple(prev2!!,prev,null))
    }
    if (prev != null) {
      yield(Triple(prev!!,null,null))
    }
  }.toList<Triple<T, T?, T?>>()
}





fun run(ss: String, sourceFname: String): List<InterpreterError> {
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

  return es
}

sealed interface EmittableLexingBlob {
  data class Err(val e: InterpreterError): EmittableLexingBlob
  data class Tok(val t: Token): EmittableLexingBlob
}

