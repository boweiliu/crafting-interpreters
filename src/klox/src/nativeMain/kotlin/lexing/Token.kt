package lexing

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
  GREATER_THAN,
  LESS_THAN_EQUAL,
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
  VAR,
  CLOCK,
  IF,
  ELSE,
  WHILE,
  FOR,
  FUNCALL,
  FN,
  RETURN,
  CLASS,
  THIS,
  SUPER,

  EOF,
}

sealed interface LiteralVal {
  data class StringVal(val v: String): LiteralVal
  data class IntVal(val v: Int): LiteralVal
  data class DoubleVal(val v: Double): LiteralVal
  val vl get(): Any {
    return when(this) {
      is StringVal -> this.v
      is IntVal -> this.v
      is DoubleVal -> this.v
    }
  }
}

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

val Token.Companion.LOOKUP_1CH_TO_TOKEN: Map<Char, TokenType>
  get() = mapOf(
    '(' to TokenType.LEFT_PAREN,
    ')' to TokenType.RIGHT_PAREN,
  )

val Token.Companion.LOOKUP_1TOKEN_TO_CH: Map<TokenType, Char>
  get() = Token.LOOKUP_1CH_TO_TOKEN.map { (k,v) -> v to k }.toMap()

