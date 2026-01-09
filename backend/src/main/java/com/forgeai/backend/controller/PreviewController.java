package com.forgeai.backend.controller;

import com.forgeai.backend.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/projects")
public class PreviewController {

    private static final Logger logger = LoggerFactory.getLogger(PreviewController.class);
    private final FileStorageService fileStorageService;

    public PreviewController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{projectId}/preview/**")
    public ResponseEntity<Resource> previewProject(
            @PathVariable String projectId,
            HttpServletRequest request) {

        try {
            String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
            String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String subPath = new AntPathMatcher().extractPathWithinPattern(bestMatchPattern, fullPath);

            // Default to index.html
            if (subPath == null || subPath.isBlank()) {
                subPath = "index.html";
            }

            // Get canonical root
            Path projectRoot = fileStorageService.getProjectRoot(projectId).toAbsolutePath().normalize();

            // Resolve requested file
            Path requestedFile = projectRoot.resolve(subPath).normalize();

            // 🔐 SECURITY: Must stay inside project folder
            if (!requestedFile.startsWith(projectRoot)) {
                logger.warn("Blocked path traversal attempt: {}", subPath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!Files.exists(requestedFile) || Files.isDirectory(requestedFile)) {
                logger.info("File not found: {}", requestedFile);
                return ResponseEntity.notFound().build();
            }

            MediaType mediaType = getMediaTypeForPath(requestedFile);

            logger.info("Serving preview file: {}", requestedFile);

            Resource resource = new FileSystemResource(requestedFile);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Preview error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType getMediaTypeForPath(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".html"))
            return MediaType.TEXT_HTML;
        if (fileName.endsWith(".css"))
            return MediaType.parseMediaType("text/css");
        if (fileName.endsWith(".js"))
            return MediaType.parseMediaType("application/javascript");
        if (fileName.endsWith(".png"))
            return MediaType.IMAGE_PNG;
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
            return MediaType.IMAGE_JPEG;
        if (fileName.endsWith(".svg"))
            return MediaType.parseMediaType("image/svg+xml");

        try {
            String contentType = Files.probeContentType(path);
            if (contentType != null) {
                return MediaType.parseMediaType(contentType);
            }
        } catch (Exception e) {
            logger.warn("Could not probe content type for {}: {}", path, e.getMessage());
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * MANUAL VERIFICATION (using curl):
     * 1. Generate a project:
     * curl -X POST http://localhost:8080/api/generate \
     * -H "Content-Type: application/json" \
     * -d '{"projectName":"TestSite","description":"A simple test
     * site","category":"Web","sections":["hero","footer"],"tech":"vanilla"}'
     * (Note the projectId from response)
     * 
     * 2. Preview index.html:
     * curl http://localhost:8080/api/projects/{projectId}/preview
     * 
     * 3. Preview other files:
     * curl http://localhost:8080/api/projects/{projectId}/preview/style.css
     */
}
