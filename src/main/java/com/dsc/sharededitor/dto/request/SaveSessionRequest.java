package com.dsc.sharededitor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveSessionRequest {
    private String sessionId;
    private Long documentId;
    private String title;
    private List<SessionLine> lines;
    private String username;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionLine {
        private String lineId;
        private int lineNumber;
        private String text;
        private String editor;
    }
}
