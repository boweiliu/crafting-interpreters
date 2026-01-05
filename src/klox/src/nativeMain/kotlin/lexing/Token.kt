package lexing

data class Token(
  val type: TokenType,

  // the full text of it
  val lexeme: String,

  // the semantic value, if literal
  val literal: LiteralVal?,

  val lineNo: Int,
  val fileName: String,
) {
  companion object {}
}

enum class TokenType {
  // literals
  STRING,
  IDENTIFIER,
  NUMBER,
  COMMENT,

  COMMA,
  DOT,
  SEMICOLON,

  PLUS,
  MINUS,
  STAR,
  SLASH,
  PERCENT,
  BANG,

  EQUAL,
  BANG_EQUAL,
  EQUAL_EQUAL,
  LESS_THAN,
  LESS_THAN_EQUAL,
  GREATER_THAN,
  GREATER_THAN_EQUAL,
  LEFT_PAREN,
  RIGHT_PAREN,
  LEFT_BRACE,
  RIGHT_BRACE,

  AND,
  OR,
  NOT,
  TRUE,
  FALSE,
  NIL,

  PRINT,
  CLOCK,
  FN,
  VAR,
  IF,
  ELSE,
  WHILE,
  FOR,
  FUNCALL,
  RETURN,
  CLASS,
  THIS,
  SUPER,

  EOF,
}

sealed class LiteralVal(val typ: String) {
  data class StringVal(val v: String): LiteralVal("str")
  data class IntVal(val v: Int): LiteralVal("int")
  data class DoubleVal(val v: Double): LiteralVal("double")
  data class BooleanVal(val v: Boolean): LiteralVal("bool")
  object NullVal: LiteralVal("null")

  val vl get(): Any? {
    return when(this) {
      is StringVal -> this.v
      is IntVal -> this.v
      is DoubleVal -> this.v
      is BooleanVal -> this.v
      is NullVal -> null
    }
  }
  fun repr(): String {
    return when(this) {
      is StringVal -> "\"" + this.v + "\""
      else -> this.vl.toString()
    }
  }

  fun isNumeric(): Boolean = (this is IntVal || this is DoubleVal)

  fun toDoubleOrNull(): Double? = when (this) {
    is IntVal -> this.v.toDouble()
    is DoubleVal -> this.v
    else -> null
  }
  fun toDouble(): Double = this.toDoubleOrNull()!!

  fun isBoolable(): Boolean = (this is BooleanVal || this is NullVal)
  fun toBooleanOrNull(): Boolean? = when (this) {
    is BooleanVal -> this.v
    is NullVal -> false
    else -> null
  }
  fun toBoolean(): Boolean = this.toBooleanOrNull()!!
}

val Token.Companion.LOOKUP_1CH_TO_TOKEN: Map<Char, TokenType>
  get() = mapOf(
    '(' to TokenType.LEFT_PAREN,
    ')' to TokenType.RIGHT_PAREN,
    '{' to TokenType.LEFT_BRACE,
    '}' to TokenType.RIGHT_BRACE,
    ',' to TokenType.COMMA,
    ';' to TokenType.SEMICOLON,
    '+' to TokenType.PLUS,
    '-' to TokenType.MINUS,
    '*' to TokenType.STAR,
    '/' to TokenType.SLASH,
    '%' to TokenType.PERCENT,

    // tricky but we took care of this
    '.' to TokenType.DOT,

    // remember to handle these as bigrams
    '!' to TokenType.BANG,
    '=' to TokenType.EQUAL,
    '<' to TokenType.LESS_THAN,
    '>' to TokenType.GREATER_THAN,
  )

val Token.Companion.LOOKUP_1TOKEN_TO_CH: Map<TokenType, Char>
  get() = Token.LOOKUP_1CH_TO_TOKEN.map { (k,v) -> v to k }.toMap()

val Token.Companion.LOOKUP_2CH_TO_TOKEN: Map<Pair<Char, Char>, TokenType>
  get() = mapOf(
    Pair('!' , '=') to TokenType.BANG_EQUAL,
    Pair('=' , '=') to TokenType.EQUAL_EQUAL,
    Pair('<' , '=') to TokenType.LESS_THAN_EQUAL,
    Pair('>' , '=') to TokenType.GREATER_THAN_EQUAL,
  )

// val Token.Companion.LOOKUP_2TOKEN_TO_CH: Map<TokenType, Char>
//   get() = Token.LOOKUP_1CH_TO_TOKEN.map { (k,v) -> v to k }.toMap()

val Token.Companion.LOOKUP_ALPHA_TO_TOKEN: Map<String, TokenType>
  get() = mapOf(
    "AND" to TokenType.AND,
    "OR" to TokenType.OR,
    "NOT" to TokenType.NOT,
    "TRUE" to TokenType.TRUE, // hmm how to populate LiteralVal here too? or no?
    "FALSE" to TokenType.FALSE,
    "NIL" to TokenType.NIL,
    "PRINT" to TokenType.PRINT,
    "CLOCK" to TokenType.CLOCK,
    "FN" to TokenType.FN,
    "VAR" to TokenType.VAR,
    "IF" to TokenType.IF,
    "ELSE" to TokenType.ELSE,
    "WHILE" to TokenType.WHILE,
    "FOR" to TokenType.FOR,
    "FUNCALL" to TokenType.FUNCALL,
    "RETURN" to TokenType.RETURN,
    "CLASS" to TokenType.CLASS,
    "THIS" to TokenType.THIS,
    "SUPER" to TokenType.SUPER,
  )

fun TokenType.toLiteralValOrNull() =
  when (this) {
    TokenType.TRUE -> LiteralVal.BooleanVal(true)
    TokenType.FALSE -> LiteralVal.BooleanVal(false)
    TokenType.NIL -> LiteralVal.NullVal
    else -> null
  }

fun TokenTypeSet(vararg args: TokenType) = args.toSet()

