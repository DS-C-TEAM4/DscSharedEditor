package com.dsc.sharededitor.domain.message;

import com.dsc.sharededitor.dto.message.BaseMessage;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class MessageParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BaseMessage parseBaseMessage(String rawMessage) {
        try {
            return objectMapper.readValue(rawMessage, BaseMessage.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("메시지 파싱에 실패했습니다.", e);
        }
    }

    public <T> T parseMessage(String rawMessage, Class<T> messageType) {
        try {
            return objectMapper.readValue(rawMessage, messageType);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("메시지 파싱에 실패했습니다.", e);
        }
    }
}
