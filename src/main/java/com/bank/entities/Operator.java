package com.bank.entities;

import lombok.Data;

/**Класс оператора*/
@Data
public class Operator {
    /**Данное поле дает знать - занят в данный момент конкретный оператор или нет*/
    private boolean isBusy;
    /**Число с плавающей точкой - содержит время начала обслуживания конкретного клиента*/
    private double serviceStartTime;
}
