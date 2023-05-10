# glang-interpreter

# glang-base
GLang is a simple scripting language created with ANTLR4 using visitor pattern and supports the following features:
1. Variables: variables can be assigned and used in expressions.
2. Arithmetic expressions: arithmetic expressions can be used involving addition, subtraction, multiplication, division, and modulus operations.
3. Parentheses: parentheses can be used to group subexpressions to control operator precedence.
4. Conditional statements: the language allows for if-else statements that can evaluate boolean expressions and execute different blocks of code based on the result.
5. Comparison operators: the grammar allows for comparison operators such as ==, !=, <, >, <=, and >=.
6. Printing: the language includes a simple print statement that can output the value of an expression.

#### Tools

> Oracle OpenJDK 19.0.2

> Apache Maven 3.8.5

> IntelliJ IDEA Ultimate 2022.3.2
> - ANTLR v4 plugin

#### Build

Generate ANTLR visitor with:

    mvn clean antlr4:antlr4@generate-visitor

Build jar with:

    mvn clean install

Run jar with:

    java -jar target/glang-interpreter-1.0.jar -f samples/test.glang
    java -jar target/glang-interpreter-1.0.jar -i

#### Features ideas