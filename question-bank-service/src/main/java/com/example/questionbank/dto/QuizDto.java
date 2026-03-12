package com.example.questionbank.dto;

public class QuizDto {
    private String id;
    private String title;
    private String description;
    private String category;
    private String difficulty;
    private int timeLimit;
    private boolean isActive;
    private String creator;
    private String createdAt;
    private int questionCount;
    private int participants;

    // Default constructor
    public QuizDto() {
    }

    // Constructor for creation
    public QuizDto(String id, String title, String description, String category,
            String difficulty, int timeLimit, boolean isActive, String creator) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.timeLimit = timeLimit;
        this.isActive = isActive;
        this.creator = creator;
        this.createdAt = java.time.LocalDateTime.now().toString();
        this.questionCount = 0;
        this.participants = 0;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getParticipants() {
        return participants;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }
}
