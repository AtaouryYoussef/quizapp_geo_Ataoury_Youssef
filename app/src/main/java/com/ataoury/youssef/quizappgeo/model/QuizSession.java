package com.ataoury.youssef.quizappgeo.model;

public class QuizSession {

    private String sessionId;
    private String userId;
    private String cityName;
    private int score;
    private long timestamp;

    public QuizSession() {
    }

    public QuizSession(String userId, String cityName, int score, long timestamp) {
        this.userId = userId;
        this.cityName = cityName;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
