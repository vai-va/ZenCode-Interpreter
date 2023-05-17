package edu.ktu.glang.interpreter.exception;

public class GLangVariableNotDeclaredException extends GLangException {
    public GLangVariableNotDeclaredException(String variableNme) {
        super(String.format("Variable '%s' is not declared.", variableNme));
    }
}
