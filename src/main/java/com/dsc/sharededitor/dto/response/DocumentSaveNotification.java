package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentSaveNotification {
    private final String type = "DOCUMENT_SAVE";
    private Long documentId;
    private String username;
    private String message;
    private String timestamp;

    public DocumentSaveNotification(Long documentId, String username, String message, String timestamp) {
        this.documentId = documentId;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }
}
