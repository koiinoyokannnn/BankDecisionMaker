package com.bank.random;

/**Класс для получения псевдослучайного числа с пуассоновским распределением*/
public final class PoissonDistribution {
    public static int get(double lambda, LCG lcg) {
        if (lambda <= 0) throw new IllegalArgumentException("Лямбда должна быть положительной");
        var l = Math.exp(-lambda);
        var k = 0;
        var p = 1.0;
        while (p > l) {
            k++;
            p *= lcg.getRand();
        }
        return k - 1;
    }
}
