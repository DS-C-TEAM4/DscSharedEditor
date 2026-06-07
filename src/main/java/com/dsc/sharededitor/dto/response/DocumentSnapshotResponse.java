package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DocumentSnapshotResponse {
    private final String type = "DOCUMENT_SNAPSHOT";
    private Long documentId;
    private String title;
    private String ownerUsername;
    private List<String> lines;
    private String content;
    private List<String> members;
    private List<String> activeParticipants;
    private List<DocumentEditLogResponse> editLogs;
    private String topic;

    public DocumentSnapshotResponse(Long documentId,
                                    String title,
                                    String ownerUsername,
                                    List<String> lines,
                                    String content,
                                    List<String> members,
                                    List<String> activeParticipants,
                                    List<DocumentEditLogResponse> editLogs,
                                    String topic) {
        this.documentId = documentId;
        this.title = title;
        this.ownerUsername = ownerUsername;
        this.lines = lines;
        this.content = content;
        this.members = members;
        this.activeParticipants = activeParticipants;
        this.editLogs = editLogs;
        this.topic = topic;
    }
}
