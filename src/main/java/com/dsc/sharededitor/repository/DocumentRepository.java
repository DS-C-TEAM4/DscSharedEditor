package com.dsc.sharededitor.repository;

import com.dsc.sharededitor.model.Document;
import com.dsc.sharededitor.model.DocumentEditLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Repository
public class DocumentRepository {

    private final ObjectMapper objectMapper;
    private final Path storageDir;
    private final AtomicLong idSequence = new AtomicLong(1L);
    private final Map<Long, Document> documents = new ConcurrentHashMap<>();

    public DocumentRepository(ObjectMapper objectMapper,
                              @Value("${app.storage.documents-dir:./data/documents}") String storageDir) {
        this.objectMapper = objectMapper;
        this.storageDir = Path.of(storageDir);
    }

    @PostConstruct
    void loadDocuments() {
        try {
            Files.createDirectories(storageDir);
            try (Stream<Path> stream = Files.list(storageDir)) {
                stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                        .sorted(Comparator.comparing(Path::toString))
                        .forEach(this::loadDocumentFile);
            }

            if (documents.isEmpty()) {
                Document seed = new Document(idSequence.getAndIncrement(), "공유 문서", "system");
                documents.put(seed.getDocumentId(), seed);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("문서 저장소를 초기화할 수 없습니다.", ex);
        }
    }

    public synchronized Document create(String title, String ownerUsername) {
        Long documentId = idSequence.getAndIncrement();
        Document document = new Document(documentId, title, ownerUsername);
        documents.put(documentId, document);
        writeDocument(document);
        return document;
    }

    public Optional<Document> findById(Long documentId) {
        return Optional.ofNullable(documents.get(documentId));
    }

    public boolean exists(Long documentId) {
        return documents.containsKey(documentId);
    }

    public List<Document> findAll() {
        return new ArrayList<>(documents.values());
    }

    public synchronized void save(Document document) {
        documents.put(document.getDocumentId(), document);
        writeDocument(document);
    }

    private void loadDocumentFile(Path file) {
        try {
            PersistedDocument persisted = objectMapper.readValue(file.toFile(), PersistedDocument.class);
            Document document = persisted.toDocument();
            documents.put(document.getDocumentId(), document);
            idSequence.accumulateAndGet(document.getDocumentId() + 1, Math::max);
        } catch (IOException ex) {
            throw new UncheckedIOException("문서 파일을 읽을 수 없습니다: " + file, ex);
        }
    }

    private void writeDocument(Document document) {
        PersistedDocument persisted = PersistedDocument.from(document);
        Path target = storageDir.resolve(fileName(document.getDocumentId()));
        Path tempFile = storageDir.resolve(fileName(document.getDocumentId()) + ".tmp");

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), persisted);
            try {
                Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(tempFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("문서를 저장할 수 없습니다: " + document.getDocumentId(), ex);
        }
    }

    private String fileName(Long documentId) {
        return "document-" + documentId + ".json";
    }

    public record PersistedDocument(
            Long documentId,
            String title,
            String ownerUsername,
            Set<String> members,
            List<String> lines,
            List<PersistedEditLog> editLogs,
            long nextLogSequence
    ) {
        static PersistedDocument from(Document document) {
            List<PersistedEditLog> logs = document.getEditLogs().stream()
                    .map(PersistedEditLog::from)
                    .toList();
            return new PersistedDocument(
                    document.getDocumentId(),
                    document.getTitle(),
                    document.getOwnerUsername(),
                    Set.copyOf(document.getMembers()),
                    document.getLines(),
                    logs,
                    document.getNextLogSequence()
            );
        }

        Document toDocument() {
            Collection<DocumentEditLog> logs = editLogs == null
                    ? List.of()
                    : editLogs.stream().map(PersistedEditLog::toDocumentEditLog).toList();
            return new Document(
                    documentId,
                    title,
                    ownerUsername,
                    members,
                    lines,
                    logs,
                    nextLogSequence
            );
        }
    }

    public record PersistedEditLog(
            long sequence,
            Instant timestamp,
            String username,
            String operation,
            int lineNumber,
            String beforeText,
            String afterText
    ) {
        static PersistedEditLog from(DocumentEditLog log) {
            return new PersistedEditLog(
                    log.getSequence(),
                    log.getTimestamp(),
                    log.getUsername(),
                    log.getOperation(),
                    log.getLineNumber(),
                    log.getBeforeText(),
                    log.getAfterText()
            );
        }

        DocumentEditLog toDocumentEditLog() {
            return new DocumentEditLog(
                    sequence,
                    timestamp,
                    username,
                    operation,
                    lineNumber,
                    beforeText,
                    afterText
            );
        }
    }
}
