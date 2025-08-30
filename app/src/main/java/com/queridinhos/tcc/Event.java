package com.queridinhos.tcc;

public class Event {
    private long id;
    private String title;
    private String description;
    private long startDate;

    public Event(long id, String title, String description, long startDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
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
}