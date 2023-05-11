package edu.ktu.glang.interpreter;

import edu.ktu.glang.GLangBaseVisitor;
import edu.ktu.glang.GLangParser;


public class InterpreterVisitor extends GLangBaseVisitor<Object> {

    private final StringBuilder SYSTEM_OUT = new StringBuilder();

    private final SymbolTable symbolTable;
    private final IfStatementVisitor ifStatementVisitor;

    public InterpreterVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
        this.ifStatementVisitor = new IfStatementVisitor(this);
    }

    @Override
    public Object visitProgram(GLangParser.ProgramContext ctx) {
        super.visitProgram(ctx);
        return SYSTEM_OUT.toString();
    }

    @Override
    public Object visitVariableDeclaration(GLangParser.VariableDeclarationContext ctx) {
        String varName = ctx.ID().getText();
        Object value = visit(ctx.expression());
        if (!this.symbolTable.contains(varName)) {
            this.symbolTable.put(varName, value);
        } else {
            throw new RuntimeException("Variable already exists.");
        }
        return null;
    }

    @Override
    public Object visitAssignment(GLangParser.AssignmentContext ctx) {
        String varName = ctx.ID().getText();
        Object value = visit(ctx.expression());
        if (this.symbolTable.contains(varName)) {
            this.symbolTable.put(varName, value);
        } else {
            throw new RuntimeException("Undeclared variable.");
        }
        return null;
    }

    @Override
    public Object visitIntExpression(GLangParser.IntExpressionContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Object visitIdExpression(GLangParser.IdExpressionContext ctx) {
        String varName = ctx.ID().getText();
        return this.symbolTable.get(varName);
    }

    @Override
    public Object visitPrintStatement(GLangParser.PrintStatementContext ctx) {
        String text = visit(ctx.expression()).toString();
        //System.out.println(text);
        SYSTEM_OUT.append(text).append("\n");
        return null;
    }

    @Override
    public Object visitParenthesesExpression(GLangParser.ParenthesesExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Integer visitIntAddOpExpression(GLangParser.IntAddOpExpressionContext ctx) {
        Integer val1 = (Integer) visit(ctx.expression(0));
        Integer val2 = (Integer) visit(ctx.expression(1));
        String operator = ctx.intAddOp().getText();

        if (val1 == null || val2 == null) {
            throw new RuntimeException("Null value in arithmetic operation");
        }

        switch (operator) {
            case "+": return val1 + val2;
            case "-": return val1 - val2;
            default: throw new RuntimeException("Unknown operator: " + operator);
        }
    }


    @Override
    public Object visitIntMultiOpExpression(GLangParser.IntMultiOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        //TODO - validation etc
        return switch (ctx.intMultiOp().getText()) {
            case "*" -> (Integer) val1 * (Integer) val2;
            case "/" -> (Integer) val1 / (Integer) val2;
            case "%" -> (Integer) val1 % (Integer) val2;
            default -> null;
        };
    }

    @Override
    public Object visitIfStatement(GLangParser.IfStatementContext ctx) {
        return this.ifStatementVisitor.visitIfStatement(ctx);
    }

    @Override
    public Void visitForLoop(GLangParser.ForLoopContext ctx) {
        // Execute the initialization
        visit(ctx.initialization());

        // Loop while the condition is true
        while ((boolean) visitCondition(ctx.condition())) {
            // Execute the statements in the loop body
            for (GLangParser.StatementContext stmt : ctx.statement()) {
                visit(stmt);
            }

            // Execute the increment
            visit(ctx.increment());
        }

        return null;
    }
    @Override
    public Integer visitIncrement(GLangParser.IncrementContext ctx) {
        // Get the name of the variable to increment
        String varName = ctx.ID().getText();

        // Get the current value of the variable from the symbol table
        int currentValue = (int)symbolTable.get(varName);

        // Increment the variable
        int newValue = currentValue + 1;

        // Update the symbol table with the new value
        symbolTable.put(varName, newValue);

        // Return the new value
        return newValue;
    }
    
    @Override
    public Object visitSwitchStatement(GLangParser.SwitchStatementContext ctx) {
        Object value = visit(ctx.expression());
        boolean foundMatch = false;

        for (GLangParser.CaseStatementContext caseCtx : ctx.caseStatement()) {
            Object caseValue = visit(caseCtx.expression());
            if (value.equals(caseValue)) {
                // Execute the statements inside the matching case
                foundMatch = true;
                for (GLangParser.StatementContext stmt : caseCtx.statement()) {
                    visit(stmt);
                }
                break;
            }
        }
        if (!foundMatch && ctx.defaultStatement() != null) {
            for (GLangParser.StatementContext stmt : ctx.defaultStatement().statement()) {
                visit(stmt);
            }
        }
        return null;
    }

    @Override
    public Object visitCondition(GLangParser.ConditionContext ctx) {
        Integer left = (Integer) visit(ctx.expression(0));
        Integer right = (Integer) visit(ctx.expression(1));
        String operator = ctx.relationOp().getText();

        switch (operator) {
            case "==": return left.equals(right);
            case "!=": return !left.equals(right);
            case "<": return left < right;
            case ">": return left > right;
            case "<=": return left <= right;
            case ">=": return left >= right;
            default: throw new RuntimeException("Unknown operator: " + operator);
        }
    }



}
