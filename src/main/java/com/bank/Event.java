package com.bank;

import java.util.Comparator;


/**
 * Класс события
 */
public class Event implements Comparable<Event>{
    private EventType type;
    private int duration;
    private int startTime;
    @Override
    public int compareTo(Event o) {
        // TODO Auto-generated method stub
        return Integer.compare(this.startTime, o.startTime);
    }
}

enum EventType {
    ARRIVAL,
    QUEUE_SELECTION,
    STANDING,
    SERVICE,
    COMPLETING
}
