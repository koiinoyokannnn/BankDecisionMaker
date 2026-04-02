package com.bank;
import lombok.Data;

/**Класс <b>Configutarion</b> нужен для определения общей конфигурации модельной системы*/
@Data
public class Configuration {
    /**Базовые вероятности возникновения каждой из 5-и типов операций*/
    private double[] probabilities;
    /**Длительность соответствующих видов операций*/
    private int[] durations;
    /**Число, которое используется для инициализации генератора псевдослучайных чисел (ГПСЧ)*/
    private int seed;
    /**Параметр интенсивности (скорости), определяющий среднее число событий, происходящих в единицу времени*/
    private int lambda = 200;
    /**Булевое значение - выбор варианта (Одиночные или разделенные очереди)*/
    private boolean isSingleQueue = false;
    /**Время общей симуляции в секундах*/
    private int simulationTimeSeconds;

    /**Конструктор*/
    public Configuration(double[] probabilities, int[] durations, int seed, int lambda, boolean isSingleQueue, int simulationTimeSeconds) {
        this.probabilities = probabilities;
        this.durations = durations;
        this.seed = seed;
        this.lambda = lambda;
        this.isSingleQueue = isSingleQueue;
        this.simulationTimeSeconds = simulationTimeSeconds;
    }


}
