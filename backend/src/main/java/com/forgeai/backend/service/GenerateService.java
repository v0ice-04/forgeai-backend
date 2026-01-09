package com.forgeai.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeai.backend.client.OpenRouterClient;
import com.forgeai.backend.dto.GenerateRequest;
import com.forgeai.backend.dto.GenerateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class GenerateService {

    private static final Logger logger = LoggerFactory.getLogger(GenerateService.class);
    private final ObjectMapper objectMapper;
    private final OpenRouterClient openRouterClient;
    private final FileStorageService fileStorageService;
    private final ZipService zipService;

    public GenerateService(ObjectMapper objectMapper, OpenRouterClient openRouterClient,
            FileStorageService fileStorageService, ZipService zipService) {
        this.objectMapper = objectMapper;
        this.openRouterClient = openRouterClient;
        this.fileStorageService = fileStorageService;
        this.zipService = zipService;
    }

    public GenerateResponse generateProject(GenerateRequest request) {
        String prompt = buildPrompt(request);
        String projectId = UUID.randomUUID().toString();

        try {
            String aiResult = openRouterClient.generate(prompt);

            logger.info("===== RAW AI RESPONSE START =====");
            logger.info(aiResult);
            logger.info("===== RAW AI RESPONSE END =====");

            String cleanedJson = cleanAiJson(aiResult);

            GeneratedFilesWrapper wrapper = objectMapper.readValue(cleanedJson, GeneratedFilesWrapper.class);

            // SAVE FILES TO DISK
            fileStorageService.saveProjectFiles(projectId, wrapper.files());

            // ZIP PROJECT
            zipService.zipProject(projectId);

            return new GenerateResponse(
                    true,
                    "Project generated successfully",
                    projectId,
                    wrapper.files());
        } catch (Exception e) {
            logger.error("PRODUCTION ERROR: Failed to generate or parse project. Error: {}", e.getMessage());
            return new GenerateResponse(false, "Failed to generate project: " + e.getMessage(), null,
                    Collections.emptyList());
        }
    }

    private String cleanAiJson(String raw) {
        if (raw == null)
            return "{}";

        String cleaned = raw.trim();

        // Remove markdown code blocks if present
        if (cleaned.contains("```")) {
            // Specifically look for ```json ... ```
            if (cleaned.contains("```json")) {
                int start = cleaned.indexOf("```json") + 7;
                int end = cleaned.lastIndexOf("```");
                if (end > start) {
                    cleaned = cleaned.substring(start, end).trim();
                }
            } else {
                // Handle generic ``` ... ```
                int start = cleaned.indexOf("```") + 3;
                int end = cleaned.lastIndexOf("```");
                if (end > start) {
                    cleaned = cleaned.substring(start, end).trim();
                }
            }
        }

        // If it still contains markdown characters or text outside the JSON, we can't
        // easily fix it here
        // without a more complex regex, but this handles the most common case.
        return cleaned;
    }

    private String buildPrompt(GenerateRequest request) {
        String techStack = "react".equalsIgnoreCase(request.getTech()) ? "React + Tailwind CSS" : "HTML + Vanilla CSS";

        return String.format(
                "You are an autonomous code generation engine.\n" +
                        "\n" +
                        "CRITICAL INSTRUCTION: You MUST return ONLY valid, raw JSON.\n" +
                        "- DO NOT include any markdown code blocks (no ```json or ```).\n" +
                        "- DO NOT include any explanations, greetings, or text before or after the JSON.\n" +
                        "- DO NOT include comments inside the JSON.\n" +
                        "- Provide the EXACT JSON structure below.\n" +
                        "\n" +
                        "REQUIRED JSON FORMAT:\n" +
                        "{\n" +
                        "  \"projectName\": \"%s\",\n" +
                        "  \"files\": [\n" +
                        "    {\n" +
                        "      \"path\": \"relative/path/to/file\",\n" +
                        "      \"content\": \"Full file content here\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n" +
                        "\n" +
                        "PROJECT DETAILS:\n" +
                        "Name: %s\n" +
                        "Description: %s\n" +
                        "Category: %s\n" +
                        "Features/Sections: %s\n" +
                        "Technology Stack: %s\n" +
                        "\n" +
                        "Strict Rules:\n" +
                        "1. All files must be complete and ready to deploy.\n" +
                        "2. Include index.html (or appropriate entry point for the stack).\n" +
                        "3. Ensure all code is professional and follows modern best practices.\n" +
                        "4. Use proper escaping for double quotes and newlines in the JSON content values.",
                request.getProjectName(),
                request.getProjectName(),
                request.getDescription(),
                request.getCategory(),
                String.join(", ", request.getSections()),
                techStack);
    }

    private record GeneratedFilesWrapper(String projectName, List<GenerateResponse.GeneratedFile> files) {
    }
}
