package com.dsc.sharededitor.controller;

import com.dsc.sharededitor.dto.request.SaveSessionRequest;
import com.dsc.sharededitor.dto.response.DocumentSaveNotification;
import com.dsc.sharededitor.dto.response.SaveSessionResponse;
import com.dsc.sharededitor.dto.response.SavedFileInfoResponse;
import com.dsc.sharededitor.service.DocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

@RestController
@RequestMapping("/api/sessions")
public class SessionRestController {

    private final DocumentService documentService;
    private final SimpMessagingTemplate messagingTemplate;

    public SessionRestController(DocumentService documentService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.documentService = documentService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/save")
    public SaveSessionResponse save(@RequestBody SaveSessionRequest request) {
        Long documentId = request.getDocumentId();
        if (documentId == null) {
            throw new IllegalArgumentException("documentId가 필요합니다.");
        }
        documentService.saveDocument(documentId, request.getUsername(), request.getLines());
        try {
            messagingTemplate.convertAndSend(
                    "/topic/documents/" + documentId,
                    new DocumentSaveNotification(
                            documentId,
                            request.getUsername(),
                            request.getUsername() + "님이 문서를 저장했습니다.",
                            Instant.now().toString()
                    )
            );
        } catch (RuntimeException ex) {
            // 저장 성공을 알림 실패 때문에 막지 않는다.
        }
        return new SaveSessionResponse(
                true,
                "document-" + documentId + ".json",
                "세션을 파일로 저장했습니다."
        );
    }

    @GetMapping("/saved")
    public java.util.List<SavedFileInfoResponse> savedFiles() {
        return documentService.listSavedFiles();
    }
}
