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
    tokens.shouldHaveSize(1)
    errs.shouldHaveSize(0)
    tokens.shouldBe(listOf(Token(TokenType.EOF, "", null, 1, "<stdin>")))
  }

  @Test
  fun itLexesParens() {
    val (tokens, errs) = run("()", "<stdin>")
    tokens.size.shouldBe(3)
    errs.shouldHaveSize(0)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.LEFT_PAREN,
      TokenType.RIGHT_PAREN,
      TokenType.EOF
    ))
  }

  @Test
  fun itErrorsUnmatchedString() {
    val (tokens, errs) = run("\"", "<stdin>")
    tokens.shouldHaveSize(0)
    errs.shouldHaveSize(1)
  }

  @Test
  fun itLexesEmptyString() {
    val (tokens, errs) = run("\"\"", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(2)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.STRING,
      TokenType.EOF
    ))
    tokens[0].type.shouldBe(TokenType.STRING)
    tokens[0].lexeme.shouldBe("\"\"")
    tokens[0].shouldBe(Token(TokenType.STRING, "\"\"", LiteralVal.StringVal(""), 1, "<stdin>"))
  }

  @Test
  fun itLexesShortString() {
    val (tokens, errs) = run("\"a\"", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(2)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.STRING,
      TokenType.EOF
    ))
    tokens[0].type.shouldBe(TokenType.STRING)
    tokens[0].literal!!.vl.shouldBe("a")
    tokens[0].shouldBe(Token(TokenType.STRING, "\"a\"", LiteralVal.StringVal("a"), 1, "<stdin>"))
  }

  @Test
  fun itLexesComment() {
    val (tokens, errs) = run("//x", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(2)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.COMMENT,
      TokenType.EOF
    ))
    tokens[0].type.shouldBe(TokenType.COMMENT)
    tokens[0].shouldBe(Token(TokenType.COMMENT, "//x", null, 1, "<stdin>"))
  }

  @Test
  fun itLexesCommentFollowedByStuff() {
    val (tokens, errs) = run("//x\n\"f\"", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(3)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.COMMENT,
      TokenType.STRING,
      TokenType.EOF
    ))
    tokens[0].shouldBe(Token(TokenType.COMMENT, "//x", null, 1, "<stdin>"))
  }

  @Test
  fun itLexesVariousSymbols() {
    val sourceString = """
    ( ) { } , ; + - *
//  1 2 3 4 5 6 7 8 9
    / % ! = < >
//  1 2 3 4 5 6
    != == <= >=    // trailing whitespace
//  18 19 20 21    22    23=thisline 24=eof
"""
    val (tokens, errs) = run(sourceString, "<stdin>")
    errs.shouldHaveSize(0)
    // 10 per assertion so we can easily spot which is missing
    tokens.map { it.type }.take(10).shouldHaveSize(10)
    tokens.map { it.type }.drop(10).take(10).shouldHaveSize(10)
    tokens.map { it.type }.drop(20).take(10).shouldHaveSize(4)
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
    result.shouldBe(listOf(Triple(null, null, null)))
  }

  @Test
  fun itJustRunsOnOne() {
    val result = listOf<String>("A").peekAhead3()
    result.shouldBe(listOf(Triple("A", null, null), Triple(null, null, null)))
  }

  @Test
  fun itJustRunsOnMany() {
    val result = "ABCD".toCharArray().toList().peekAhead3()
    result.shouldBe(listOf(
      Triple('A', 'B', 'C'),
      Triple('B', 'C', 'D'),
      Triple('C', 'D', null),
      Triple('D', null, null),
      Triple(null, null, null),
    ))
  }

  @Test
  @Ignore
  fun itRunsLazily() {
    // TODO
  }
}
