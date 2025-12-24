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
    val tokens = run("", "<stdin>")
    tokens.size.shouldBe(0)
  }
}
