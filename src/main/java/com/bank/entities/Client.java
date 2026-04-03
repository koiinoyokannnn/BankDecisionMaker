package com.bank.entities;

import com.bank.random.LCG;
import com.bank.random.PoissonDistribution;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Класс клиента с полями <b>id</b>, <b>serviceTime</b> и <b>arrivalTime</b>
*/
@Data
public class Client {
    /**Поле id*/
    private int id;
    /**Поле времени прибытия*/
    @Getter @Setter
    private double arrivalTime;
    /**Поле времени обслуживания*/
    @Getter @Setter
    private double serviceTime;
    /**Поле времени соответстующего типа операции*/
    @Getter @Setter
    private int operationType;
    /**Поле времени ожидания в очереди*/
    @Getter @Setter
    private double waitTime;
    /**Поле полного времени нахождения в банке*/
    @Getter @Setter
    private double fullSpentTime;

    /**Конструктор с инициализацией id, времени прибытия и времени обслуживания*/
    public Client(int id, int arrivalTime, int serviceTime, int operationType) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.operationType = operationType;
    }
}
