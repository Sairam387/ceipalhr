package com.smarthr.controller;

import com.smarthr.entity.EmployeeDocument;
import com.smarthr.repository.EmployeeDocumentRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/employee-documents")
@CrossOrigin(origins = "*")
public class EmployeeDocumentController {

    private final EmployeeDocumentRepository documentRepository;

    private final Path uploadDirectory =
            Paths.get("uploads", "employee-documents")
                    .toAbsolutePath()
                    .normalize();

    public EmployeeDocumentController(
            EmployeeDocumentRepository documentRepository) {

        this.documentRepository = documentRepository;

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Unable to create document upload directory",
                    e
            );
        }
    }

    // =========================================================
    // GET DOCUMENTS FOR EMPLOYEE
    // =========================================================

    @GetMapping("/{employeeId}")
    public List<EmployeeDocument> getEmployeeDocuments(
            @PathVariable Long employeeId) {

        return documentRepository
                .findByEmployeeIdOrderByUploadedAtDesc(employeeId);
    }

    // =========================================================
    // UPLOAD DOCUMENT
    // =========================================================

    @PostMapping(
            value = "/upload/{employeeId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public EmployeeDocument uploadDocument(
            @PathVariable Long employeeId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Please select a document"
            );
        }

        if (documentType == null ||
                documentType.trim().isEmpty()) {

            throw new RuntimeException(
                    "Document type is required"
            );
        }

        try {

            Path employeeDirectory =
                    uploadDirectory.resolve(
                            String.valueOf(employeeId)
                    );

            Files.createDirectories(employeeDirectory);

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null ||
                    originalFileName.trim().isEmpty()) {

                originalFileName = "document";
            }

            originalFileName =
                    Paths.get(originalFileName)
                            .getFileName()
                            .toString();

            String storedFileName =
                    UUID.randomUUID()
                            + "_"
                            + originalFileName;

            Path targetFile =
                    employeeDirectory.resolve(
                            storedFileName
                    );

            Files.copy(
                    file.getInputStream(),
                    targetFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            EmployeeDocument document =
                    new EmployeeDocument();

            document.setEmployeeId(employeeId);

            document.setDocumentType(
                    documentType.trim()
            );

            document.setFileName(
                    originalFileName
            );

            document.setFilePath(
                    targetFile.toString()
            );

            document.setContentType(
                    file.getContentType()
            );

            document.setFileSize(
                    file.getSize()
            );

            document.setUploadedAt(
                    LocalDateTime.now()
            );

            return documentRepository.save(document);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Document upload failed",
                    e
            );
        }
    }

    // =========================================================
    // DOWNLOAD DOCUMENT
    // =========================================================

    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long documentId) {

        EmployeeDocument document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"
                                )
                        );

        try {

            Path filePath =
                    Paths.get(
                            document.getFilePath()
                    )
                    .toAbsolutePath()
                    .normalize();

            if (!Files.exists(filePath)) {

                throw new RuntimeException(
                        "Document file not found"
                );
            }

            Resource resource =
                    new UrlResource(
                            filePath.toUri()
                    );

            String contentType =
                    document.getContentType();

            if (contentType == null ||
                    contentType.isBlank()) {

                contentType =
                        "application/octet-stream";
            }

            ContentDisposition disposition =
                    ContentDisposition
                            .attachment()
                            .filename(
                                    document.getFileName()
                            )
                            .build();

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    contentType
                            )
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            disposition.toString()
                    )
                    .body(resource);

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "Unable to load document",
                    e
            );
        }
    }

    // =========================================================
    // DELETE DOCUMENT
    // =========================================================

    @DeleteMapping("/{documentId}")
    public String deleteDocument(
            @PathVariable Long documentId) {

        EmployeeDocument document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document not found"
                                )
                        );

        try {

            Path filePath =
                    Paths.get(
                            document.getFilePath()
                    )
                    .toAbsolutePath()
                    .normalize();

            Files.deleteIfExists(filePath);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Unable to delete document file",
                    e
            );
        }

        documentRepository.delete(document);

        return "Document deleted successfully";
    }
}