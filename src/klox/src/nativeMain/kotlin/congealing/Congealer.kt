package congealing

import lexing.*
import sequence.*

data class CongealedToken(val t : Any?)

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

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<CongealedToken>, List<InterpreterError>> {

  var myState: Any? = null

  @Suppress("UNCHECKED_CAST")
  // val input_seq = (inputTokens.peekAhead3().dropLast() as Sequence<Triple<Token, Token?, Token?>>)

  val outputTokens: Sequence<Sequence<CongealedToken>> = inputTokens.peekAhead3()
    .mapDuoSequence {
      val stateStack: ArrayDeque<CState> = ArrayDeque(listOf(CState.Ss("ROOT")))
      var results: MutableList<CongealedToken>? = null

      while (true) {
        var (curr, nxt1, nxt2) = results ?.let { duoYield(it.asSequence()) }
          ?: initCoYield().also { results = mutableListOf() }
        if (curr == null) break

        val (actions, ) = computeActionDatas(stateStack.lastOrNull()!!, curr)
        actions.forEach {
          when(it) {
	    is CDatum.Em -> {
            }
	    else -> { }
          }
        }
      }
      results!!.asSequence()
    }
  return Pair(outputTokens.flatten(), errsAcc.toList<InterpreterError>())
}

sealed class CState(val s: String) {
  data class Ss(val ss: String): CState(ss)
  // data class Sn(val ss: String, val n: Int): CState(ss)
}

sealed class CDatum(val ty: String) {
  data class Re(val re: CStackReplace): CDatum("Re")
  // data class R2(val r2: CStackReplace2): CDatum("R2")
  data class Ad(val ad: CStackAdd): CDatum("Ad")
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
        // is CStackReplace2  -> CDatum.R2(it)
        is CStackAdd       -> CDatum.Ad(it)
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
// data class CStackReplace2(val todos: List<CState>) {
//   constructor(vararg args: String) : this(args.map { it -> CState.Ss(it) })
// }
fun CStackPop() = CStackReplace()
data class CStackAdd(val todos: List<CState>) { 
  constructor(vararg args: String) : this(args.map { it -> CState.Ss(it) })
}
fun CChomp() = CDatum.CChomp
data class CEmit(val c: CongealedToken) {
  constructor(s: String) : this(CongealedToken(s))
}
data class CError(val state: CState, val curr: Token, val expectedToken: TokenType? = null)
data class CMatchFail(val tokenType: TokenType)

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
      // if (statePeek2?.s == "ADD_END")
      //   CDatas.of(CStackReplace2("MULT", "ADD_END", "ADD_MORE"))
      // else
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


      // // hmm. try to compute the state transitions.
      // // val (datas, ) = computeCongealerActionDatas(old = myState, curr, nxt1, nxt2, this.coYield)
      // var actionDatas: Any? = mutableListOf<Any?>()
      // var stateStack: Any? = mutableListOf<String>("EXPECT_TERM")

      // var (curr, nxt1, nxt2)  = actionDatas ?.let { duoYield(it) }
      //   ?: initCoYield().also { actionDatas = mutableListOf<Any?>() }

      // actionDatas.add(CToken(curr))

      // if (stateStack.last() == "EXPECT_TERM") {
      //   if (curr.type == TokenType.LEFT_PAREN) {
      //     actionDatas.add(CStackPush("PAREN"))
      //     actionDatas.add(CStackPush("EXPECT_TERM"))

      //   } else if (curr.type == TokenType.NUMBER) {
      //     // TODO: add more ops here
      //     actionDatas.add(CStackPop())
      //   }
      // } else if (stateStack.last() == "PAREN") {
      //   if (curr.type == TokenType.RIGHT_PAREN) {
      //     actionDatas.add(CStackPop())
      //   } else {
      //     actionDatas.add(CError())
      //   }
      // }
