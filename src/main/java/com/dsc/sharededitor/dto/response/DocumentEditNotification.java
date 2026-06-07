package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DocumentEditNotification {
    private final String type = "DOCUMENT_EDIT";
    private Long documentId;
    private String operation;
    private String username;
    private int lineNumber;
    private String content;
    private List<String> lines;
    private DocumentEditLogResponse logEntry;

    public DocumentEditNotification(Long documentId,
                                    String operation,
                                    String username,
                                    int lineNumber,
                                    String content,
                                    List<String> lines,
                                    DocumentEditLogResponse logEntry) {
        this.documentId = documentId;
        this.operation = operation;
        this.username = username;
        this.lineNumber = lineNumber;
        this.content = content;
        this.lines = lines;
        this.logEntry = logEntry;
    }
}
