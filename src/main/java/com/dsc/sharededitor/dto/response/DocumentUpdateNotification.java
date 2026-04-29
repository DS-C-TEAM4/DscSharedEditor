package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentUpdateNotification {
    private final String type = "DOCUMENT_UPDATE";
    private String documentId;
    private String operation;
    private String username;
    private String content;

    public DocumentUpdateNotification(String documentId, String operation, String username, String content) {
        this.documentId = documentId;
        this.operation = operation;
        this.username = username;
        this.content = content;
    }
}
