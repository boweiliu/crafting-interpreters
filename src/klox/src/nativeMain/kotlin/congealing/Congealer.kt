package congealing

import lexing.*
import sequence.*

sealed interface CongealedToken {
  data class RawToken(val tt: Token): CongealedToken
  data class ParsingToken(val ss: String, val arity: Int = 0 /* TODO rm default */): CongealedToken
  companion object { }
}

fun <T> Sequence<T>.dropLast(): Sequence<T> {
  val oldSeq = this.iterator()
  val newSeq = sequence<T> {
    while (oldSeq.hasNext()) {
      val curr = oldSeq.next()
      if (oldSeq.hasNext()) yield(curr)
    }
  }
  return newSeq
}

fun <T> ArrayDeque<T>.push(cs: T) = this.add(cs)
fun <T> ArrayDeque<T>.pop() = this.removeLast()
fun <T> ArrayDeque<T>.pop(n: Int) = (0..<n).map { this.pop() }.reversed()

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<CongealedToken>, List<InterpreterError>> {

  val outputTokens: Sequence<List<CongealedToken>> = inputTokens.peekAhead3()
    .mapDuoSequence {
      val stateStack: ArrayDeque<CState> = ArrayDeque(listOf(CState.Ss("ROOT")))

      var results: MutableList<CongealedToken> = mutableListOf()

      var (curr, nxt1, nxt2) = initCoYield()

      // When we should grab the next input token.
      // Necessary to hold this because we dont always need a fresh token, and when we do,
      // we would like to emit as much data as possible before requesting another
      var shouldChomp: Boolean = false

      // Loop over processing actions on the stateStack
      while (true) {
        val peekState = stateStack.lastOrNull() ?: break

        if (peekState.doesNeedToken() && shouldChomp) {
          duoYield(results).let { (a,b,c) ->
            curr = a; nxt1 = b; nxt2 = c;
          }.also {
            results = mutableListOf()
            shouldChomp = false
          }
        }

        if (curr == null) break

        val (actions, ) = computeActionDatas(peekState, curr)
        actions.forEach {
          when(it) {
	    is CDatum.Re -> {
	      stateStack.pop()
	      it.re.todos.reversed().forEach { 
	        stateStack.push(it)
	      }
	    }
	    is CDatum.CChomp -> {
	      results.add(CongealedToken.RawToken(curr))
	      shouldChomp = true // delay this, need to process other actions first
            }
	    is CDatum.Em -> {
	      results.add(it.em.cToken)
	    }
	    is CDatum.Er -> {
	      TODO("give more info about the error $it")
            }
	    is CDatum.Mf -> {
	      // ignore for now, only useful when errors show up
            }
          }
        }
      }
      results
    }
  return Pair(
    outputTokens.map { it.asSequence() }.flatten(),
    errsAcc.toList<InterpreterError>()
  )
}

sealed class CState(val s: String) {
  data class Ss(val ss: String): CState(ss)
}

sealed class CDatum(val ty: String) {
  data class Re(val re: CStackReplace): CDatum("Re")
  object CChomp: CDatum("Ch")
  data class Em(val em: CEmit): CDatum("Em")
  data class Er(val er: CError): CDatum("Er")
  data class Mf(val mf: CMatchFail): CDatum("Mf")
}

data class CDatas(val stuff: List<CDatum>) {
  companion object {
    fun of(vararg args: Any) = args.mapNotNull { it ->
      when(it) {
        is CStackReplace   -> CDatum.Re(it)
        is CDatum.CChomp   -> it
        is CEmit           -> CDatum.Em(it)
        is CError          -> CDatum.Er(it)
        is CMatchFail      -> CDatum.Mf(it)
        else -> null
      }
    }.let { CDatas(it) }
  }
}

// the todos are in the order the human thinks; they must be reversed for the work stack
data class CStackReplace(val todos: List<CState>) { 
  constructor(vararg args: String) : this(args.map { it -> CState.Ss(it) })
}
fun CStackPop() = CStackReplace()
// Consume a token
fun CChomp() = CDatum.CChomp
// Emit a group indicator
data class CEmit(val cToken: CongealedToken) {
  constructor(s: String, n: Int) : this(CongealedToken.ParsingToken(s, n))
}
// Error while parsing
data class CError(val state: CState, val curr: Token, val expectedToken: Set<TokenType> = setOf())
// Data while parsing to help with error messaging ability
data class CMatchFail(val expectedToken: Set<TokenType> = setOf()) {
  constructor(expectedToken: TokenType) : this(setOf(expectedToken))
}

// When we CChomp a token, it's not always true we need another token immediately
fun CState.doesNeedToken(): Boolean = !(this.s.endsWith("_END"))

// TODO: it looks like this could be replaced with a big lookup dictionary
// TODO: autogenerate this based on EBNF
fun computeActionDatas(statePeek: CState, curr: Token, statePeek2: CState? = null): CDatas {

  val tokenTypeMatchSet = Token.PRECEDENCE_SET[statePeek.s]

  return when (statePeek.s) {
    // ROOT : ROOT_CLOSE | ROOT_BODY
    // (either we expect an immediate EOF or something else)
    "ROOT" -> {
      CDatas.of(CStackReplace("ROOT_CLOSE"))
    }
    "ROOT_CLOSE" -> {
      if (curr.type == TokenType.EOF) {
        CDatas.of(CStackReplace("ROOT_END"), CChomp())
      } else {
        CDatas.of(CStackReplace("ROOT_BODY"), CMatchFail(TokenType.EOF))
      }
    }
    "ROOT_END" -> {
      CDatas.of(CStackPop(), CEmit("ROOT", 1))
    }
    // ROOT_BODY : EXPR ROOT_BODY_CLOSE
    // we expect an EXPR followed by an EOF
    "ROOT_BODY" -> {
      CDatas.of(CStackReplace("EXPR", "ROOT_BODY_CLOSE"))
    }
    "ROOT_BODY_CLOSE" -> {
      if (curr.type == TokenType.EOF) {
        CDatas.of(CStackReplace("ROOT_BODY_END"), CChomp())
      } else {
        CDatas.of(CError(statePeek, curr, setOf(TokenType.EOF)))
      }
    }
    "ROOT_BODY_END" -> {
      CDatas.of(CStackPop(), CEmit("ROOT_BODY", 2))
    }
    "EXPR" -> {
      CDatas.of(CStackReplace("ADD"))
    }
    // ADD : MULT ADD_MORE
    // we expect a higher-precedence term followed by left-associating additions
    "ADD" -> {
      CDatas.of(CStackReplace("MULT", "ADD_MORE"))
    }
    "ADD_MORE" -> {
      if (curr.type in tokenTypeMatchSet!!) {
        CDatas.of(CStackReplace("MULT", "ADD_END", "ADD_MORE"), CChomp())
      } else {
        CDatas.of(CStackPop(), CMatchFail(tokenTypeMatchSet))
      }
    }
    "ADD_END" -> {
      CDatas.of(CStackPop(), CEmit("ADD", 3))
    }
    // MULT : UNARY MULT_MORE
    // we expect a higher-precedence term followed by left-associating additions
    "MULT" -> {
      CDatas.of(CStackReplace("UNARY", "MULT_MORE"))
    }
    "MULT_MORE" -> {
      if (curr.type in tokenTypeMatchSet!!) {
        CDatas.of(CStackReplace("UNARY", "MULT_END", "MULT_MORE"), CChomp())
      } else {
        CDatas.of(CStackPop(), CMatchFail(tokenTypeMatchSet))
      }
    }
    "MULT_END" -> {
      CDatas.of(CStackPop(), CEmit("MULT", 3))
    }
    // UNARY : UNARY GROUP | GROUP
    // either we have one or more symbols, or a higher-precedence term
    "UNARY" -> {
      if (curr.type in tokenTypeMatchSet!!) {
        CDatas.of(CStackReplace("UNARY", "UNARY_END"), CChomp())
      } else {
        CDatas.of(CStackReplace("GROUP"), CMatchFail(tokenTypeMatchSet))
      }
    }
    "UNARY_END" -> {
      CDatas.of(CStackPop(), CEmit("UNARY", 2))
    }
    // GROUP : "(" EXPR GROUP_CLOSE | LITERAL
    // either we have paired symbols, or a higher-precedence term
    "GROUP" -> {
      if (curr.type == TokenType.LEFT_PAREN) {
        CDatas.of(CStackReplace("EXPR", "GROUP_CLOSE"), CChomp())
      } else {
        CDatas.of(CStackReplace("LITERAL"), CMatchFail(TokenType.LEFT_PAREN))
      }
    }
    "GROUP_CLOSE" -> {
      if (curr.type == TokenType.RIGHT_PAREN) {
        CDatas.of(CStackReplace("GROUP_END"), CChomp())
      } else {
        CDatas.of(CError(statePeek, curr, setOf(TokenType.RIGHT_PAREN)))
      }
    }
    "GROUP_END" -> {
      CDatas.of(CStackPop(), CEmit("GROUP", 3))
    }
    // LITERAL is a leaf rule
    "LITERAL" -> {
      if (curr.type in tokenTypeMatchSet!!) {
        CDatas.of(CStackReplace("LITERAL_END"), CChomp())
      } else {
        CDatas.of(CError(statePeek, curr, tokenTypeMatchSet))
      }
    }
    "LITERAL_END" -> {
      CDatas.of(CStackPop(), CEmit("LITERAL", 1))
    }
    else -> {
      CDatas.of(CError(statePeek, curr))
    }
  }
}

val Token.Companion.PRECEDENCE_SET: Map<String, Set<TokenType>> get() = mapOf(
  "LITERAL" to TokenTypeSet(
    TokenType.NUMBER, TokenType.TRUE, TokenType.FALSE, TokenType.STRING, TokenType.NIL),
  "MULT_MORE" to TokenTypeSet(
    TokenType.STAR, TokenType.SLASH, TokenType.PERCENT),
  "ADD_MORE" to TokenTypeSet(
    TokenType.PLUS, TokenType.MINUS),
  "UNARY" to TokenTypeSet(
    TokenType.MINUS, TokenType.BANG, TokenType.NOT),
)
