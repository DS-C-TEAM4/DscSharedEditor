package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.dto.request.SaveSessionRequest;
import com.dsc.sharededitor.dto.response.SaveSessionResponse;
import com.dsc.sharededitor.service.DocumentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionRestController {

    private final DocumentService documentService;

    public SessionRestController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/save")
    public SaveSessionResponse save(@RequestBody SaveSessionRequest request) {
        Long documentId = request.getDocumentId();
        if (documentId == null) {
            throw new IllegalArgumentException("documentId가 필요합니다.");
        }
        documentService.saveDocument(documentId, request.getUsername(), request.getLines());
        return new SaveSessionResponse(
                true,
                "document-" + documentId + ".json",
                "세션을 파일로 저장했습니다."
        );
    }
}
