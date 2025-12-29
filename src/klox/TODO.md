1. refactor lineNo + fileName = location

## KOTLIN

1. use template strings instead of custom string fformat
2. find the gradle options to remove all compiler optimizations, which should help with debug runstacks
3. find a popen library and import it

## LEXER

1. Add a notion of "Unknown tokens" to support slots later. in addition to erroring, caputre all unknown characters into tokens, and also use it for unparseable numbers
2. Add colNo in addition to lineNo, for better location
3. Add compiler stacktraces to debug info on error messages so we can debug the compiler (need to thread it through where the errors are created)
4. Finish up that test to make sure lexing is seq2seq (important for feeding in a sequence to the parser)
5. Newlines (and later, other whitespace tokens) are sure to be useful - lets emit them during lexing in case we want to do something useful during parsing (eg syntax error recovery)
6. Think about how extensible the lexer is

## PARSER

1. separate it out into a grouper and whatever else
2. grouper should convert the token stream into a forth-like stack, with GROUPN tokens inserted that indicate the sentence type too (XOX, OXO, etc.)
3. Remember to write your code functionally! functional core imperative shell means easy testing. also means that it needs to "emit" scratch modifications so they can be applied and emitted
3. Build incrementally in both dimensions of the double dispatch problem -- add visitor behaviors and tree nodes in whatever order. Find a way to runtime skip / default behavior to enable testing ASAP.
4. If you foresee trouble writing in coroutine style in C or zig, zoom to self-hosting
5. In order to self-host asap, we'll want a way to drop down to C syntax... unclear how tho
