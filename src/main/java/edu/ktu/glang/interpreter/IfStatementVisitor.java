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

        // Get the relation operator from the context
        TerminalNode relOpNode = (TerminalNode) ctx.relationOp().getChild(0);
        String relOp = relOpNode.getText();

        // Resolve the condition and execute the appropriate statement
        if (resolveCondition(leftObj, rightObj, relOp)) {
            return parent.visit(ctx.statement(0));
        } else if (ctx.statement().size() > 1) { // check if there is an else statement
            return parent.visit(ctx.statement(1));
        }

        return null; // or return any default value
    }


    private boolean resolveCondition(Object left, Object right, String relOp) {
        switch (relOp) {
            case "==" -> {
                if(left instanceof Integer && right instanceof Integer) {
                    return ((Integer) left).intValue() == ((Integer) right).intValue();
                } else if (left instanceof String && right instanceof String) {
                    return ((String) left).equals((String) right);
                } else {
                    throw new RuntimeException("Incompatible types.");
                }
            }
            case "!=" -> {
                if(left instanceof Integer && right instanceof Integer) {
                    return ((Integer) left).intValue() != ((Integer) right).intValue();
                } else if (left instanceof String && right instanceof String) {
                    return !((String) left).equals((String) right);
                } else {
                    throw new RuntimeException("Incompatible types.");
                }
            }
            case ">" -> {
                if(left instanceof Integer && right instanceof Integer) {
                    return (Integer)left > (Integer)right;
                } else {
                    throw new RuntimeException("Incompatible types or unsupported operator for these types.");
                }
            }
            case "<" -> {
                if(left instanceof Integer && right instanceof Integer) {
                    return (Integer)left < (Integer)right;
                } else {
                    throw new RuntimeException("Incompatible types or unsupported operator for these types.");
                }
            }
            case ">=" -> {
                if(left instanceof Integer && right instanceof Integer) {
                    return (Integer)left >= (Integer)right;
                } else {
                    throw new RuntimeException("Incompatible types or unsupported operator for these types.");
                }
            }
            case "<=" -> {
                if(left instanceof Integer && right instanceof Integer) {
                    return (Integer)left <= (Integer)right;
                } else {
                    throw new RuntimeException("Incompatible types or unsupported operator for these types.");
                }
            }
            default -> throw new RuntimeException("Unsupported operator.");
        }
    }
}
