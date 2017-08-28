# klox
A Kotlin implementation of the Lox language (mimics the Java implementation from ["Crafting Interpreters"][book]).

The implementation follows [the book][book] by [munificent][].

To launch the interpreter or run a file, use `./klox [file]` after building the `jar` target with Gradle.

## Grammar

This is the grammar implemented in klox.

It's mostly the grammar as explained in the book, with some of the "challenges" implemented as well.

Both line (`//`) and multi-line (`/* */`) comments are supported (but not nested multi-line comments, at the moment).

```
program        -> declaration* EOF ;

declaration    -> funDecl | varDecl | statement ;

funDecl        -> "fun" function ;
function       -> IDENTIFIER "(" parameters? ")" block ;
parameters     -> IDENTIFIER ( "," IDENTIFIER )* ;

varDecl        -> "var" IDENTIFIER ("=" expression)? ";" ;

statement      -> exprStmt | forStmt | ifStmt | returnStmt
                | whileStmt | block | break ;
exprStmt       -> expression ";" ;
forStmt        -> "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;
ifStmt         -> "if" "(" expression ")" statement ( "else" statement )? ;
returnStmt     -> "return" expression? ";" ;
whileStmt      -> "while" "(" expression ")" statement ;
block          -> "{" declaration* "}" ;
break          -> "break" ";" ;

expression     -> comma ;
comma          -> assignment ( "," assignment )* ;
arguments      -> assignment ( "," assignment )* ;
assignment     -> IDENTIFIER ( "-" | "+" )? "=" assignment | conditional ;
conditional    -> logic_or ( "?" expression ":" conditional )? ;
logic_or       -> logic_and ( "or" logic_and )* ;
logic_and      -> equality ( "and" equality )* ;
equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication -> unary ( ( "/" | "*" ) unary )* ;
unary          -> ( "!" | "-" ) unary | call ;
call           -> primary ( "(" arguments? ")" )* ;
primary        -> NUMBER | STRING | "false" | "true" | "nil"
                | "(" expression ")" | IDENTIFIER | funExpr
                // Erroneous grammar
                | ( "!=" | "==" ) equality
                | ( ">" | ">=" | "<" | "<=" ) comparison
                | "+" addition
                | ( "/" | "*" ) multiplication ;
funExpr        -> "fun" "(" parameters? ")" block ;
```

## Built-ins

The implementation contains the built-in functions from the book, as well as some additional ones.

Some of these functions are inspired from their namesakes in the Lua language.

 * `print(value)` - Prints `value` to stdout (originally a statement).
 * `clock()` - Returns current time in seconds since the Unix epoch.
 * `read()` - Reads one line from stdin and returns it.
 * `type(value)` - Returns the type of `value`.
 * `tonumber(value)` - Converts `value` to a number (or `nil` if conversion fails).
 * `tostring(value)` - Returns the string representation of `value`.

## License

Copyright (c) 2017 by Adam Hellberg.

This project is licensed under the [MIT License][mit], following the [main repo][main] licensing the code part under MIT.

See the file `LICENSE` for more information.

[book]: http://www.craftinginterpreters.com/
[munificent]: https://github.com/munificent
[mit]: https://opensource.org/licenses/MIT
[main]: https://github.com/munificent/craftinginterpreters
