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
    // suspend fun coYield(ig: T?): A // convienient for duoYield but without the type check
}

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
      return suspendCoroutine { cont -> nextStep = cont }
    }

    // Continuation/coroutine implementation
    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<T>): Unit {
        bufferedValue = result.getOrThrow()
        didFinish = true
    }
}
