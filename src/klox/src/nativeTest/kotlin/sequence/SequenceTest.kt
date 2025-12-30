package sequence
import kotlin.test.*

import io.kotest.matchers.*
import io.kotest.matchers.collections.*



@Test
fun my() {
  val acc: MutableList<Int> = mutableListOf(-1)
  val s = mySequence<Int> { 
    for (i in 1..3) {
      acc.add(i * 10)
      yield1(i)
    }
  }
  val it = s.iterator()
  acc.toList().shouldBe(listOf(-1))
  it.hasNext()
  acc.toList().shouldBe(listOf(-1,10))
  it.next()
  acc.toList().shouldBe(listOf(-1,10))
  it.next()
  acc.toList().shouldBe(listOf(-1,10,20))
  it.next()
  acc.toList().shouldBe(listOf(-1,10,20,30))
  it.hasNext().shouldBe(false)
}

@Test
fun obtain() {
  val source: Sequence<Int> = listOf(1,2,3,4,5).asSequence()
  val acc: MutableList<Int> = mutableListOf(-1)

  val s = myEmitter<Int>(source) {
    while (true) {
      val it = obtain2()
      if (it >= 4) break
      acc.add(it * 10)
      yield1(it)
    }
  }

  val it = s.iterator()
  acc.toList().shouldBe(listOf(-1))
  it.hasNext()
  acc.toList().shouldBe(listOf(-1,10))
  it.next()
  acc.toList().shouldBe(listOf(-1,10))
  it.next()
  acc.toList().shouldBe(listOf(-1,10,20))
  it.next()
  acc.toList().shouldBe(listOf(-1,10,20,30))
  it.hasNext().shouldBe(false)
}

/*

Sequences expose a iterable interface.
on the coroutine side, our state machine calls "[await] yield(val)" -> paused, holding val
on the interface (nocoro) side, clients call "val x = iterator.next()" -> get it
next() is sort of like "go start doing work and give it back synchronously when you're done"

what we want: a DuoSequence which exposes a message-like interface.
on th interface side, we want to do "stuff = machine.duoNext(token)"
duoNext() is sort of like "go start doing work, synchronously, here's a token if you need it,
  give me back the result when you're done"
on the coroutine side, our state machine calls
  "{ firstToken -> ... nextToken = [await] duoYield(firstResult) ... " or
  "{ ..init.. token = [buff'd] coYield() ; ... ; [await] yield(result) && token = [buff'd] coYield()
  "{ (token, retSlot) = checkout() ; ... ; (nextToken, nextRetSlot) = (token, retSlot).yield(value) ; nextGuy = coYield()
 ""
    yieldStep({
      yield: prev,
      obtain: { val nextToken = kont() }
    })
    yield(prev) && val nextToken = coYield()
    lateinit var nextToken
    yieldDuo(prev, coYieldInto = { nextToken = it })


what I really want to write is something like

    return(stuffSoFar) jmpto HERE
    HERE{}: (val nextToken, ) cofn
    

hmmm... how to debug the coroutine state? would be nice if both the program state
 and any locals were inspectable and easily testable...
  basically: set up the locals and the current line of the program (and any context closed over),
  AND the locals and line number of the entire stack depth, so we can compare that in the test...
(it sounds like this is too hard for kotlin right now, but worth asking/considering later)
  

parserSequence = sequence {
  val stateMachine = duoSequence { behaviorFn }

  tokenStream.forEach { token ->
    val updates = stateMachine.send(token) // send is like yieldable

    updates.forEach { when(it) is parserBlob -> yield(it) }
  }
}

parserSequence.groupByLines().forEach { line -> execute(line) }
*/

