package edu.ktu.glang.interpreter.exception;

public class GLangVariableAlreadyDeclaredException extends GLangException {
    public GLangVariableAlreadyDeclaredException(String variableNme) {
        super(String.format("Variable '%s' is already declared.", variableNme));
    }
}