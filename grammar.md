# Lox Grammar

This is the grammar implemented in klox.

It's mostly the grammar as explained in the book, with some of the "challenges" implemented as well.

```
program        -> declaration* EOF ;

declaration    -> varDecl | statement ;

varDecl        -> "var" IDENTIFIER ("=" expression)? ";" ;

statement      -> exprStmt | ifStmt | printStmt | block ;
exprStmt       -> expression ";" ;
ifStmt         -> "if" "(" expression ")" statement ( "else" statement )? ;
printStmt      -> "print" expression ";" ;
block          -> "{" declaration* "}" ;

expression     -> comma ;
comma          -> assignment ( "," assignment )* ;
assignment     -> IDENTIFIER "=" assignment | conditional ;
conditional    -> equality ( "?" expression ":" conditional )? ;
equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication -> unary ( ( "/" | "*" ) unary )* ;
unary          -> ( "!" | "-" ) unary | primary ;
primary        -> NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" | IDENTIFIER
                // Erroneous grammar
                | ( "!=" | "==" ) equality
                | ( ">" | ">=" | "<" | "<=" ) comparison
                | "+" addition
                | ( "/" | "*" ) multiplication ;
```
