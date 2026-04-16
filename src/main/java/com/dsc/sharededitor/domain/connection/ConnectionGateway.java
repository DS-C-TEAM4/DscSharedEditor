package com.dsc.sharededitor.domain.connection;

import com.dsc.sharededitor.domain.message.MessageParser;
import com.dsc.sharededitor.dto.message.BaseMessage;
import com.dsc.sharededitor.dto.message.MessageType;

public class ConnectionGateway {

    private final MessageParser messageParser;

    public ConnectionGateway(MessageParser messageParser) {
        this.messageParser = messageParser;
    }

    public MessageType resolveMessageType(String rawMessage) {
        BaseMessage baseMessage = messageParser.parseBaseMessage(rawMessage);
        return baseMessage.getType();
    }
}