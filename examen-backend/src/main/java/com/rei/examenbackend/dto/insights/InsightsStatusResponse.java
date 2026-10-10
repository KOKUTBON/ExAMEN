package com.rei.examenbackend.dto.insights;

/**
 * @param aiEnabled true when a Gemini API key is configured and enabled
 * @param provider  "gemini" or "local" (deterministic heuristics, no data leaves the server)
 */
public record InsightsStatusResponse(boolean aiEnabled, String provider) {
}
