package com.queridinhos.tcc;

public class Event {
    private long id;
    private String title;
    private String description;
    private long startDate;
    private long endDate;
    private boolean isRecurring;

    public Event(long id, String title, String description, long startDate, long endDate, boolean isRecurring) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isRecurring = isRecurring;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public boolean isRecurring() {
        return isRecurring;
    }
}