package com.rei.examenbackend.dto.growth;

public class WeeklySummaryResponse {
    private long sessionsCompleted;
    private long todosCompleted;
    private long habitsScored;
    private double averageHabitScore;
    private long gratitudeCount;

    public WeeklySummaryResponse() {}

    public WeeklySummaryResponse(long sessionsCompleted, long todosCompleted, long habitsScored, double averageHabitScore, long gratitudeCount) {
        this.sessionsCompleted = sessionsCompleted;
        this.todosCompleted = todosCompleted;
        this.habitsScored = habitsScored;
        this.averageHabitScore = averageHabitScore;
        this.gratitudeCount = gratitudeCount;
    }

    public long getSessionsCompleted() {
        return sessionsCompleted;
    }

    public void setSessionsCompleted(long sessionsCompleted) {
        this.sessionsCompleted = sessionsCompleted;
    }

    public long getTodosCompleted() {
        return todosCompleted;
    }

    public void setTodosCompleted(long todosCompleted) {
        this.todosCompleted = todosCompleted;
    }

    public long getHabitsScored() {
        return habitsScored;
    }

    public void setHabitsScored(long habitsScored) {
        this.habitsScored = habitsScored;
    }

    public double getAverageHabitScore() {
        return averageHabitScore;
    }

    public void setAverageHabitScore(double averageHabitScore) {
        this.averageHabitScore = averageHabitScore;
    }

    public long getGratitudeCount() {
        return gratitudeCount;
    }

    public void setGratitudeCount(long gratitudeCount) {
        this.gratitudeCount = gratitudeCount;
    }
}
