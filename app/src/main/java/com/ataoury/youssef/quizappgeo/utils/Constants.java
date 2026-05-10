package com.ataoury.youssef.quizappgeo.utils;

public final class Constants {

    private Constants() {
        // Empêche l'instanciation de cette classe utilitaire.
    }

    public static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    public static final String GEMINI_MODEL_NAME = "gemini-pro";

    public static final String FIRESTORE_COLLECTION_USERS = "users";
    public static final String FIRESTORE_COLLECTION_SESSIONS = "quizSessions";

    public static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";
    public static final String EXTRA_SCORE = "EXTRA_SCORE";

    public static final String PREFS_NAME = "quizappgeo_prefs";
    public static final String KEY_USER_ID = "KEY_USER_ID";

    public static final int QUIZ_QUESTIONS_COUNT = 10;
}
