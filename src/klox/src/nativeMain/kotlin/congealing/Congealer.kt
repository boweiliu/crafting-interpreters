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
      // hmm. try to compute the state transitions.
      // val (datas, ) = computeCongealerActionDatas(old = myState, curr, nxt1, nxt2, this.coYield)
      var actionDatas: Any? = mutableListOf<Any?>()
      var stateStack: Any? = mutableListOf<String>("EXPECT_TERM")

      var (curr, nxt1, nxt2)  = actionDatas ?.let { duoYield(it) }
        ?: initCoYield().also { actionDatas = mutableListOf<Any?>() }

      actionDatas.add(CToken(curr))

      if (stateStack.last() == "EXPECT_TERM") {
        if (curr.type == TokenType.LEFT_PAREN) {
          actionDatas.add(CStackPush("PAREN"))
          actionDatas.add(CStackPush("EXPECT_TERM"))

        } else if (curr.type == TokenType.NUMBER) {
          // TODO: add more ops here
          actionDatas.add(CStackPop())
        }
      } else if (stateStack.last() == "PAREN") {
        if (curr.type == TokenType.RIGHT_PAREN) {
          actionDatas.add(CStackPop())
        } else {
          actionDatas.add(CError())
        }
      }
    }.buildSequenceUsing(inputTokens.peekAhead3().dropLast())

  return Pair(outputTokens, errsAcc.toList<InterpreterError>())
}

