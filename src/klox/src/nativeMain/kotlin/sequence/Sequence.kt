package sequence

import kotlin.coroutines.*
import kotlin.experimental.*

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
    suspend fun coYield(value: T?): A // convienient for duoYield but without the type check
}

private class DuoSequenceCoroutine<A, T>:
  DuoSequenceScope<T, A>,
  Continuation<T>,
  DuoIterator<A, T>
{
    private var didStart = false // was the coroutine started at all
    private var didInit = false // did we pass the first initial coYield (receive the first input)
    private var didFinish = false // did we return the last output
    private var bufferedValue: T? = null
    lateinit var firstStep: Continuation<Unit>
    private lateinit var nextStep: Continuation<A>

    override fun hasStarted(): Boolean = didStart
    override fun canSend() = !didFinish

    override fun start() {
      didStart = true
      firstStep.resume(Unit)
    }

    override fun send(aa: A): T {
      nextStep.resume(aa)
      return bufferedValue!!.also { bufferedValue = it }
    }

    // Generator implementation
    override suspend fun duoYield(value: T): A {
      bufferedValue = value
      return suspendCoroutine { cont -> nextStep = cont }
    }

    override suspend fun initCoYield(): A {
      didInit = true
      return suspendCoroutine { cont -> nextStep = cont }
    }

    override suspend fun coYield(value: T?): A {
      return if (didInit) {
        this.duoYield(value as T)
      } else {
        this.initCoYield()
      }
    }

    // Continuation/coroutine implementation
    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>): Unit {
        bufferedValue = result.getOrThrow()
        didFinish = true
    }
}
