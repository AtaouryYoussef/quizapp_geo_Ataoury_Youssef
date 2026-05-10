package com.ataoury.youssef.quizappgeo.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.ataoury.youssef.quizappgeo.model.QuizSession;
import com.ataoury.youssef.quizappgeo.repository.QuizRepository;

import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends ViewModel {

    private final QuizRepository quizRepository;
    private final MutableLiveData<List<QuizSession>> history = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HistoryViewModel() {
        this.quizRepository = new QuizRepository();
    }

    public LiveData<List<QuizSession>> getHistory() {
        return history;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadHistory(String userId) {
        LiveData<List<QuizSession>> source = quizRepository.getUserHistory(userId);
        source.observeForever(new Observer<List<QuizSession>>() {
            @Override
            public void onChanged(List<QuizSession> sessions) {
                history.postValue(sessions);
            }
        });

        quizRepository.getErrorMessage().observeForever(message -> {
            if (message != null && !message.trim().isEmpty()) {
                errorMessage.postValue(message);
            }
        });
    }
}
