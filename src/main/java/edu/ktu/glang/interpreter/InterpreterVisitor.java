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
    public Object visitIntAddOpExpression(GLangParser.IntAddOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        return switch (ctx.intAddOp().getText()) {
            case "+" -> (Integer) val1 + (Integer) val2;
            case "-" -> (Integer) val1 - (Integer) val2;
            default -> null;
        };
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
}
