package congealing

import lexing.*
import sequence.*

data class CongealedToken(val t : Any?)

fun <T> Sequence<T>.peekAhead3(): Sequence<Triple<T?, T?, T?>> = TODO()
fun <T> Sequence<T>.dropLast(): Sequence<T> = TODO()

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<CongealedToken>, List<InterpreterError>> {

  var myState: Any? = null

  @Suppress("UNCHECKED_CAST")
  val xx = inputTokens.peekAhead3().dropLast() as Sequence<Triple<Token, Token?, Token?>>
  val outputTokens: Sequence<CongealedToken> = xx
    .mapDuoSequence<Triple<Token, Token?, Token?>, CongealedToken> {
      val stateStack: MutableList<String> = mutableListOf("ROOT")
      var buffer: MutableList<String> = mutableListOf()
      var result: CongealedToken? = null

      var tokenTriple: Triple<Token, Token?, Token?> = result ?.let { duoYield(it) }
        ?: initCoYield()
      var curr: Token = tokenTriple.first

      stateStack.add("ADD")
      while (true) {
        val topState = stateStack.lastOrNull()!!
        when (topState) {
          "ROOT" -> {
            CStackReplace("EXPR", "ROOT_END")
          }
          "EXPR" -> {
            CStackReplace("ADD")
          }
          "ADD" -> {
            CStackReplace("MULT, ADD_MORE")
          }
          "ADD_MORE" -> {
            if (curr.type == TokenType.PLUS) {
              CStackReplace("ADD_END", "ADD")
            } else {
              CStackPop()
            }
          }
          "MULT" -> {
            CStackReplace("UNARY", "MULT_MORE")
          }
          "MULT_MORE" -> {
            if (curr.type == TokenType.STAR) {
              CStackReplace("MULT")
            } else {
              CStackPop()
            }
          }
          "UNARY" -> {
            if (curr.type == TokenType.MINUS) {
              CStackAdd("UNARY")
            } else {
              CStackReplace("GROUP")
            }
          }
          "GROUP" -> {
            if (curr.type == TokenType.LEFT_PAREN) {
              CStackReplace("EXPR", "GROUP_END")
            } else {
              CStackReplace("LITERAL")
            }
          }
          "GROUP_END" -> {
            if (curr.type == TokenType.RIGHT_PAREN) {
              CStackPop()
            } else {
              TODO("error")
            }
          }
          "LITERAL" -> {
            if (curr.type == TokenType.NUMBER) {
              CStackPop()
              TODO("just added number")
            } else {
              TODO("error")
            }
          }
        }
      }

      result!!
    }

  return Pair(outputTokens, errsAcc.toList<InterpreterError>())
}

sealed class CState {
  data class Ss(val s: String): CState()
  data class Sn(val s: String, val n: Int): CState()
}

sealed class CDatum(val ty: String) {
  // data class Tr(val tr: CTransition): CDatum("Tr")
  data class Re(val re: CStackReplace): CDatum("Re")
  data class R2(val r2: CStackReplace2): CDatum("R2")
}

data class CDatas(val stuff: List<CDatum>) {
  companion object { }
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
  TODO()
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
