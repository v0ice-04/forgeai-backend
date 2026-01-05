package com.forgeai.backend.controller;

import com.forgeai.backend.dto.GenerateRequest;
import com.forgeai.backend.dto.GenerateResponse;
import com.forgeai.backend.service.GenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GenerateController {

    private final GenerateService generateService;

    @Autowired
    public GenerateController(GenerateService generateService) {
        this.generateService = generateService;
    }

    @PostMapping("/generate")
    public GenerateResponse generate(@RequestBody GenerateRequest request) {
        return generateService.generateProject(request);
    }
}
