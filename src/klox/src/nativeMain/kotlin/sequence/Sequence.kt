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
): Sequence<T> = 
    SequenceAndEmitterCoroutine<T>(data.iterator()).apply {
        nextStep = block.createCoroutine(receiver = this, completion = this)
    }.asSequence()



@RestrictsSuspension
interface SequenceAndEmitterScope<in T> {
    suspend fun yield1(value: T)
    suspend fun obtain(): Int
}

private class SequenceAndEmitterCoroutine<T>(val myData: Iterator<Int>): AbstractIterator<T>(), SequenceAndEmitterScope<T>, Continuation<Unit> {
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

    // Generator implementation
    override suspend fun obtain(): Int {
        return myData.next()
    }
}
