# Lox Grammar

```
program        -> declaration* EOF ;

declaration    -> varDecl | statement ;

varDecl        -> "var" IDENTIFIER ("=" expression)? ";" ;

statement      -> exprStmt | printStmt ;
exprStmt       -> expression ";" ;
printStmt      -> "print" expression ";" ;

expression     -> comma ;
comma          -> equality ( "," equality )* ;
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
