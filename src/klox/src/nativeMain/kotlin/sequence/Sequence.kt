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
  fun hasStarted(): Boolean
  fun start(): Unit
  fun canSend(): Boolean
  fun send(aa: A): T

  fun iterator(ins: Sequence<A>): Iterator<T> {
    val seq: Sequence<T> = sequence {
      this@DuoIterator.start()
      ins.forEach { aa ->
        if (this@DuoIterator.canSend())
          yield(this@DuoIterator.send(aa))
        else
          return@sequence
      }
    }
    return seq.iterator()
  }

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

    private var didStart = false
    override fun hasStarted() = didStart
    override fun start() {
      didStart = true
    }
    override fun iterator() = this
    override fun canSend() = hasNext()
    override fun send(aa: Int): T {
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
    suspend fun initCoYield(ig: T?): A = initCoYield()
    // suspend fun coYield(ig: T?): A // convienient for duoYield but without the type check
    // suspend fun finalYield(value: T): Unit
}


// interface DuoIterator<A, T> {
//   fun start(): Unit
//   fun canSend(): Boolean
//   fun send(aa: A): T
//   // Temp for testing/uncompile
//   fun iterator(): Iterator<T>
// }

private class DuoSequenceCoroutine<A, T>:
  DuoSequenceScope<T, A>,
  Continuation<T>,
  DuoIterator<A, T>
{
    private var didStart = false
    private var didFinish = false
    private var bufferedValue: T? = null
    lateinit var firstStep: Continuation<Unit>
    private lateinit var nextStep: Continuation<A>

    override fun iterator() = TODO() // legacy thing, unneeded
    override fun hasStarted(): Boolean = didStart
    override fun start() {
      didStart = true
      firstStep.resume(Unit)
      // TODO() ??
    }
    override fun canSend() = !didFinish
    override fun send(aa: A): T {
      nextStep.resume(aa)
      val result: T = bufferedValue!!
      bufferedValue = null
      return result

      // calls the continuation...? or...?
      // return suspendCoroutine { cont -> nextStep = cont ; tt }
    }

    // Generator implementation
    override suspend fun duoYield(value: T): A {
      bufferedValue = value
      return suspendCoroutine { cont -> nextStep = cont }
    }
    override suspend fun initCoYield(): A {
      return suspendCoroutine { cont -> nextStep = cont }
    }
    // override suspend fun finalYield(value: T): Unit { TODO() }


    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>): Unit {
        bufferedValue = result.getOrThrow()
        didFinish = true
    }
}
