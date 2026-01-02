package congealing

import lexing.*
import sequence.*

data class CongealedToken(val t : Any?)

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<CongealedToken>, List<InterpreterError>> {

  var myState: Any? = null

  val outputTokens = inputTokens.peekAhead3().dropLast()
    .mapDuoSequence<Token, CongealedToken> {
      val stateStack: MutableList<String> = mutableListOf("ROOT")
      var buffer: MutableList<String> = mutableListOf()
      var results: MutableList<Any?>? = null

      var (curr, nxt1, nxt2)  = results ?.let { duoYield(it) }
        ?: initCoYield().also { results = mutableListOf<Any?>() }

      stateStack.add("ADD")
      while (true) {
        val topState = stateStack.peek()
        return@while when (topState) {
          "ROOT" -> {
            stateStack.add("EXPR")
          }
          "EXPR" -> {
            stateStack.add("ADD")
          }
          "ADD" -> {
            stateStack.addBack("MULT, ADD_MORE")
          }
          "ADD_MORE" -> {
            if (curr.type == TokenType.PLUS) {
              stateStack.replaceWith("ADD")
            } else {
              stateStack.pop()
            }
          }
          "MULT" -> {
            stateStack.replaceWith("UNARY", "MULT_MORE")
          }
          "MULT_MORE" -> {
            if (curr.type == TokenType.STAR) {
              stateStack.replaceWith("MULT")
            } else {
              stateStack.pop()
            }
          }
          "UNARY" -> {
            if (curr.type == TokenType.MINUS) {
              stateStack.add("UNARY")
            } else {
              stateStack.replaceWith("GROUP")
            }
          }
          "GROUP" -> {
            if (curr.type == TokenType.LEFT_PAREN) {
              stateStack.addBack("EXPR", "GROUP_END")
            } else {
              stateStack.replaceWith("LITERAL")
            }
          }
          "GROUP_END" -> {
            if (curr.type == TokenType.RIGHT_PAREN) {
              stateStack.pop()
            } else {
              TODO("error")
            }
          }
          "LITERAL" -> {
            if (curr.type == TokenType.NUMBER) {
              stateStack.pop()
              TODO("just added number")
            } else {
              TODO("error")
            }
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
    }.buildSequenceUsing(inputTokens.peekAhead3().dropLast())

  return Pair(outputTokens, errsAcc.toList<InterpreterError>())
}

