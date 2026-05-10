package com.ataoury.youssef.quizappgeo.ui.quiz;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.ataoury.youssef.quizappgeo.model.Question;
import com.ataoury.youssef.quizappgeo.service.AiRepository;

import java.util.ArrayList;
import java.util.List;

public class QuizViewModel extends ViewModel {

    private final AiRepository aiRepository;

    private final MutableLiveData<List<Question>> questions = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Integer> questionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> quizFinished = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public QuizViewModel() {
        this.aiRepository = new AiRepository();
    }

    public LiveData<List<Question>> getQuestions() {
        return questions;
    }

    public LiveData<Question> getCurrentQuestion() {
        return currentQuestion;
    }

    public LiveData<Integer> getQuestionIndex() {
        return questionIndex;
    }

    public LiveData<Integer> getScore() {
        return score;
    }

    public LiveData<Boolean> getQuizFinished() {
        return quizFinished;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadQuestions(@NonNull String cityName) {
        if (TextUtils.isEmpty(cityName.trim())) {
            errorMessage.setValue("Ville invalide.");
            return;
        }

        isLoading.setValue(true);
        score.setValue(0);
        questionIndex.setValue(0);
        quizFinished.setValue(false);

        LiveData<List<Question>> source = aiRepository.generateQuizQuestions(cityName);
        Observer<List<Question>> observer = new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> loadedQuestions) {
                source.removeObserver(this);
                isLoading.setValue(false);

                if (loadedQuestions == null || loadedQuestions.isEmpty()) {
                    errorMessage.setValue("Aucune question disponible pour cette ville.");
                    return;
                }

                questions.setValue(loadedQuestions);
                questionIndex.setValue(0);
                currentQuestion.setValue(loadedQuestions.get(0));
            }
        };
        source.observeForever(observer);

        aiRepository.getErrorMessage().observeForever(message -> {
            if (!TextUtils.isEmpty(message)) {
                isLoading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void answerQuestion(int selectedIndex) {
        Question question = currentQuestion.getValue();
        if (question == null) {
            return;
        }

        List<String> choices = question.getChoices();
        if (choices != null && selectedIndex >= 0 && selectedIndex < choices.size()) {
            question.setSelectedAnswer(choices.get(selectedIndex));
        }

        Integer currentScore = score.getValue();
        if (currentScore == null) {
            currentScore = 0;
        }

        if (selectedIndex == question.getCorrectAnswerIndex()) {
            score.setValue(currentScore + 1);
        }
    }

    public void nextQuestion() {
        List<Question> allQuestions = questions.getValue();
        Integer currentIndex = questionIndex.getValue();

        if (allQuestions == null || allQuestions.isEmpty() || currentIndex == null) {
            quizFinished.setValue(true);
            return;
        }

        int nextIndex = currentIndex + 1;
        if (nextIndex >= allQuestions.size()) {
            quizFinished.setValue(true);
            return;
        }

        questionIndex.setValue(nextIndex);
        currentQuestion.setValue(allQuestions.get(nextIndex));
    }
}
