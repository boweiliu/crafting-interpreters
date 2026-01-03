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

class Parens() {
  @Test
  fun itHandlesParensWrappingNumber() {
    val inputTokens = Token.TTL(TokenType.LEFT_PAREN, TokenType.NUMBER, TokenType.RIGHT_PAREN)
  }

  @Test
  fun itMovesState() {
    var stateStack: MutableList<String> = mutableListOf()
    // val result = stateStack.processToken(TokenType.LEFT_PAREN)
    // TODO()
  }
}

@Test
fun itHandlesNegate() {
  val inputTokens = Token.TTL(TokenType.MINUS, TokenType.NUMBER)
}

class UtilsTest() {
  @Test
  fun dropLastWorksOnEmptySeq() {
    val ins = listOf<Int>().asSequence()
    ins.dropLast().toList().shouldBe(listOf())
  }

  @Test
  fun dropLastWorksOnSingletonSeq() {
    val ins = listOf<Int>(1).asSequence()
    ins.dropLast().toList().shouldBe(listOf())
  }

  @Test
  fun dropLastWorksOnTwoThings() {
    val ins = listOf<Int>(1, 2).asSequence()
    ins.dropLast().toList().shouldBe(listOf(1))
  }

  @Test
  fun dropLastWorksLazily() {
    val ins = sequence { while(true) yield(7) }
    ins.dropLast().take(3).toList().shouldBe(listOf(7,7,7))
  }
}
