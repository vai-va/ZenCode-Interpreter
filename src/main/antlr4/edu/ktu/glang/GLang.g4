grammar GLang;

program : statement+ EOF ;

statement
    : variableDeclaration ';'
    | assignment ';'
    | ifStatement
    | printStatement ';'
    | forLoop
    ;

variableDeclaration : TYPE ID '=' expression ;

assignment : ID '=' expression ;

expression
    : INT                               #intExpression
    | ID                                #idExpression
    | '(' expression ')'                #parenthesesExpression
    | expression intMultiOp expression  #intMultiOpExpression
    | expression intAddOp expression    #intAddOpExpression
    ;

intMultiOp : '*' | '/' | '%' ;
intAddOp : '+' | '-' ;

ifStatement : 'if' '(' expression relationOp expression ')' '{' statement '}'
    ('else' '{' statement '}') ;

relationOp : '==' | '!=' | '>' | '<' | '>=' | '<=';

printStatement : PRINT '(' expression ')' ;

//--------------------------------------------
forLoop
    : 'for' '(' initialization ';' condition ';' increment ')' '{' statement* '}' ;

initialization: variableDeclaration | assignment ;
condition: expression relationOp expression ;
increment: assignment ;
//-------------------------------------------

TYPE    : 'int'
        | 'bool'
        ;

PRINT   : 'print';
ID      : [a-zA-Z]+ ;
INT     : [0-9]+ ;

COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;
WS      : [ \t\r\n]+ -> skip ;