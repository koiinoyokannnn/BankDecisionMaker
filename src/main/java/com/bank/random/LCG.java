package com.bank.random;

/**
 * Класс LCG (Linear Congruential Generator — Линейный конгруэнтный метод)
 *  — это простой и быстрый алгоритм генерации псевдослучайных чисел, 
 * используемый для создания последовательностей, имитирующих случайность.
*/
public class LCG {
    private long next;
    private long a = 1103515245L;
    private long c = 12345L;
    private long m = 2147483648L;

    public LCG(long seed) {
        this.next = seed;
    }

    public LCG(long seed, long a, long c, long m) {
        this.next = seed;
        this.a = a;
        this.c = c;
        this.m = m;
    }

    public float getRand() {
        next = (next * a + c) % m;
        return ((float)next / m);
    }

    public void setSeed(long seed) {
        this.next = seed;
    }

    public float[] generateSequence(int n) {
        float[] sequence = new float[n];
        for (int i = 0; i < n; i++) {
            sequence[i] = getRand();
        }
        return sequence;
    }
}