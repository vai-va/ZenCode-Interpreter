package edu.ktu.glang.interpreter;

import edu.ktu.glang.GLangBaseVisitor;
import edu.ktu.glang.GLangParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class IfStatementVisitor extends GLangBaseVisitor<Object> {

    private final InterpreterVisitor parent;

    public IfStatementVisitor(InterpreterVisitor parent) {
        this.parent = parent;
    }

    @Override
    public Object visitIfStatement(GLangParser.IfStatementContext ctx) {
        // Get the left and right expressions from the context
        Object leftObj = parent.visit(ctx.expression(0));
        Object rightObj = parent.visit(ctx.expression(1));

        // Try to parse the objects as integers
        int left, right;
        try {
            left = Integer.parseInt(leftObj.toString());
            right = Integer.parseInt(rightObj.toString());
        } catch (NumberFormatException e) {
            throw new RuntimeException("Cannot parse input as integers.");
        }

        // Get the relation operator from the context
        TerminalNode relOpNode = (TerminalNode) ctx.relationOp().getChild(0);
        String relOp = relOpNode.getText();

        // Resolve the condition and execute the appropriate statement
        if (resolveCondition(left, right, relOp)) {
            return parent.visit(ctx.statement(0));
        } else {
            return parent.visit(ctx.statement(1));
        }
    }

    private static boolean resolveCondition(int left, int right, String relOp) {
        return switch (relOp) {
            case "==" -> left == right;
            case "!=" -> left != right;
            case ">" -> left > right;
            case "<" -> left < right;
            case ">=" -> left >= right;
            case "<=" -> left <= right;
            default -> throw new RuntimeException("Unsupported operator.");
        };
    }
}
