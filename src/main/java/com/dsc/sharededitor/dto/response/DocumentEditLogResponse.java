package com.dsc.sharededitor.dto.response;

import com.dsc.sharededitor.model.DocumentEditLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class DocumentEditLogResponse {
    private long sequence;
    private Instant timestamp;
    private String username;
    private String operation;
    private int lineNumber;
    private String beforeText;
    private String afterText;

    public DocumentEditLogResponse(DocumentEditLog log) {
        this.sequence = log.getSequence();
        this.timestamp = log.getTimestamp();
        this.username = log.getUsername();
        this.operation = log.getOperation();
        this.lineNumber = log.getLineNumber();
        this.beforeText = log.getBeforeText();
        this.afterText = log.getAfterText();
    }
}
