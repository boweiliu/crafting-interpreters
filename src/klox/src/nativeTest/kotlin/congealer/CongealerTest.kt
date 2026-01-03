import kotlin.test.*
import lexing.*
import congealing.*

import io.kotest.matchers.*
import io.kotest.matchers.collections.*


fun CongealedToken.Companion.TT(s: String) = CongealedToken.ParsingToken(s)

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

class ComputeActionDatasTest {
  @Test
  fun itJustRuns() {
    computeActionDatas(CState.Ss("ROOT"), Token.TT(TokenType.NUMBER))
  }

  @Test
  fun itReturnsModificationsForROOT() {
    val result = computeActionDatas(CState.Ss("ROOT"), Token.TT(TokenType.NUMBER))
    result.stuff.shouldHaveSize(1)
    result.stuff.map { it.ty }.shouldBe(listOf("Re"))
    result.stuff[0].let { it as CDatum.Re }.re.todos.let { todos -> 
      todos.shouldHaveSize(2)
      todos.map { it.s }.shouldBe(listOf("EXPR", "ROOT_CLOSE"))
    }
  }

  fun simulate(
    toks: List<Token>,
    stateStack: ArrayDeque<CState> = ArrayDeque(listOf(CState.Ss("ROOT"))),
  ): List<Any?> {
    val tokens: MutableList<Token> = toks.toMutableList()
    var curr: Token? = tokens.removeFirstOrNull()
    var acc: MutableList<Any?> = mutableListOf()
    while(true) {
      val peekState = stateStack.lastOrNull() ?: break
      if (curr == null) break
      val (actions, ) = computeActionDatas(peekState, curr)
      actions.forEach {
        when(it) {
          is CDatum.Re -> {
            stateStack.pop()
            it.re.todos.reversed().forEach { stateStack.push(it) }
          }
          is CDatum.CChomp -> {
            acc.add(curr)
            curr = tokens.removeFirstOrNull()
          }
          is CDatum.Em -> acc.add(it.em.cToken)
          is CDatum.Er -> throw RuntimeException("$it")
          else -> { }
        }
      }
    }
    return acc.toList()
  }

  @Test
  @Ignore
  fun itErrorsForEOF() {
    val results = simulate(Token.TTL(TokenType.EOF))
    results.shouldHaveSize(2)
  }

  @Test
  fun itSimulatesForLiteral() {
    val results = simulate(Token.TTL(TokenType.NUMBER, TokenType.EOF))
    results.shouldHaveSize(3)
    results.shouldBe(listOf(
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      Token.TT(TokenType.EOF)
    ))
  }

  @Test
  fun itSimulatesForUnary() {
    val results = simulate(Token.TTL(TokenType.MINUS, TokenType.NUMBER, TokenType.EOF))
    results.shouldHaveSize(5)
    results.shouldBe(listOf(
      Token.TT(TokenType.MINUS),
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      CongealedToken.TT("UNARY_2"),
      Token.TT(TokenType.EOF)
    ))
  }

  @Test
  fun itSimulatesForPlus() {
    val results = simulate(Token.TTL(TokenType.NUMBER, TokenType.PLUS, TokenType.NUMBER, TokenType.EOF))
    results.shouldHaveSize(7)
    results.shouldBe(listOf(
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      Token.TT(TokenType.PLUS),
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      CongealedToken.TT("ADD_3"),
      Token.TT(TokenType.EOF)
    ))
  }

  @Test
  fun itSimulatesForReallyComplicatedThing() {
    val results = simulate(Token.TTL(
      TokenType.NUMBER,
      TokenType.PLUS,
      TokenType.NUMBER,
      TokenType.STAR,
      TokenType.LEFT_PAREN,
      TokenType.NUMBER,
      TokenType.PLUS,
      TokenType.MINUS,
      TokenType.NUMBER,
      TokenType.RIGHT_PAREN,
      TokenType.PLUS,
      TokenType.NUMBER,
      TokenType.EOF,
    ))
    results.shouldBe(listOf(
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      Token.TT(TokenType.PLUS),
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      Token.TT(TokenType.STAR),
      Token.TT(TokenType.LEFT_PAREN),
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      Token.TT(TokenType.PLUS),
      Token.TT(TokenType.MINUS),
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      CongealedToken.TT("UNARY_2"),
      CongealedToken.TT("ADD_3"),
      Token.TT(TokenType.RIGHT_PAREN),
      CongealedToken.TT("GROUP_3"),
      CongealedToken.TT("MULT_3"),
      CongealedToken.TT("ADD_3"), // hmm, should we expect the plus or the add_3 first?
      Token.TT(TokenType.PLUS), // ans: add_3 since that otherwise the plus would be wrong group
      Token.TT(TokenType.NUMBER), CongealedToken.TT("LITERAL_1"),
      CongealedToken.TT("ADD_3"),
      Token.TT(TokenType.EOF),
    ))
  }
}
  
