package com.dsc.sharededitor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentLockResponse {
    private int lineNumber;
    private String clientId;
    private String username;
    private String targetClientId;
    private long timestamp;
}
