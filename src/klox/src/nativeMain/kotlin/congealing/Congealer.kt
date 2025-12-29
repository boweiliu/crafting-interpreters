package congealing

import lexing.*

fun runCongealer(
  inputTokens: Sequence<Token>,
  sourceFname: String = "<unnamed>",
  errsAcc: MutableList<InterpreterError> = mutableListOf()
): Pair<Sequence<Token>, List<InterpreterError>> {


  var myState: Any? = null

  val outputTokens = sequence<Token> {
    inputTokens.peekAhead3<Token>().forEach { (curr, nxt1, nxt2) ->
      if (curr == null) return@forEach

      // val (datas, ) = computeCongealerActionDatas(old = myState, curr, nxt1, nxt2)
      
      yield(curr)
    }
  }
  return Pair(outputTokens, errsAcc.toList<InterpreterError>())
}

