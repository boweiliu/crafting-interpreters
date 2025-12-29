import kotlin.test.*
import lexing.*
import congealing.*

import io.kotest.matchers.*
import io.kotest.matchers.collections.*

fun Token.Companion.TT(type: TokenType) =
  Token(type, lexeme = "", literal = null, lineNo = 0, fileName = "")

fun Token.Companion.TTL(vararg args: TokenType) =
  args.map { arg -> Token.TT(arg) }.toList()

@Test
fun itJustRuns() {
  val inputTokens = Token.TTL()
  runCongealer(inputTokens.asSequence())
}

@Test
fun itWorksOnNumbers() {
  val inputTokens = Token.TTL(TokenType.NUMBER)
}

@Test
fun itWorksOnPlus() {
  val inputTokens = Token.TTL(TokenType.NUMBER, TokenType.PLUS, TokenType.NUMBER)
}

@Test
fun itHandlesParens() {
  val inputTokens = Token.TTL(TokenType.LEFT_PAREN, TokenType.NUMBER, TokenType.RIGHT_PAREN)
}

@Test
fun itHandlesNegate() {
  val inputTokens = Token.TTL(TokenType.LEFT_PAREN, TokenType.NUMBER, TokenType.RIGHT_PAREN)
}
