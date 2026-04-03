package com.bank.entities;

import lombok.Getter;
import lombok.Setter;


/**
 * Класс события. Имплементирует Comparable<Event> для будущей сортировки
 * очереди событий.
 */
public class Event implements Comparable<Event>{
    /**Содержит тип события (Arrival/Completion)*/
    @Getter @Setter
    private EventType type;
    /**Время наступления события*/
    @Getter @Setter
    private double time;
    /**Клиент связанный с данным событием (Может быть <i>null</i> для ARRIVAL*/
    @Getter @Setter
    private Client client;
    /**Оператор связанный с данным событием (Для COMPLETION)*/
    @Getter @Setter
    private Operator operator;

    /**Конструктор*/
    public Event(EventType eventType, double time, Client client, Operator operator) {
        this.type = eventType;
        this.time = time;
        this.client = client;
        this.operator = operator;
    }
    /** Сравнение для сортировки. Сначала по времени, потом по приоритету (чтобы COMPLETING раньше ARRIVAL при одинаковом времени)*/
    @Override
    public int compareTo(Event o) {
        int timeCompare = Double.compare(this.time, o.time);
        return timeCompare != 0 ? timeCompare :
            Integer.compare(this.type.getPriority(), o.type.getPriority());
    }
}
