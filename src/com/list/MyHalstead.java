package com.list;

/*
*************************************************************
*
n1 = distinct operators.
n2 = distinct operands.
N1 = total number of operators.
N2 = total number of operands.
Program Length      : N = N1 + N2
Program Vocabulary  : n = n1 + n2
Volume              : V = N * log2N
Difficulty          : D = (n1/2) * (N2/n2)
Effort              : E = D * V
Time Required       : T = E/18
Delivered Bugs      : B = E^(2/3) / 3000 or V / 3000
*
*************************************************************
 */

public class MyHalstead {
    private double distinctOpt, distinctOpr, numberOfOpt, numberOfOpr, programLength, programVocab,
            volume, difficulty, effort, timeRequired, deliveredBugs;

    public double getDistinctOpt() {
        return distinctOpt;
    }

    public void setDistinctOpt(double distinctOpt) {
        this.distinctOpt = distinctOpt;
    }

    public double getDistinctOpr() {
        return distinctOpr;
    }

    public void setDistinctOpr(double distinctOpr) {
        this.distinctOpr = distinctOpr;
    }

    public double getNumberOfOpt() {
        return numberOfOpt;
    }

    public void setNumberOfOpt(double numberOfOpt) {
        this.numberOfOpt = numberOfOpt;
    }

    public double getNumberOfOpr() {
        return numberOfOpr;
    }

    public void setNumberOfOpr(double numberOfOpr) {
        this.numberOfOpr = numberOfOpr;
    }

    public double getProgramLength() {
        return programLength;
    }

    public void setProgramLength(double programLength) {
        this.programLength = programLength;
    }

    public double getProgramVocab() {
        return programVocab;
    }

    public void setProgramVocab(double programVocab) {
        this.programVocab = programVocab;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getEffort() {
        return effort;
    }

    public void setEffort(double effort) {
        this.effort = effort;
    }

    public double getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(double timeRequired) {
        this.timeRequired = timeRequired;
    }

    public double getDeliveredBugs() {
        return deliveredBugs;
    }

    public void setDeliveredBugs(double deliveredBugs) {
        this.deliveredBugs = deliveredBugs;
    }

    @Override
    public String toString() {
        return "MyHalstead{" +
                "distinctOpt=" + distinctOpt +
                ", distinctOpr=" + distinctOpr +
                ", numberOfOpt=" + numberOfOpt +
                ", numberOfOpr=" + numberOfOpr +
                ", programLength=" + programLength +
                ", programVocab=" + programVocab +
                ", volume=" + volume +
                ", difficulty=" + difficulty +
                ", effort=" + effort +
                ", timeRequired=" + timeRequired +
                ", deliveredBugs=" + deliveredBugs +
                '}';
    }
}
