# Lox Grammar

This is the grammar implemented in klox.

It's mostly the grammar as explained in the book, with some of the "challenges" implemented as well.

```
program        -> declaration* EOF ;

declaration    -> varDecl | statement ;

varDecl        -> "var" IDENTIFIER ("=" expression)? ";" ;

statement      -> exprStmt | forStmt | ifStmt | printStmt | whileStmt | block | break ;
exprStmt       -> expression ";" ;
forStmt        -> "for" "(" ( varDecl | exprStmt | ";" ) expression? ";" expression? ")" statement ;
ifStmt         -> "if" "(" expression ")" statement ( "else" statement )? ;
printStmt      -> "print" expression ";" ;
whileStmt      -> "while" "(" expression ")" statement ;
block          -> "{" declaration* "}" ;
break          -> "break" ";" ;

expression     -> comma ;
comma          -> assignment ( "," assignment )* ;
arguments      -> assignment ( "," assignment )* ;
assignment     -> IDENTIFIER "=" assignment | conditional ;
conditional    -> logic_or ( "?" expression ":" conditional )? ;
logic_or       -> logic_and ( "or" logic_and )* ;
logic_and      -> equality ( "and" equality )* ;
equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication -> unary ( ( "/" | "*" ) unary )* ;
unary          -> ( "!" | "-" ) unary | call ;
call           -> primary ( "(" arguments? ")" )* ;
primary        -> NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" | IDENTIFIER
                // Erroneous grammar
                | ( "!=" | "==" ) equality
                | ( ">" | ">=" | "<" | "<=" ) comparison
                | "+" addition
                | ( "/" | "*" ) multiplication ;
```
