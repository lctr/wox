# Wox

Ahoy! This here is a simple [Lox](http://www.craftinginterpreters.com/)
implementation in Java, with a subtle stylistic influence from my (main)
Rust-based compiler project [_Wysk_](https://github.com/lctr/wysk) as well as
absolutely _zero_ third-party dependencies.

The main goal of this project is to review Java. Thus, simplifications and/or
particular optimizations (and hand-offs to other low-level languages) are
generally to be ignored in favor of re-exploring Java.

Additionally, I hit a standstill in writing _Wysk_'s backend. Since _Lox_, and
by extension _Wox_, is a simple imperative class-based language -- with greater
resources -- it should be theoretically easier to write a VM, as well as a
simple compiler in C.

The _C_-based compiler will come later when I transition to reviewing _C_ after
_Java_.

## Differences from Lox

Wox is a but more expression-based than common Lox implementations.

- Conditionals are expressions and do not require surrounding parentheses around
  the predicate, but do require `then` and `else` keywords (and accompanying
  subexpressions) to be included
- Rust-like `loop` expressions are implemented as statements
- _Wox_ introduces `do`-expressions, which are effectively block-statements that
  return the last expression
- `return` statements are optional, i.e., block statements return their last
  expression
- Syntax sugar for tuples and arrays have been added to the parser
- The `fun` keyword has been replaced with the shorter `fn` keyword

## Differences from Wysk

- _Wox_ is imperative and class-based, while _Wysk_ is declarative/functional
- _Wox_ is dynamically typed, while _Wysk_ is statically typed and type-inferred
- _Wox_ is primarily interpeted, while _Wysk_ is compiled
- _Wox_ is written in Java (and then C, per the
  [book](http://www.craftinginterpreters.com/)), while _Wysk_ is written in Rust
