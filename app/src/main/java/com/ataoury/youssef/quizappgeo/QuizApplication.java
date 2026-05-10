package com.ataoury.youssef.quizappgeo;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class QuizApplication extends Application {

    private static QuizApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Gestionnaire global minimal pour éviter un crash silencieux.
            throwable.printStackTrace();
        });
    }

    public static QuizApplication getInstance() {
        return instance;
    }
}
