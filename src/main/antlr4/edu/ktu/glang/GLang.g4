grammar GLang;

program
 : line+ EOF
 ;

line
 : functionDeclaration
 | statement
 ;

statement
 : variableDeclaration ';'
 | assignment ';'
 | ifStatement
 | printStatement ';'
 | forLoop
 | switchStatement
 | increment ';'
 | functionCall ';'
 | systemFunctionCall
 | returnStatement ';'
 | zenFilterStatement ';'
 | filterRulesStatement ';'
 | printArrayStatement ';'
 ;

filterRulesStatement : 'let' ID '=' '[' filterRule (',' filterRule)* ']' ;

zenFilterStatement : 'let' ID '=' 'ZenFilter' '(' ID ',' ID ')' ;

printArrayStatement : PRINT '(' ID ')' ;

variableDeclaration : TYPE ID '=' expression ;

assignment : ID '=' expression ;

expression
 : INT                               #intExpression
 | ID                                #idExpression
 | STRING                            #stringExpression
 | BOOLEAN                           #booleanExpression
 | '(' expression ')'                #parenthesesExpression
 | expression intMultiOp expression  #intMultiOpExpression
 | expression intAddOp expression    #intAddOpExpression
 | functionCall                      #functionCallExpression
 | arrayLiteral                      #arrayLiteralExpression
 ;

intMultiOp : '*' | '/' | '%' ;
intAddOp : '+' | '-' ;

ifStatement : 'if' '(' expression relationOp expression ')' '{' statement* '}'
 ('else' '{' statement* '}')? ;

relationOp : '==' | '!=' | '>' | '<' | '>=' | '<=';

printStatement : PRINT '(' expression ')' ;
//--------------------------------------------

functionDeclaration
 : 'func' ID '(' paramList? ')' functionBody
 ;

paramList : ID (',' ID)* ;

functionBody : '{' statement* '}' ; //TODO cannot return from the middle of the function

functionCall
 : ID '(' expressionList? ')'
 ;
systemFunctionCall
 : PRINT '(' expression ')'                             #printFunctionCall
 ;

returnStatement : 'return' expression? ;

expressionList
 : expression (',' expression)*
 ;

//--------------------------------------------
forLoop
 : 'for' '(' initialization ';' condition ';' increment ')' '{' statement* '}' ;

initialization: variableDeclaration | assignment ;
condition: expression relationOp expression ;
increment: ID ('++' | '--' | ('=' ID intAddOp INT) | ('+=' INT) | ('-=' INT)) ;
//-------------------------------------------
switchStatement: 'switch' '(' expression ')' '{' caseStatement* defaultStatement? '}';

caseStatement: 'case' expression ':' statement*;

defaultStatement: 'default' ':' statement*;
//-------------------------------------------
arrayLiteral : '[' arrayElement (',' arrayElement)* ']' ;

arrayElement : objectLiteral | expression ;

objectLiteral : '{' property (',' property)* '}' ;

property : ID '=' expression ;
//----------------------------------------------

filterRule : '{' 'type' '=' STRING ',' 'property' '=' STRING ',' 'value' '=' STRING '}' ;

//----------------------------------------------

TYPE    : 'int'
        | 'bool'
        | 'string'
        ;

PRINT   : 'print';
STRING : ["] ( ~["\r\n\\] | '\\' ~[\r\n] )* ["] ;
ID      : [a-zA-Z_][a-zA-Z_0-9]* ;
INT     : [0-9]+ ;
BOOLEAN : 'true' | 'false' ;

COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;
WS      : [ \t\r\n]+ -> skip ;
