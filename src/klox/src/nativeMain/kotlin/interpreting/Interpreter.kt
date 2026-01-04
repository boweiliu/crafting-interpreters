package interpreting

import lexing.*
import congealing.*

// Runs the program
fun runInterpreter(
  inputTokens: Sequence<CongealedToken>
): Unit {
  val dataStack: ArrayDeque<Any?> = ArrayDeque()
  inputTokens.forEach { tok ->
    println("dataStack $dataStack op $tok")
    when (tok) {
      is CongealedToken.RawToken -> dataStack.push(tok.tt)
      is CongealedToken.ParsingToken -> {
        val arity = tok.arity
        val argsN: List<Any?> = dataStack.pop(arity)// .map { it as Token }
        val opType = tok.ss
        when (opType) {
          "LITERAL" -> dataStack.push((argsN[0] as Token).literal)
          "ADD", "MULT" -> {
            val left = argsN[0] as LiteralVal
            val right = argsN[2] as LiteralVal
            val op_fn = (argsN[1] as Token).type.let { FN3_IMPL_LOOKUP.get(it) ?: TODO("impl $it") }
            val result = op_fn.invoke(left, right) 
            dataStack.push(result)
          }
          "UNARY" -> {
            val tgt = argsN[1] as LiteralVal
            val op_fn = (argsN[0] as Token).type.let { FN2_IMPL_LOOKUP.get(it) ?: TODO("impl $it") }
            val result = op_fn.invoke(tgt)
            dataStack.push(result)
          }
          "ROOT" -> dataStack.push(argsN[0] as LiteralVal)
          "ROOT0" -> { /* no-op */ } 
          "GROUP" -> dataStack.push(argsN[1] as LiteralVal)
          else -> TODO("hmm $opType")
        }
      }
    }
  }
  println(dataStack.lastOrNull()?.let { (it as LiteralVal).repr() })
}

val FN3_IMPL_LOOKUP: Map<TokenType, (LiteralVal, LiteralVal) -> LiteralVal> = mapOf(
  TokenType.PLUS to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.IntVal(a.v + b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.DoubleVal(a.toDouble() + b.toDouble())
    else if (a is LiteralVal.StringVal && b is LiteralVal.StringVal) LiteralVal.StringVal(a.v + b.v)
    else TODO("types")
  }),
  TokenType.MINUS to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.IntVal(a.v - b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.DoubleVal(a.toDouble() - b.toDouble())
    else TODO("types")
  }),
  TokenType.STAR to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.IntVal(a.v * b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.DoubleVal(a.toDouble() * b.toDouble())
    else TODO("types")
  }),
  TokenType.SLASH to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (b.isNumeric() && b.toDouble() == 0.toDouble()) TODO("divison by 0")
    else if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.IntVal(a.v / b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.DoubleVal(a.toDouble() / b.toDouble())
    else TODO("types")
  }),
  TokenType.PERCENT to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.IntVal(a.v % b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.DoubleVal(a.toDouble() % b.toDouble())
    else TODO("types")
  }),
)

val FN2_IMPL_LOOKUP: Map<TokenType, (LiteralVal) -> LiteralVal> = mapOf(
  TokenType.MINUS to (fun(a: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal) LiteralVal.IntVal(-a.v)
    else if (a is LiteralVal.DoubleVal) LiteralVal.DoubleVal(-a.v)
    else TODO("types")
  }),
)
