package com.bank.random;

/**Класс для получения псевдослучайного числа с экспоненциальным распределением*/
public class ExponentialDistribution {
    public static double get(double lambda, LCG lcg) {
        if (lambda <= 0) throw new IllegalArgumentException("Лямбда должна быть положительной");
        return - Math.log(lcg.getRand()) / lambda;
    }
}
