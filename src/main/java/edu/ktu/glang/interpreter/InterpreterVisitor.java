package edu.ktu.glang.interpreter;

import edu.ktu.glang.GLangBaseVisitor;
import edu.ktu.glang.GLangParser;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.*;


public class InterpreterVisitor extends GLangBaseVisitor<Object> {

    private final StringBuilder SYSTEM_OUT = new StringBuilder();

    private final SymbolTable symbolTable;
    private final IfStatementVisitor ifStatementVisitor;
    private final Stack<GLangScope> scopeStack = new Stack<>();
    private GLangScope currentScope = new GLangScope();


    private final Map<String, GLangParser.FunctionDeclarationContext> functions = new HashMap<>();

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
        if (value instanceof List<?>) {
            List<?> listValue = (List<?>) value;
            this.symbolTable.put(varName, listValue);
        } else if (value instanceof Integer) {
            int intValue = (Integer) value;
            this.symbolTable.put(varName, intValue);
        } else {
            throw new RuntimeException("Invalid assignment. Expected an array literal or an integer value.");
        }
        return null;
    }

    @Override
    public Object visitIntExpression(GLangParser.IntExpressionContext ctx) {
        return Integer.parseInt(ctx.INT().getText());
    }

    @Override
    public Object visitBooleanExpression(GLangParser.BooleanExpressionContext ctx) {
        return Boolean.parseBoolean(ctx.BOOLEAN().getText());
    }

    @Override
    public Object visitIdExpression(GLangParser.IdExpressionContext ctx) {
        String varName = ctx.ID().getText();
        if (currentScope.isDeclared(varName)) {
            return currentScope.resolveVariable(varName);
        }
        else if (this.symbolTable.contains(varName)) {
            return this.symbolTable.get(varName);
        }
        else {
            throw new RuntimeException("Undeclared variable: " + varName);
        }
    }

    @Override
    public Object visitPrintStatement(GLangParser.PrintStatementContext ctx) {
        Object expressionValue = visit(ctx.expression());

        if (expressionValue instanceof String) {
            // Print a regular string expression
            String text = expressionValue.toString();
            SYSTEM_OUT.append(text).append("\n");
        } else if (expressionValue instanceof List) {
            // Print an array or list
            List<Object> list = (List<Object>) expressionValue;
            for (Object element : list) {
                SYSTEM_OUT.append(element.toString()).append("\n");
            }
        } else {
            String text = visit(ctx.expression()).toString();
            //System.out.println(text);
            SYSTEM_OUT.append(text).append("\n");
        }

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
        while ((boolean) visit(ctx.condition())) {
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
        String varName = ctx.ID(0).getText(); // get the variable name
        int currentValue = (int) symbolTable.get(varName); // get the current value
        int incrementValue = ctx.INT() != null ? Integer.parseInt(ctx.INT().getText()) : 1; // get the increment value

        if (ctx.getChild(1).getText().equals("++")) { // handle i++ case
            currentValue++;
        } else if (ctx.getChild(1).getText().equals("--")) { // handle i-- case
            currentValue--;
        } else if (ctx.getChild(1).getText().equals("+=")) { // handle i += x case
            currentValue += incrementValue;
        } else if (ctx.getChild(1).getText().equals("-=")) { // handle i -= x case
            currentValue -= incrementValue;
        }

        symbolTable.put(varName, currentValue); // update the symbol table
        return currentValue;
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

    @Override
    public Object visitStringExpression(GLangParser.StringExpressionContext ctx) {
        String str = ctx.STRING().getText();
        // Remove the leading and trailing quotation marks
        str = str.substring(1, str.length() - 1);
        // Return the modified string
        return str;
    }


    @Override
    public Object visitReturnStatement(GLangParser.ReturnStatementContext ctx) {
        if (ctx.expression() == null) {
            return new ReturnValue(null);
        } else {
            return new ReturnValue(this.visit(ctx.expression()));
        }
    }
    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Object currentResult) {
        return !(currentResult instanceof ReturnValue);
    }
    @Override
    public Object visitFunctionDeclaration(GLangParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.ID().getText();

        //TODO create Function class that has constructor(FunctionDeclarationContext), invoke method
        //TODO validate if does not exist
        //TODO probably something else
        this.functions.put(functionName, ctx);
        return null;
    }
    @Override
    public Object visitFunctionCall(GLangParser.FunctionCallContext ctx) {

        String functionName = ctx.ID().getText();
        //TODO validate if exists
        GLangParser.FunctionDeclarationContext function = this.functions.get(functionName);

        //TODO validate args count

        List<Object> arguments = new ArrayList<>();
        if (ctx.expressionList() != null) {
            for (var expr : ctx.expressionList().expression()) {
                arguments.add(this.visit(expr));
            }
        }

        //TODO validate args types

        GLangScope functionScope = new GLangScope();

        if (function.paramList() != null) {
            for (int i = 0; i < function.paramList().ID().size(); i++) {
                String paramName = function.paramList().ID(i).getText();
                functionScope.declareVariable(paramName, arguments.get(i));
            }
        }

        scopeStack.push(currentScope);
        currentScope = functionScope;
        ReturnValue value = (ReturnValue) this.visitFunctionBody(function.functionBody());
        currentScope = scopeStack.pop();

        return value.getValue();
    }

    @Override
    public Object visitFunctionBody(GLangParser.FunctionBodyContext ctx) {
        Object value = super.visitFunctionBody(ctx);
        if (value instanceof ReturnValue) {
            return value;
        }
        return new ReturnValue(null);
    }

    @Override
    public Object visitArrayLiteral(GLangParser.ArrayLiteralContext ctx) {
        List<Object> elements = new ArrayList<>();
        for (GLangParser.ArrayElementContext elementCtx : ctx.arrayElement()) {
            elements.add(visit(elementCtx));
        }
        return elements;
    }

    @Override
    public Object visitObjectLiteral(GLangParser.ObjectLiteralContext ctx) {
        Map<String, Object> properties = new HashMap<>();
        for (GLangParser.PropertyContext propertyCtx : ctx.property()) {
            String propertyName = propertyCtx.ID().getText();
            Object value = visit(propertyCtx.expression());
            properties.put(propertyName, value);
        }
        return properties;
    }

    @Override
    public Object visitFilterRule(GLangParser.FilterRuleContext ctx) {
        String type = ctx.STRING(0).getText();
        String property = ctx.STRING(1).getText();
        String value = ctx.STRING(2).getText();

        // Remove the leading and trailing quotation marks
        type = type.substring(1, type.length() - 1);
        property = property.substring(1, property.length() - 1);
        value = value.substring(1, value.length() - 1);

        Map<String, Object> filterRule = new HashMap<>();
        filterRule.put("type", type);
        filterRule.put("property", property);
        filterRule.put("value", value);

        return filterRule;
    }
    
     @Override
    public Object visitFilterRulesStatement(GLangParser.FilterRulesStatementContext ctx) {
        String filterRulesName = ctx.ID().getText();
        List<Map<String, Object>> filterRules = new ArrayList<>();

        for (GLangParser.FilterRuleContext filterRuleContext : ctx.filterRule()) {
            Map<String, Object> filterRule = (Map<String, Object>) visit(filterRuleContext);
            filterRules.add(filterRule);
        }

        symbolTable.put(filterRulesName, filterRules);
        return null;
    }

    @Override
    public Object visitZenFilterStatement(GLangParser.ZenFilterStatementContext ctx) {
        String filteredUsersName = ctx.ID(0).getText();
        String usersName = ctx.ID(1).getText();
        String filterRulesName = ctx.ID(2).getText();

        List<Map<String, Object>> users = (List<Map<String, Object>>) symbolTable.get(usersName);
        List<Map<String, Object>> filterRules = (List<Map<String, Object>>) symbolTable.get(filterRulesName);

        List<Map<String, Object>> filteredUsers = new ArrayList<>();

        for (Map<String, Object> user : users) {
            boolean passFilter = true;

            if(filterRules != null) {
                for (Map<String, Object> filterRule : filterRules) {
                    String property = (String) filterRule.get("property");
                    String value = (String) filterRule.get("value");

                    if (!user.containsKey(property) || !compareValues(user.get(property), value)) {
                        passFilter = false;
                        break;
                    }
                }
            }

            if (passFilter) {
                filteredUsers.add(user);
            }
        }

        symbolTable.put(filteredUsersName, filteredUsers);

        return null;
    }

    private boolean compareValues(Object value1, String value2) {
        if (value1 instanceof String) {
            String name = (String) value1;
            return name.equalsIgnoreCase(value2);
        }
        return false;
    }

}
