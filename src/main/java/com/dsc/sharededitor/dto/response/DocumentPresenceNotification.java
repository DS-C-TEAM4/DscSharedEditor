package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DocumentPresenceNotification {
    private final String type = "DOCUMENT_PRESENCE";
    private Long documentId;
    private String username;
    private String status;
    private String message;
    private List<String> activeParticipants;

    public DocumentPresenceNotification(Long documentId,
                                        String username,
                                        String status,
                                        String message,
                                        List<String> activeParticipants) {
        this.documentId = documentId;
        this.username = username;
        this.status = status;
        this.message = message;
        this.activeParticipants = activeParticipants;
    }
}
