package com.forgeai.backend.controller;

import com.forgeai.backend.dto.GenerateResponse;
import com.forgeai.backend.service.GenerateService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class EditController {

    private final GenerateService generateService;

    public EditController(GenerateService generateService) {
        this.generateService = generateService;
    }

    @PostMapping("/{projectId}/edit")
    public GenerateResponse editProject(
            @PathVariable String projectId,
            @RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "");
        return generateService.editProject(projectId, message);
    }
}
