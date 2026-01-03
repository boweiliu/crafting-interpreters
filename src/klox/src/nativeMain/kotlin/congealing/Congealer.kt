package congealing

import lexing.*
import sequence.*

sealed interface CongealedToken {
  data class RawToken(val tt: Token): CongealedToken
  data class ParsingToken(val ss: String): CongealedToken
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

fun ArrayDeque<CState>.push(cs: CState) = this.add(cs)
fun ArrayDeque<CState>.pop() = this.removeLast()

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
	      shouldChomp = true // delay this, need to process other actions first
            }
	    is CDatum.Em -> {
	      results.add(it.em.cToken)
	    }
	    is CDatum.Er -> {
	      TODO("give more info about the error")
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
  constructor(s: String) : this(CongealedToken.ParsingToken(s))
}
// Error while parsing
data class CError(val state: CState, val curr: Token, val expectedToken: TokenType? = null)
// Data while parsing to help with error messaging ability
data class CMatchFail(val tokenType: TokenType)

// When we CChomp a token, it's not always true we need another token immediately
fun CState.doesNeedToken(): Boolean = !(this.s.endsWith("_END"))

// TODO: it looks like this could be replaced with a big lookup dictionary
// TODO: autogenerate this based on EBNF
fun computeActionDatas(statePeek: CState, curr: Token, statePeek2: CState? = null): CDatas {
  return when (statePeek.s) {
    "ROOT" -> {
      CDatas.of(CStackReplace("EXPR", "ROOT_CLOSE"))
    }
    "ROOT_CLOSE" -> {
      if (curr.type == TokenType.EOF) {
        CDatas.of(CStackReplace("ROOT_END"), CChomp())
      } else {
        CDatas.of(CError(statePeek, curr, TokenType.EOF))
      }
    }
    "ROOT_END" -> {
      CDatas.of(CStackPop(), CEmit("ROOT_1"))
    }
    "EXPR" -> {
      CDatas.of(CStackReplace("ADD"))
    }
    "ADD" -> {
      CDatas.of(CStackReplace("MULT", "ADD_MORE"))
    }
    "ADD_MORE" -> {
      if (curr.type == TokenType.PLUS) {
        CDatas.of(CStackReplace("MULT", "ADD_END", "ADD_MORE"), CChomp())
      } else {
        CDatas.of(CStackPop())
      }
    }
    "ADD_END" -> {
      CDatas.of(CStackPop(), CEmit("ADD_3"))
    }
    "MULT" -> {
      CDatas.of(CStackReplace("UNARY", "MULT_MORE"))
    }
    "MULT_MORE" -> {
      if (curr.type == TokenType.STAR) {
        CDatas.of(CStackReplace("UNARY", "MULT_END", "MULT_MORE"), CChomp())
      } else {
        CDatas.of(CStackPop(), CMatchFail(TokenType.STAR))
      }
    }
    "MULT_END" -> {
      CDatas.of(CStackPop(), CEmit("MULT_3"))
    }
    "UNARY" -> {
      if (curr.type == TokenType.MINUS) {
        CDatas.of(CStackReplace("UNARY", "UNARY_END"), CChomp())
      } else {
        CDatas.of(CStackReplace("GROUP"), CMatchFail(TokenType.MINUS))
      }
    }
    "UNARY_END" -> {
      CDatas.of(CStackPop(), CEmit("UNARY_2"))
    }
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
        CDatas.of(CError(statePeek, curr, TokenType.RIGHT_PAREN))
      }
    }
    "GROUP_END" -> {
      CDatas.of(CStackPop(), CEmit("GROUP_3"))
    }
    "LITERAL" -> {
      if (curr.type == TokenType.NUMBER) {
        CDatas.of(CStackReplace("LITERAL_END"), CChomp())
      } else {
        CDatas.of(CError(statePeek, curr, TokenType.NUMBER))
      }
    }
    "LITERAL_END" -> {
      CDatas.of(CStackPop(), CEmit("LITERAL_1"))
    }
    else -> {
      CDatas.of(CError(statePeek, curr))
    }
  }
}
