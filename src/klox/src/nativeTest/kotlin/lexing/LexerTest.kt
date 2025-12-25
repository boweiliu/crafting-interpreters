import kotlin.test.*
import lexing.*

import io.kotest.matchers.*
import io.kotest.matchers.collections.*


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
    val (tokens, errs) = run("", "<stdin>")
    tokens.size.shouldBe(1)
    tokens.shouldBe(listOf(Token(TokenType.EOF, "", null, 2, "<stdin>")))
  }

  @Test
  fun itLexesParens() {
    val (tokens, errs) = run("()", "<stdin>")
    tokens.size.shouldBe(3)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.LEFT_PAREN,
      TokenType.RIGHT_PAREN,
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesErrors() {
    val (tokens, errs) = run("^", "<stdin>")
    errs.shouldHaveSize(1)
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
