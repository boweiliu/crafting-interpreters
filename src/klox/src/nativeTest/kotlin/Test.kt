import kotlin.test.Test
import kotlin.test.assertEquals
import io.kotest.matchers.*
import io.kotest.matchers.collections.*

@Test
fun test1() {
  println("test")
  assertEquals(1, 1, "good this should pass")
  // assertEquals(1, 9, "this should fail here")
}


@Test
fun test2() {
  println("test")
  assertEquals(1, 1, "good this should pass")
}

@Test
fun testRunLexerJustRuns() {
  val (tokenSeq, errorsSoFar) = runLexer("1 + 2", "<stdin>")
  errorsSoFar.shouldHaveSize(0)
}


@Test
fun testRunLexerRunsLazily() {
  val (tokenSeq, errorsSoFar) = runLexer("1 + 2\"", "<stdin>")
  errorsSoFar.shouldHaveSize(0)
  tokenSeq.toList()
  errorsSoFar.shouldHaveSize(2)
}


