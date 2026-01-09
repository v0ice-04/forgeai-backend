package com.forgeai.backend.service;

import com.forgeai.backend.dto.GenerateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final String BASE_FOLDER = "generated";

    public void saveProjectFiles(String projectId, List<GenerateResponse.GeneratedFile> files) {
        Path projectDirPath = Paths.get(BASE_FOLDER, projectId);

        try {
            // 1. Create Base Folder and Project Folder
            Files.createDirectories(projectDirPath);
            logger.info("Created directory: {}", projectDirPath.toAbsolutePath());

            // 2. Write Files
            for (GenerateResponse.GeneratedFile file : files) {
                Path filePath = projectDirPath.resolve(file.getPath());

                // Ensure parent directories for the file exist (nested paths)
                Files.createDirectories(filePath.getParent());

                Files.writeString(filePath, file.getContent());
                logger.info("Saved: {}/{}", BASE_FOLDER, projectId + "/" + file.getPath());
            }
        } catch (IOException e) {
            logger.error("Failed to save project files for projectId {}: {}", projectId, e.getMessage());
            throw new RuntimeException("Could not save generated files", e);
        }
    }

    public Path getProjectRoot(String projectId) {
        return Paths.get(BASE_FOLDER, projectId);
    }

    public Path getBaseFolder() {
        return Paths.get(BASE_FOLDER);
    }
}
