package com.dsc.sharededitor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DocumentLineUpdateRequest {
    private int lineNumber;
    private String text;
    private Boolean logEdit;

    public DocumentLineUpdateRequest(int lineNumber, String text) {
        this(lineNumber, text, null);
    }

    public DocumentLineUpdateRequest(int lineNumber, String text, Boolean logEdit) {
        this.lineNumber = lineNumber;
        this.text = text;
        this.logEdit = logEdit;
    }
}
