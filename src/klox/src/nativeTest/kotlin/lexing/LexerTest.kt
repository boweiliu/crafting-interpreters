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
    tokens.shouldHaveSize(3)
    errs.shouldHaveSize(0)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.LEFT_PAREN,
      TokenType.RIGHT_PAREN,
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesDoubleEqual() {
    val (tokens, errs) = run("==", "<stdin>")
    tokens.shouldHaveSize(2)
    errs.shouldHaveSize(0)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.EQUAL_EQUAL,
      TokenType.EOF
    ))
  }

  @Test
  fun itErrorsUnmatchedString() {
    val (tokens, errs) = run("\"", "<stdin>")
    errs.shouldHaveSize(2)
    tokens.shouldHaveSize(1)
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
  fun itLexesWhitespace() {
    val (tokens, errs) = run("  \t    \n    ", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(1)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesBackToBackStrings() {
    val (tokens, errs) = run(" \"a\"\"b\"     \"c\"  ", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(4)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.STRING,
      TokenType.STRING,
      TokenType.STRING,
      TokenType.EOF,
    ))
  }

  @Test
  fun itLexesVariousSymbols() {
    val sourceString = """
    ( ) { } , ; + - *
//  1 2 3 4 5 6 7 8 9 10=thisline
    . / % ! = < >
//  1 2 3 4 5 6 7 18=thisline
    != == <= >=    // trailing whitespace
//  19 20 21 22    23    24=thisline 25=eof
"""
    val (tokens, errs) = run(sourceString, "<stdin>")
    errs.shouldHaveSize(0)
    // 10 per assertion so we can easily spot which is missing
    tokens.map { it.type }.take(10).shouldHaveSize(10)
    tokens.map { it.type }.drop(10).take(10).shouldHaveSize(10)
    tokens.map { it.type }.drop(20).take(10).shouldHaveSize(5)
    tokens.map { it.type }[0].shouldBe(TokenType.LEFT_PAREN)
    tokens.map { it.type }[18].shouldBe(TokenType.BANG_EQUAL)
  }

  @Test
  fun itLexesDigits() {
    val (tokens, errs) = run("1", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(2)
  }

  @Test
  fun itLexesNumbers() {
    val (tokens, errs) = run("123 4.5 -6*7// hello", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(8)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.NUMBER,
      TokenType.NUMBER,
      TokenType.MINUS,
      TokenType.NUMBER,
      TokenType.STAR,
      TokenType.NUMBER,
      TokenType.COMMENT,
      TokenType.EOF
    ))
    tokens.take(2).map { it.literal!!.vl }.shouldBe(listOf<Any>(123, 4.5))
  }

  @Test
  fun itErrorsInvalidNumber1() {
    val (tokens, errs) = run("1.1..1", "<stdin>")
    errs.shouldHaveSize(2) // one for each extra dot
    tokens.shouldHaveSize(1)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.EOF
    ))
  }

  @Test
  fun itParsesBigIntegerAsFloat() {
    val (tokens, errs) = run("666666666666666666666666666666666666666666666666666666666666666666", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(2)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.NUMBER,
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesSomethingDotZero() {
    val (tokens, errs) = run("\"soo\".0", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(4)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.STRING,
      TokenType.DOT,
      TokenType.NUMBER,
      TokenType.EOF,
    ))
  }


  @Test
  fun itErrorsInvalidNumbers() {
    val (tokens, errs) = run("11.x * 2.2f 3_3 4d4 5./ 6.", "<stdin>")
    errs.shouldHaveSize(6)
    tokens.shouldHaveSize(3)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.STAR,
      TokenType.SLASH,
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesAKeyword() {
    val (tokens, errs) = run("and", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(2)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.AND,
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesCaseInsensitiveKeyword() {
    val (tokens, errs) = run("and OR nOt", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(4)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.AND,
      TokenType.OR,
      TokenType.NOT,
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesVariables() {
    val (tokens, errs) = run("x _.y_/z2", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(7)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.IDENTIFIER,
      TokenType.IDENTIFIER,
      TokenType.DOT,
      TokenType.IDENTIFIER,
      TokenType.SLASH,
      TokenType.IDENTIFIER,
      TokenType.EOF
    ))
  }

  @Test
  fun itLexesMaximally() {
    val (tokens, errs) = run("andy likes orchids", "<stdin>")
    errs.shouldHaveSize(0)
    tokens.shouldHaveSize(4)
    tokens.map { it.type }.shouldBe(listOf(
      TokenType.IDENTIFIER,
      TokenType.IDENTIFIER,
      TokenType.IDENTIFIER,
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
