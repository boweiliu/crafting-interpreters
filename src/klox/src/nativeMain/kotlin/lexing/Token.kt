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
  STRING, // done
  IDENTIFIER,
  NUMBER, // done
  COMMENT, // done

  COMMA, // done
  DOT, // done
  SEMICOLON, // done

  PLUS, // done
  MINUS, // done
  STAR, // done
  SLASH, // done
  PERCENT, // done
  BANG, // done

  EQUAL, // done
  BANG_EQUAL, // done
  EQUAL_EQUAL, // done
  LESS_THAN, // done
  LESS_THAN_EQUAL, // done
  GREATER_THAN, // done
  GREATER_THAN_EQUAL, // done
  LEFT_PAREN, // done
  RIGHT_PAREN, // done
  LEFT_BRACE, // done
  RIGHT_BRACE, // done

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

  EOF, // done
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

  fun isNumeric(): Boolean = (this is IntVal || this is DoubleVal)
  fun toDoubleOrNull(): Double? = when (this) {
    is IntVal -> this.v.toDouble()
    is DoubleVal -> this.v
    is StringVal -> null
  }
  fun toDouble(): Double = this.toDoubleOrNull()!!
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
    "TRUE" to TokenType.TRUE,
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
