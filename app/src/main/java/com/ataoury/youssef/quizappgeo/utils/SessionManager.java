package com.ataoury.youssef.quizappgeo.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public final class SessionManager {

    private static volatile SessionManager instance;

    private final SharedPreferences sharedPreferences;

    private SessionManager(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        this.sharedPreferences = appContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SessionManager getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    public void saveUserId(String uid) {
        sharedPreferences.edit().putString(Constants.KEY_USER_ID, uid).apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null);
    }

    public void clearSession() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        String userId = getUserId();
        return userId != null && !userId.trim().isEmpty();
    }
}
