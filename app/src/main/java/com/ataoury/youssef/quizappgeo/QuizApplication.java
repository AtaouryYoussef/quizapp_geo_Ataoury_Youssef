package com.ataoury.youssef.quizappgeo;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class QuizApplication extends Application {

    private static QuizApplication instance;
    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseApp.initializeApp(this);
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // Log first, then delegate to Android's default handler.
            throwable.printStackTrace();
            if (defaultExceptionHandler != null) {
                defaultExceptionHandler.uncaughtException(thread, throwable);
            }
        });
    }

    public static QuizApplication getInstance() {
        return instance;
    }
}
