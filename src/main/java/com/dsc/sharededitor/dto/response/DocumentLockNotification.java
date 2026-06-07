package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentLockNotification {
    private final String type = "DOCUMENT_LOCK";
    private Long documentId;
    private String mode;
    private int lineNumber;
    private String clientId;
    private String username;
    private String targetClientId;
    private long timestamp;

    public DocumentLockNotification(Long documentId,
                                    String mode,
                                    int lineNumber,
                                    String clientId,
                                    String username,
                                    String targetClientId,
                                    long timestamp) {
        this.documentId = documentId;
        this.mode = mode;
        this.lineNumber = lineNumber;
        this.clientId = clientId;
        this.username = username;
        this.targetClientId = targetClientId;
        this.timestamp = timestamp;
    }
}
