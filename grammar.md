# Lox Grammar

```
expression     -> comma ;
comma          -> equality ( "," equality )* ;
conditional    -> equality ( "?" expression ":" conditional )? ;
equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication -> unary ( ( "/" | "*" ) unary )* ;
unary          -> ( "!" | "-" ) unary | primary ;
primary        -> NUMBER | STRING | "false" | "true" | "nil" | "(" expression ")" ;
```
