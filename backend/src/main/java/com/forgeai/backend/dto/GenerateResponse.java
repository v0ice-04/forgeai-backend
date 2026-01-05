package com.forgeai.backend.dto;

public class GenerateResponse {
    private boolean success;
    private String message;
    private String projectId;

    public GenerateResponse(boolean success, String message, String projectId) {
        this.success = success;
        this.message = message;
        this.projectId = projectId;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
