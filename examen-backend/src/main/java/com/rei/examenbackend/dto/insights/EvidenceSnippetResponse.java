package com.rei.examenbackend.dto.insights;

import java.time.LocalDate;

public class EvidenceSnippetResponse {
    private String source;
    private LocalDate date;
    private String snippet;
    private String rationale;

    public EvidenceSnippetResponse() {}

    public EvidenceSnippetResponse(String source, LocalDate date, String snippet, String rationale) {
        this.source = source;
        this.date = date;
        this.snippet = snippet;
        this.rationale = rationale;
    }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public String getRationale() { return rationale; }
    public void setRationale(String rationale) { this.rationale = rationale; }
}
