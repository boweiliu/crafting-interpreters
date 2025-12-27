1. refactor lineNo + fileName = location


## LEXER

1. Add a notion of "Unknown tokens" to support slots later. in addition to erroring, caputre all unknown characters into tokens, and also use it for unparseable numbers
2. Add colNo in addition to lineNo, for better location
3. Add compiler stacktraces to debug info on error messages so we can debug the compiler (need to thread it through where the errors are created)
4. Finish up that test to make sure lexing is seq2seq (important for feeding in a sequence to the parser)
5. Newlines (and later, other whitespace tokens) are sure to be useful - lets emit them during lexing in case we want to do something useful during parsing (eg syntax error recovery)
6. Think about how extensible the lexer is
