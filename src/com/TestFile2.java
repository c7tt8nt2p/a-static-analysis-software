package com;


import com.list.MyPair;
import javafx.util.Pair;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class TestFile2 {


    public static void main(String[] args) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        System.out.println(formatter.format(4.0));


        double answer;
        answer = ((Math.pow(8601.319566, 0.667)) / 3000.0);
        System.out.println(formatter.format(answer));

    }

    public static double roundToThreePlaces(double d) {

        return Math.round(d * 100) / 100.0;
    }

}
