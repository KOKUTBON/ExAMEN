package com.rei.examenbackend.dto.growth;

public class PdfExportResponse {
    private String message;

    public PdfExportResponse() {}

    public PdfExportResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
