import kotlin.test.*
import lexing.*

import io.kotest.matchers.shouldBe


class LexerTest {
  @Test
  fun itJustRuns() {
    run("1", "source fname")
  }

  @Test
  fun tokensExist() {
    val t = Token(TokenType.EOF, "", null, -1, "<stdin>")
    assertEquals(t.lexeme, "")
  }

  @Test
  @Ignore
  fun itLexesEof() {
    val tokens = run("", "<stdin>")
    tokens.size.shouldBe(0)
  }

  @Test
  fun wrongItLexesErrors() {
    val errs = run("", "<stdin>")
    errs.size.shouldBe(1)
  }
}

class PeekAheadTest {
  @Test
  fun itJustRuns() {
    peekAhead3(listOf("a", "b", "c"))
  }

  @Test
  fun itJustRunsOnEmpty() {
    val result = peekAhead3(listOf<String>())
    result.size.shouldBe(0)
  }

  @Test
  fun itJustRunsOnOne() {
    val result = peekAhead3(listOf<String>("A"))
    result.shouldBe(listOf(Triple("A", null, null)))
  }

  @Test
  fun itJustRunsOnMany() {
    val result = peekAhead3<Char>("ABCD".toCharArray().toList())
    result.shouldBe(listOf(
      Triple('A', 'B', 'C'),
      Triple('B', 'C', 'D'),
      Triple('C', 'D', null),
      Triple('D', null, null),
    ))
  }

  @Test
  @Ignore
  fun itRunsLazily() {
    // TODO
  }
}
