package com.dsc.sharededitor.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserStatusNotification {
    private final String type = "USER_STATUS";
    private String username;
    private String status;
    private String message;

    public UserStatusNotification(String username, String status, String message) {
        this.username = username;
        this.status = status;
        this.message = message;
    }
}
