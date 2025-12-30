package sequence
import kotlin.test.*

import io.kotest.matchers.*
import io.kotest.matchers.collections.*



@Test
fun my() {
  val acc: MutableList<Int> = mutableListOf(-1)
  val s = mySequence<Int> { 
    for (i in 1..3) {
      acc.add(0)
      yield1(i)
    }
  }
  s.iterator().hasNext().shouldBe(true)
  acc.toList().shouldBe(listOf(-1))
  s.iterator().next()
  acc.toList().shouldBe(listOf(-1,0))
  s.iterator().next()
  acc.toList().shouldBe(listOf(-1,0,0))
  s.iterator().next()
  acc.toList().shouldBe(listOf(-1,0,0,0))
  s.iterator().hasNext().shouldBe(true)
}

/*
fun obtain() {
  val source: List<Int> = listOf(1,2,3,4,5)
  val acc: MutableList<Int> = mutableListOf(-1)

  val s = myEmitter<Int>(source) {
    while (true) {
      val it = obtain2()
      if (it >= 4) break
      acc.add(it * 10)
    }
  }

  s.iterator().hasNext().shouldBe(true)
  acc.toList().shouldBe(listOf(-1))
  s.iterator().next()
  acc.toList().shouldBe(listOf(-1,10))
  s.iterator().next()
  acc.toList().shouldBe(listOf(-1,10,20))
  s.iterator().next()
  acc.toList().shouldBe(listOf(-1,10,20,30))
  s.iterator().hasNext().shouldBe(false)
}

*/
