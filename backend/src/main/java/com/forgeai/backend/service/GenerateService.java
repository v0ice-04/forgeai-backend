package com.forgeai.backend.service;

import com.forgeai.backend.dto.GenerateRequest;
import com.forgeai.backend.dto.GenerateResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GenerateService {

    public GenerateResponse generateProject(GenerateRequest request) {
        // Simulate project generation logic
        String projectId = UUID.randomUUID().toString();
        return new GenerateResponse(true, "Project generation started", projectId);
    }
}
