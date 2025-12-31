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

@OptIn(kotlin.experimental.ExperimentalTypeInference::class)
fun <T> duoSequence(
  @BuilderInference block: suspend SequenceAndEmitterScope<T>.() -> Unit
): DuoIterator<Int, T> = 
    SequenceAndEmitterCoroutine<T>(listOf<Int>().asSequence().iterator()).apply {
        nextStep = block.createCoroutine(receiver = this, completion = this)
    }

interface DuoIterator<A, T> {
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
