package com.bank.entities;

/**Перечисление типов основных событий*/
public enum EventType {
    /**Событие <i>завершения</i> обслуживания клиента с приоритетом <b>0</b>*/
    COMPLETING(0),
    /**Событие <i>прихода</i> нового клиента с приоритетом <b>1</b>*/
    ARRIVAL(1);
    /**Неизменяемое значение приоритета*/
    private final int priority;
    /**Конструктор с назначением приоритета*/
    EventType(int p) { this.priority = p; }
    /**Геттер для получения приоритета конкретного события */
    public int getPriority() { return priority; }
}
