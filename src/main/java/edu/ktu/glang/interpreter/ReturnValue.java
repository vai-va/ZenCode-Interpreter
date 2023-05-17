package edu.ktu.glang.interpreter;

public record ReturnValue(Object value) {

    public Object getValue() {
        return value;
    }
}
