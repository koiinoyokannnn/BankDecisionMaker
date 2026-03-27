package com.bank.random;

public class ExponentialDistribution {
    public static double Get(double lambda, LCG lcg) {
        if (lambda <= 0) throw new IllegalArgumentException("Лямбда должна быть положительной");
        return - Math.log(lcg.getRand()) / lambda;
    }
}
