package com.dsc.sharededitor.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Document {

    private final String documentId;

    @Getter(lombok.AccessLevel.NONE)
    private final StringBuilder content = new StringBuilder();

    public synchronized void insert(int position, String text) {
        int clamped = Math.max(0, Math.min(position, content.length()));
        content.insert(clamped, text);
    }

    public synchronized String getContent() {
        return content.toString();
    }
}
