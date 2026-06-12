package com.dsc.sharededitor.dto.response;

public record SavedFileInfoResponse(
        String fileName,
        Long documentId,
        String title,
        String updatedAt
) {
}
