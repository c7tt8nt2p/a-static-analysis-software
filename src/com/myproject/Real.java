package com.myproject;

class Real extends Token {
    private final double value;

    Real(String type, double value) {
        super(type, value + "");
        this.value = value;
    }

    @Override
    public String toString() {
        return super.toString() + ", Value : " + value;
    }
}
