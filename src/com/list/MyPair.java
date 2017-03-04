package com.list;


public class MyPair<L, R> {
    private final L l;
    private R r;

    public MyPair(L l, R r) {
        this.l = l;
        this.r = r;
    }

    public L getL() {
        return l;
    }

    public R getR() {
        return r;
    }

    public void setR(R r) {
        this.r = r;
    }
}
