package com.myproject;

class Num extends Token {
    private final int value;

    Num(String type, int value) {
        super(type, value + "");
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ", Value : " + value;
    }
}
