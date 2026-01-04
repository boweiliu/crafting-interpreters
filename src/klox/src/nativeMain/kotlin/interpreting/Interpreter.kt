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
        val arityType = Token.LOOKUP_OP_TO_ARITY_TYPE[opType]
        when (arityType) {
          "x" -> dataStack.push((argsN[0] as Token).literal) // "LITERAL"
          "x A x" -> {
            val left = argsN[0] as LiteralVal
            val right = argsN[2] as LiteralVal
            val op_fn = (argsN[1] as Token).type.let { FN3_IMPL_LOOKUP.get(it) ?: TODO("impl $it") }
            val result = op_fn.invoke(left, right) 
            dataStack.push(result)
          }
          "A x A" -> dataStack.push(argsN[1] as LiteralVal) // "GROUP"
          "A x" -> {
            val tgt = argsN[1] as LiteralVal
            val op_fn = (argsN[0] as Token).type.let { FN2_IMPL_LOOKUP.get(it) ?: TODO("impl $it") }
            val result = op_fn.invoke(tgt)
            dataStack.push(result)
          }
          "x A" -> {
            val tgt = argsN[0] as LiteralVal
            val op_fn = (argsN[1] as Token).type.let { FN2_IMPL_LOOKUP.get(it) ?: TODO("impl $it") }
            val result = op_fn.invoke(tgt)
            dataStack.push(result)
          }
          "" -> { /* no-op */ }  // "ROOT"
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
  TokenType.LESS_THAN to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.BooleanVal(a.v < b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.BooleanVal(a.toDouble() < b.toDouble())
    else if (a is LiteralVal.StringVal && b is LiteralVal.StringVal) LiteralVal.BooleanVal(a.v < b.v)
    else TODO("types")
  }),
  TokenType.LESS_THAN_EQUAL to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.BooleanVal(a.v <= b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.BooleanVal(a.toDouble() <= b.toDouble())
    else if (a is LiteralVal.StringVal && b is LiteralVal.StringVal) LiteralVal.BooleanVal(a.v <= b.v)
    else TODO("types")
  }),
  TokenType.GREATER_THAN to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.BooleanVal(a.v > b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.BooleanVal(a.toDouble() > b.toDouble())
    else if (a is LiteralVal.StringVal && b is LiteralVal.StringVal) LiteralVal.BooleanVal(a.v > b.v)
    else TODO("types")
  }),
  TokenType.GREATER_THAN_EQUAL to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal && b is LiteralVal.IntVal) LiteralVal.BooleanVal(a.v >= b.v)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.BooleanVal(a.toDouble() >= b.toDouble())
    else if (a is LiteralVal.StringVal && b is LiteralVal.StringVal) LiteralVal.BooleanVal(a.v >= b.v)
    else TODO("types")
  }),
  TokenType.EQUAL_EQUAL to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a.typ == b.typ) LiteralVal.BooleanVal(a.vl == b.vl)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.BooleanVal(a.toDouble() == b.toDouble())
    else TODO("types")
  }),
  TokenType.BANG_EQUAL to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a.typ == b.typ) LiteralVal.BooleanVal(a.vl != b.vl)
    else if (a.isNumeric() && b.isNumeric()) LiteralVal.BooleanVal(a.toDouble() != b.toDouble())
    else TODO("types")
  }),
  TokenType.AND to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.BooleanVal && b is LiteralVal.BooleanVal) LiteralVal.BooleanVal(a.v  && b.v)
    else if (a.isBoolable() && b.isBoolable()) LiteralVal.BooleanVal(a.toBoolean() && b.toBoolean())
    else TODO("types")
  }),
  TokenType.OR to (fun(a: LiteralVal, b: LiteralVal): LiteralVal {
    return if (a is LiteralVal.BooleanVal && b is LiteralVal.BooleanVal) LiteralVal.BooleanVal(a.v  || b.v)
    else if (a.isBoolable() && b.isBoolable()) LiteralVal.BooleanVal(a.toBoolean() || b.toBoolean())
    else TODO("types")
  }),
)

val FN2_IMPL_LOOKUP: Map<TokenType, (LiteralVal) -> LiteralVal> = mapOf(
  TokenType.MINUS to (fun(a: LiteralVal): LiteralVal {
    return if (a is LiteralVal.IntVal) LiteralVal.IntVal(-a.v)
    else if (a is LiteralVal.DoubleVal) LiteralVal.DoubleVal(-a.v)
    else TODO("types")
  }),
  TokenType.NOT to (fun(a: LiteralVal): LiteralVal {
    return if (a is LiteralVal.BooleanVal) LiteralVal.BooleanVal(!(a.v))
    else TODO("types")
  }),
  TokenType.BANG to (fun(a: LiteralVal): LiteralVal {
    return if (a is LiteralVal.BooleanVal) LiteralVal.BooleanVal(!(a.v))
    else TODO("types")
  }),
  TokenType.EOF to (fun(a: LiteralVal): LiteralVal = a),
)
