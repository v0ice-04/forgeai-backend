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

            // VALIDATE: Must have exactly 3 files: index.html, styles.css, script.js
            validateFiles(wrapper.files());

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
        return String.format(
                "You are ForgeAI, an expert senior web product designer and frontend architect.\n" +
                        "\n" +
                        "You generate complete production-quality websites using:\n" +
                        "- HTML\n" +
                        "- CSS\n" +
                        "- Vanilla JavaScript only\n" +
                        "\n" +
                        "📦 OUTPUT FORMAT (MANDATORY JSON)\n" +
                        "Always respond in this exact format:\n" +
                        "{\n" +
                        "  \"files\": [\n" +
                        "    { \"path\": \"index.html\", \"content\": \"...\" },\n" +
                        "    { \"path\": \"styles.css\", \"content\": \"...\" },\n" +
                        "    { \"path\": \"script.js\", \"content\": \"...\" }\n" +
                        "  ]\n" +
                        "}\n" +
                        "\n" +
                        "No markdown. No explanations. No backticks.\n" +
                        "\n" +
                        "🏗️ WEBSITE STRUCTURE (MANDATORY)\n" +
                        "Every website MUST include:\n" +
                        "- Sticky header with nav\n" +
                        "- Hero section with CTA\n" +
                        "- Features / Services section (3–6 cards)\n" +
                        "- About section\n" +
                        "- Benefits / Why Choose Us section\n" +
                        "- Call-to-action section\n" +
                        "- Footer with:\n" +
                        "  - Links\n" +
                        "  - Copyright\n" +
                        "  - Text: Built with ForgeAI\n" +
                        "\n" +
                        "🎨 DESIGN RULES\n" +
                        "Must look modern startup / SaaS quality\n" +
                        "Must include:\n" +
                        "- Cards\n" +
                        "- Shadows\n" +
                        "- Gradients\n" +
                        "- Hover animations\n" +
                        "- Scroll animations\n" +
                        "- Section reveal animations\n" +
                        "- Fully responsive\n" +
                        "\n" +
                        "🧠 CONTENT RULES\n" +
                        "Use the user's idea as the business theme\n" +
                        "Write realistic professional marketing content\n" +
                        "No lorem ipsum\n" +
                        "No placeholders\n" +
                        "No \"Hello world\"\n" +
                        "\n" +
                        "⚙️ INTERACTIVITY RULES\n" +
                        "Must include:\n" +
                        "- Smooth scrolling\n" +
                        "- Navbar scroll effect\n" +
                        "- Button hover animations\n" +
                        "- Section reveal animation\n" +
                        "- Simple JS animations\n" +
                        "\n" +
                        "🧱 TECH RULES\n" +
                        "❌ No React\n" +
                        "❌ No Tailwind\n" +
                        "❌ No Bootstrap\n" +
                        "❌ No frameworks\n" +
                        "❌ No external JS or CSS libraries\n" +
                        "❌ No CDN dependencies\n" +
                        "\n" +
                        "🏷️ BRANDING RULE\n" +
                        "If user asks who you are or who built this:\n" +
                        "Always say:\n" +
                        "I am ForgeAI, an AI website builder created by the ForgeAI team.\n" +
                        "\n" +
                        "🧨 HARD FAIL CONDITIONS\n" +
                        "Do NOT return code fences\n" +
                        "Do NOT return explanations\n" +
                        "Do NOT return markdown\n" +
                        "Do NOT return partial files\n" +
                        "\n" +
                        "PROJECT DETAILS:\n" +
                        "Name: %s\n" +
                        "Description: %s\n" +
                        "Category: %s\n" +
                        "Features/Sections: %s\n" +
                        "\n" +
                        "Use proper escaping for double quotes and newlines in the JSON content values.",
                request.getProjectName(),
                request.getDescription(),
                request.getCategory(),
                String.join(", ", request.getSections()));
    }

    public GenerateResponse editProject(String projectId, String userInstruction) {
        try {
            // Load current files
            List<GenerateResponse.GeneratedFile> currentFiles = fileStorageService.loadProjectFiles(projectId);
            
            // Build edit prompt
            String prompt = buildEditPrompt(currentFiles, userInstruction);
            
            // Generate updated files
            String aiResult = openRouterClient.generate(prompt);
            
            logger.info("===== RAW AI EDIT RESPONSE START =====");
            logger.info(aiResult);
            logger.info("===== RAW AI EDIT RESPONSE END =====");
            
            String cleanedJson = cleanAiJson(aiResult);
            GeneratedFilesWrapper wrapper = objectMapper.readValue(cleanedJson, GeneratedFilesWrapper.class);
            
            // VALIDATE: Must have exactly 3 files: index.html, styles.css, script.js
            validateFiles(wrapper.files());
            
            // SAVE UPDATED FILES TO DISK
            fileStorageService.saveProjectFiles(projectId, wrapper.files());
            
            // UPDATE ZIP
            zipService.zipProject(projectId);
            
            return new GenerateResponse(
                    true,
                    "Project updated successfully",
                    projectId,
                    wrapper.files());
        } catch (Exception e) {
            logger.error("PRODUCTION ERROR: Failed to edit project. Error: {}", e.getMessage());
            return new GenerateResponse(false, "Failed to edit project: " + e.getMessage(), projectId,
                    Collections.emptyList());
        }
    }

    private String buildEditPrompt(List<GenerateResponse.GeneratedFile> currentFiles, String userInstruction) {
        StringBuilder filesContext = new StringBuilder();
        for (GenerateResponse.GeneratedFile file : currentFiles) {
            filesContext.append("\n--- ").append(file.getPath()).append(" ---\n");
            filesContext.append(file.getContent());
            filesContext.append("\n");
        }
        
        return String.format(
                "You are ForgeAI, an expert senior web product designer and frontend architect.\n" +
                        "\n" +
                        "You generate complete production-quality websites using:\n" +
                        "- HTML\n" +
                        "- CSS\n" +
                        "- Vanilla JavaScript only\n" +
                        "\n" +
                        "📦 OUTPUT FORMAT (MANDATORY JSON)\n" +
                        "Always respond in this exact format:\n" +
                        "{\n" +
                        "  \"files\": [\n" +
                        "    { \"path\": \"index.html\", \"content\": \"...\" },\n" +
                        "    { \"path\": \"styles.css\", \"content\": \"...\" },\n" +
                        "    { \"path\": \"script.js\", \"content\": \"...\" }\n" +
                        "  ]\n" +
                        "}\n" +
                        "\n" +
                        "No markdown. No explanations. No backticks.\n" +
                        "\n" +
                        "🏗️ WEBSITE STRUCTURE (MANDATORY)\n" +
                        "Every website MUST include:\n" +
                        "- Sticky header with nav\n" +
                        "- Hero section with CTA\n" +
                        "- Features / Services section (3–6 cards)\n" +
                        "- About section\n" +
                        "- Benefits / Why Choose Us section\n" +
                        "- Call-to-action section\n" +
                        "- Footer with:\n" +
                        "  - Links\n" +
                        "  - Copyright\n" +
                        "  - Text: Built with ForgeAI\n" +
                        "\n" +
                        "🎨 DESIGN RULES\n" +
                        "Must look modern startup / SaaS quality\n" +
                        "Must include:\n" +
                        "- Cards\n" +
                        "- Shadows\n" +
                        "- Gradients\n" +
                        "- Hover animations\n" +
                        "- Scroll animations\n" +
                        "- Section reveal animations\n" +
                        "- Fully responsive\n" +
                        "\n" +
                        "🧠 CONTENT RULES\n" +
                        "Use the user's idea as the business theme\n" +
                        "Write realistic professional marketing content\n" +
                        "No lorem ipsum\n" +
                        "No placeholders\n" +
                        "No \"Hello world\"\n" +
                        "\n" +
                        "⚙️ INTERACTIVITY RULES\n" +
                        "Must include:\n" +
                        "- Smooth scrolling\n" +
                        "- Navbar scroll effect\n" +
                        "- Button hover animations\n" +
                        "- Section reveal animation\n" +
                        "- Simple JS animations\n" +
                        "\n" +
                        "🧱 TECH RULES\n" +
                        "❌ No React\n" +
                        "❌ No Tailwind\n" +
                        "❌ No Bootstrap\n" +
                        "❌ No frameworks\n" +
                        "❌ No external JS or CSS libraries\n" +
                        "❌ No CDN dependencies\n" +
                        "\n" +
                        "🏷️ BRANDING RULE\n" +
                        "If user asks who you are or who built this:\n" +
                        "Always say:\n" +
                        "I am ForgeAI, an AI website builder created by the ForgeAI team.\n" +
                        "\n" +
                        "🧨 HARD FAIL CONDITIONS\n" +
                        "Do NOT return code fences\n" +
                        "Do NOT return explanations\n" +
                        "Do NOT return markdown\n" +
                        "Do NOT return partial files\n" +
                        "\n" +
                        "CURRENT FILES:\n%s\n" +
                        "\n" +
                        "USER INSTRUCTION: %s\n" +
                        "\n" +
                        "Regenerate ALL files fully based on the user's instruction.\n" +
                        "Use proper escaping for double quotes and newlines in the JSON content values.",
                filesContext.toString(),
                userInstruction);
    }

    private void validateFiles(List<GenerateResponse.GeneratedFile> files) {
        if (files == null || files.size() != 3) {
            throw new RuntimeException(
                    "Invalid file count. Expected exactly 3 files (index.html, styles.css, script.js), but got: " +
                            (files == null ? "null" : files.size()));
        }

        boolean hasIndexHtml = false;
        boolean hasStylesCss = false;
        boolean hasScriptJs = false;

        for (GenerateResponse.GeneratedFile file : files) {
            String path = file.getPath().toLowerCase();
            if (path.equals("index.html")) {
                hasIndexHtml = true;
            } else if (path.equals("styles.css")) {
                hasStylesCss = true;
            } else if (path.equals("script.js")) {
                hasScriptJs = true;
            }
        }

        if (!hasIndexHtml || !hasStylesCss || !hasScriptJs) {
            throw new RuntimeException(
                    "Invalid files. Must contain exactly: index.html, styles.css, script.js. " +
                            "Found: " + files.stream()
                                    .map(f -> f.getPath())
                                    .reduce((a, b) -> a + ", " + b)
                                    .orElse("none"));
        }
    }

    private record GeneratedFilesWrapper(List<GenerateResponse.GeneratedFile> files) {
    }
}
