package com.dsc.sharededitor.exception;

public class DocumentNotFoundException extends RuntimeException {

    public DocumentNotFoundException(Long documentId) {
        super("문서를 찾을 수 없습니다. documentId=" + documentId);
    }
}
