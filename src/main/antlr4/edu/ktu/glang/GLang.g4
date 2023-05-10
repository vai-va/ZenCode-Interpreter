grammar GLang;

program : statement+ EOF ;

statement
    : variableDeclaration ';'
    | assignment ';'
    | ifStatement
    | printStatement ';'
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

relationOp : '==' | '!=' ;

printStatement : PRINT '(' expression ')' ;

TYPE    : 'int'
        | 'bool'
        ;

PRINT   : 'print';
ID      : [a-zA-Z]+ ;
INT     : [0-9]+ ;

COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;
WS      : [ \t\r\n]+ -> skip ;