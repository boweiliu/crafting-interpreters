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
  val input_seq = (inputTokens.peekAhead3().dropLast() as Sequence<Triple<Token, Token?, Token?>>)

  val outputTokens: Sequence<CongealedToken> = input_seq
    .mapDuoSequence {
      val stateStack: MutableList<String> = mutableListOf("ROOT")
      var buffer: MutableList<String> = mutableListOf()
      var result: CongealedToken? = null

      var (curr, nxt1, nxt2) = result ?.let { duoYield(it) }
        ?: initCoYield()

      stateStack.add("ADD")
      while (false) {
        val topState = stateStack.lastOrNull()!!
      }
      result!!
    }
  return Pair(outputTokens, errsAcc.toList<InterpreterError>())
}

sealed class CState(val s: String) {
  data class Ss(val ss: String): CState(ss)
  data class Sn(val ss: String, val n: Int): CState(ss)
}

sealed class CDatum(val ty: String) {
  // data class Tr(val tr: CTransition): CDatum("Tr")
  data class Re(val re: CStackReplace): CDatum("Re")
  data class R2(val r2: CStackReplace2): CDatum("R2")
}

data class CDatas(val stuff: List<CDatum>) {
  companion object {
    fun of(vararg args: Any) = args.mapNotNull { it ->
      when(it) {
        is CStackReplace  -> CDatum.Re(it)
        is CStackReplace2 -> CDatum.R2(it)
        else -> null
      }
    }.let { CDatas(it) }
  }
}

data class CStackReplace(val todos: List<CState>) { 
  constructor(vararg args: String) : this(args.map { it -> CState.Ss(it) })
}
data class CStackReplace2(val todos: List<CState>)
fun CStackPop() = CStackReplace()
data class CStackAdd(val todos: List<CState>) { 
  constructor(vararg args: String) : this(args.map { it -> CState.Ss(it) })
}
  
fun computeActionDatas(statePeek: CState, statePeek2: CState?, curr: Token, nxt1: Token?): CDatas {
  return when (statePeek.s) {
    "ROOT" -> {
      CDatas.of(CStackReplace("EXPR", "ROOT_END"))
    }
    "EXPR" -> {
      CDatas.of(CStackReplace("ADD"))
    }
    "ADD" -> {
      CDatas.of(CStackReplace("MULT, ADD_MORE"))
    }
    "ADD_MORE" -> {
      if (curr.type == TokenType.PLUS) {
        CDatas.of(CStackReplace("ADD_END", "ADD"))
      } else {
        CDatas.of(CStackPop())
      }
    }
    "MULT" -> {
      CDatas.of(CStackReplace("UNARY", "MULT_MORE"))
    }
    "MULT_MORE" -> {
      if (curr.type == TokenType.STAR) {
        CDatas.of(CStackReplace("MULT"))
      } else {
        CDatas.of(CStackPop())
      }
    }
    "UNARY" -> {
      if (curr.type == TokenType.MINUS) {
        CDatas.of(CStackAdd("UNARY"))
      } else {
        CDatas.of(CStackReplace("GROUP"))
      }
    }
    "GROUP" -> {
      if (curr.type == TokenType.LEFT_PAREN) {
        CDatas.of(CStackReplace("EXPR", "GROUP_END"))
      } else {
        CDatas.of(CStackReplace("LITERAL"))
      }
    }
    "GROUP_END" -> {
      if (curr.type == TokenType.RIGHT_PAREN) {
        CDatas.of(CStackPop())
      } else {
        TODO("error")
      }
    }
    "LITERAL" -> {
      if (curr.type == TokenType.NUMBER) {
        CDatas.of(CStackPop())
        TODO("just added number")
      } else {
        TODO("error")
      }
    }
    else -> TODO("should never happen")
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
