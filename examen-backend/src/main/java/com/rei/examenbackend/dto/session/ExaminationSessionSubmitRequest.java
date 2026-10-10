package com.rei.examenbackend.dto.session;

import com.rei.examenbackend.dto.answer.AnswerRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ExaminationSessionSubmitRequest {
    @NotEmpty
    private List<@Valid AnswerRequest> answers;

    @Size(max = 1000)
    private String notes;

    private Integer moodScore;

    public ExaminationSessionSubmitRequest() {}

    public ExaminationSessionSubmitRequest(List<AnswerRequest> answers, String notes, Integer moodScore) {
        this.answers = answers;
        this.notes = notes;
        this.moodScore = moodScore;
    }

    public List<AnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerRequest> answers) {
        this.answers = answers;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getMoodScore() {
        return moodScore;
    }

    public void setMoodScore(Integer moodScore) {
        this.moodScore = moodScore;
    }
}
