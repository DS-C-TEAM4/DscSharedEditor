package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SaveSessionResponse {
    private boolean success;
    private String fileName;
    private String message;

    public SaveSessionResponse(boolean success, String fileName, String message) {
        this.success = success;
        this.fileName = fileName;
        this.message = message;
    }
}
