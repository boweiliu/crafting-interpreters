package sequence

import kotlin.coroutines.*
import kotlin.experimental.*


/**
 * Example of a sequence that receives yields().
 */

@RestrictsSuspension
interface SequenceScope<in T> {
    suspend fun yield1(value: T)
}

@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
fun <T> mySequence(
  @BuilderInference block: suspend SequenceScope<T>.() -> Unit
): Sequence<T> = 
    SequenceCoroutine<T>().apply {
        nextStep = block.createCoroutine(receiver = this, completion = this)
    }.asSequence()

private class SequenceCoroutine<T>: AbstractIterator<T>(), SequenceScope<T>, Continuation<Unit> {
    lateinit var nextStep: Continuation<Unit>

    override fun computeNext() {
      nextStep.resume(Unit)
    }

    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        result.getOrThrow() // bail out on error
        done()
    }

    // Generator implementation
    override suspend fun yield1(value: T) {
        setNext(value)
        return suspendCoroutine { cont -> nextStep = cont }
    }
}

@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
fun <T> myEmitter(
  data: Sequence<Int>,
  @BuilderInference block: suspend SequenceAndEmitterScope<T>.() -> Unit
): DuoIterator<Int, T> = 
    SequenceAndEmitterCoroutine<T>(data.iterator()).apply {
        nextStep = block.createCoroutine(receiver = this, completion = this)
    }

interface DuoIterator<A, T> {
  fun start(): Unit
  fun canSend(): Boolean
  fun send(a: A): T
  // Temp for testing/uncompile
  fun iterator(): Iterator<T>
}


@RestrictsSuspension
interface SequenceAndEmitterScope<in T> {
    suspend fun yield1(value: T)
    suspend fun obtain2(i: Int): Unit
    suspend fun duoYield(i: Int): Int
}

private class SequenceAndEmitterCoroutine<T>(val myData: Iterator<Int>): AbstractIterator<T>(), SequenceAndEmitterScope<T>, Continuation<Unit>, DuoIterator<Int, T> {
    lateinit var nextStep: Continuation<Unit>

    override fun start() { }
    override fun iterator() = this
    override fun canSend() = hasNext()
    override fun send(a: Int): T {
      // calls the continuation...?
      TODO()
    }

    override fun computeNext() {
      nextStep.resume(Unit)
    }

    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        result.getOrThrow() // bail out on error
        done()
    }

    // Generator implementation
    override suspend fun yield1(value: T) {
        setNext(value)
        return suspendCoroutine { cont -> nextStep = cont }
    }

    // Generator implementation
    override suspend fun obtain2(i: Int): Unit {
        TODO()
        // return myData.next()
    }

    override suspend fun duoYield(i: Int): Int {
        return 0
        // return myData.next()
    }
}


@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
fun <A, T> duoSequence(
  @BuilderInference block: suspend DuoSequenceScope<T, A>.() -> T
): DuoIterator<A, T> = 
    DuoSequenceCoroutine<A, T>().apply {
        firstStep = block.createCoroutine(receiver = this, completion = this)
    }

@RestrictsSuspension
interface DuoSequenceScope<in T, out A> {
    suspend fun duoYield(value: T): A
    suspend fun initCoYield(): A
    suspend fun finalYield(value: T): Unit
}


// interface DuoIterator<A, T> {
//   fun start(): Unit
//   fun canSend(): Boolean
//   fun send(a: A): T
//   // Temp for testing/uncompile
//   fun iterator(): Iterator<T>
// }

private class DuoSequenceCoroutine<A, T>:
  DuoSequenceScope<T, A>,
  Continuation<T>,
  DuoIterator<A, T>
{
    lateinit var firstStep: Continuation<Unit>
    lateinit var nextStep: Continuation<T>

    override fun iterator() = TODO()
    override fun start() { }
    override fun canSend() = false
    override fun send(a: A): T {
      // calls the continuation...?
      TODO()
    }

    // Generator implementation
    override suspend fun duoYield(value: T): A {
      TODO()
    }
    override suspend fun initCoYield(): A {
      TODO()
    }
    override suspend fun finalYield(value: T): Unit {
      TODO()
    }


    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>): Unit {
        result.getOrThrow() // bail out on error
    }

}
