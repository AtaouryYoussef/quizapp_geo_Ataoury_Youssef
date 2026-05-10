package com.ataoury.youssef.quizappgeo.service;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ataoury.youssef.quizappgeo.model.Question;

import java.util.List;

public class AiRepository {

    private final GeminiApiService geminiApiService;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AiRepository() {
        this.geminiApiService = new GeminiApiService();
    }

    public LiveData<List<Question>> generateQuizQuestions(@NonNull String cityName) {
        MutableLiveData<List<Question>> questionsLiveData = new MutableLiveData<>();

        geminiApiService.generateQuizQuestions(cityName, new GeminiApiService.GeminiCallback() {
            @Override
            public void onSuccess(List<Question> questions) {
                questionsLiveData.postValue(questions);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });

        return questionsLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
