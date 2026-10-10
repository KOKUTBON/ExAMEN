package com.rei.examenbackend.dto.insights;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class WeeklyNarrativeResponse {
    private String title;
    private String narrative;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private int sessionsAnalyzed;
    private Double averageFeeling;
    private List<String> virtuesObserved;
    private List<String> strugglesIdentified;
    private List<String> gratitudeThemes;
    private List<String> stressorSignals;
    private List<String> actionableAdvice;
    private LocalDateTime generatedAt;

    public WeeklyNarrativeResponse() {}

    public WeeklyNarrativeResponse(String title, String narrative, LocalDate periodStart, LocalDate periodEnd,
                                   int sessionsAnalyzed, Double averageFeeling, List<String> virtuesObserved,
                                   List<String> strugglesIdentified, List<String> gratitudeThemes,
                                   List<String> stressorSignals, List<String> actionableAdvice,
                                   LocalDateTime generatedAt) {
        this.title = title;
        this.narrative = narrative;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.sessionsAnalyzed = sessionsAnalyzed;
        this.averageFeeling = averageFeeling;
        this.virtuesObserved = virtuesObserved;
        this.strugglesIdentified = strugglesIdentified;
        this.gratitudeThemes = gratitudeThemes;
        this.stressorSignals = stressorSignals;
        this.actionableAdvice = actionableAdvice;
        this.generatedAt = generatedAt;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public int getSessionsAnalyzed() { return sessionsAnalyzed; }
    public void setSessionsAnalyzed(int sessionsAnalyzed) { this.sessionsAnalyzed = sessionsAnalyzed; }

    public Double getAverageFeeling() { return averageFeeling; }
    public void setAverageFeeling(Double averageFeeling) { this.averageFeeling = averageFeeling; }

    public List<String> getVirtuesObserved() { return virtuesObserved; }
    public void setVirtuesObserved(List<String> virtuesObserved) { this.virtuesObserved = virtuesObserved; }

    public List<String> getStrugglesIdentified() { return strugglesIdentified; }
    public void setStrugglesIdentified(List<String> strugglesIdentified) { this.strugglesIdentified = strugglesIdentified; }

    public List<String> getGratitudeThemes() { return gratitudeThemes; }
    public void setGratitudeThemes(List<String> gratitudeThemes) { this.gratitudeThemes = gratitudeThemes; }

    public List<String> getStressorSignals() { return stressorSignals; }
    public void setStressorSignals(List<String> stressorSignals) { this.stressorSignals = stressorSignals; }

    public List<String> getActionableAdvice() { return actionableAdvice; }
    public void setActionableAdvice(List<String> actionableAdvice) { this.actionableAdvice = actionableAdvice; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
