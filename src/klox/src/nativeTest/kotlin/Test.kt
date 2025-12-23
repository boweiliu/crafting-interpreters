import kotlin.test.Test
import kotlin.test.assertEquals

@Test
fun test1() {
  println("test")
  assertEquals(1, 1, "good this should pass")
  assertEquals(1, 9, "this should fail here")
}


@Test
fun test2() {
  println("test")
  assertEquals(1, 1, "good this should pass")
}
