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

/*
fun obtain() {
  val source: List<Int> = listOf(1,2,3,4,5).asSequence()
  val acc: MutableList<Int> = mutableListOf(-1)

  val s = myEmitter<Int>(source) {
    while (true) {
      val it = obtain2()
      if (it >= 4) break
      acc.add(it * 10)
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

*/

/*

parserSequence = sequence {
  tokenStream.forEach { token ->
    val updates = stateMachine.send(token)

    updates.forEach { when(it) is parserBlob -> yield(it) }
  }
}
*/

