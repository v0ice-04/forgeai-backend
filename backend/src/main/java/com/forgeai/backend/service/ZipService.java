package com.forgeai.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    private static final Logger logger = LoggerFactory.getLogger(ZipService.class);
    private static final String BASE_FOLDER = "generated";

    public void zipProject(String projectId) {
        Path sourceDirPath = Paths.get(BASE_FOLDER, projectId);
        Path zipFilePath = Paths.get(BASE_FOLDER, projectId + ".zip");

        if (!Files.exists(sourceDirPath)) {
            logger.error("Source directory does not exist: {}", sourceDirPath.toAbsolutePath());
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)) {

            Files.walkFileTree(sourceDirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Create relative path for the zip entry
                    String relativePath = sourceDirPath.relativize(file).toString().replace("\\", "/");
                    zos.putNextEntry(new ZipEntry(relativePath));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // Add directory entry to zip if it's not the root source dir
                    if (!sourceDirPath.equals(dir)) {
                        String relativePath = sourceDirPath.relativize(dir).toString().replace("\\", "/") + "/";
                        zos.putNextEntry(new ZipEntry(relativePath));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            logger.info("Project zipped successfully: {}", zipFilePath.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to zip project {}: {}", projectId, e.getMessage());
            throw new RuntimeException("Could not zip project", e);
        }
    }
}
