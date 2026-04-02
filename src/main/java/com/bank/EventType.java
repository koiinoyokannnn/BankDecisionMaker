package com.bank;

public enum EventType {
    COMPLETING(0),   // Сначала завершаем обслуживание
    ARRIVAL(1);    // Последними — вспомогательные

    private final int priority;
    EventType(int p) { this.priority = p; }
    public int getPriority() { return priority; }
}
