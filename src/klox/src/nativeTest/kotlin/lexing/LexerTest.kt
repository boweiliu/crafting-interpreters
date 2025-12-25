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
  fun itLexesEof() {
    val (tokens, _) = run("", "<stdin>")
    tokens.size.shouldBe(1)
    tokens.shouldBe(listOf(Token(TokenType.EOF, "", null, 2, "<stdin>")))
  }

  @Test
  fun wrongItLexesErrors() {
    val (_, errs) = run("", "<stdin>")
    errs.size.shouldBe(1)
  }
}


class PeekAheadTest {
  @Test
  fun itJustRuns() {
    listOf("a", "b", "c").peekAhead3()
  }

  @Test
  fun itJustRunsOnEmpty() {
    val result = listOf<String>().peekAhead3()
    result.size.shouldBe(0)
  }

  @Test
  fun itJustRunsOnOne() {
    val result = listOf<String>("A").peekAhead3()
    result.shouldBe(listOf(Triple("A", null, null)))
  }

  @Test
  fun itJustRunsOnMany() {
    val result = "ABCD".toCharArray().toList().peekAhead3()
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
