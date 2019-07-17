package com.appspot.autoanom.core.events;

public class LogMessageEvent {

    private final String data;

    public LogMessageEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}