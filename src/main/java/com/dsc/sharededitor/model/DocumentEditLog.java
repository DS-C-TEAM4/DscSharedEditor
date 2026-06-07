package com.dsc.sharededitor.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class DocumentEditLog {

    private final long sequence;
    private final Instant timestamp;
    private final String username;
    private final String operation;
    private final int lineNumber;
    private final String beforeText;
    private final String afterText;

    public DocumentEditLog(long sequence,
                           String username,
                           String operation,
                           int lineNumber,
                           String beforeText,
                           String afterText) {
        this.sequence = sequence;
        this.timestamp = Instant.now();
        this.username = username;
        this.operation = operation;
        this.lineNumber = lineNumber;
        this.beforeText = beforeText;
        this.afterText = afterText;
    }
}
