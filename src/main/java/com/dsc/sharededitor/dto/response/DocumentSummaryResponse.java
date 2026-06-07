package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DocumentSummaryResponse {
    private Long documentId;
    private String title;
    private String ownerUsername;
    private int lineCount;
    private int memberCount;
    private int activeParticipantCount;
    private String topic;

    public DocumentSummaryResponse(Long documentId,
                                   String title,
                                   String ownerUsername,
                                   int lineCount,
                                   int memberCount,
                                   int activeParticipantCount,
                                   String topic) {
        this.documentId = documentId;
        this.title = title;
        this.ownerUsername = ownerUsername;
        this.lineCount = lineCount;
        this.memberCount = memberCount;
        this.activeParticipantCount = activeParticipantCount;
        this.topic = topic;
    }
}
