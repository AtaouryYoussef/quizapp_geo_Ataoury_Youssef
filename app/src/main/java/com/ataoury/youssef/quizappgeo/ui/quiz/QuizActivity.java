package com.ataoury.youssef.quizappgeo.ui.quiz;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ataoury.youssef.quizappgeo.R;
import com.ataoury.youssef.quizappgeo.model.Question;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";
    private static final String EXTRA_SCORE = "EXTRA_SCORE";
    private static final String RESULT_ACTIVITY_FQCN = "com.ataoury.youssef.quizappgeo.ui.quiz.ResultActivity";

    private final Handler handler = new Handler(Looper.getMainLooper());

    private QuizViewModel quizViewModel;
    private TextView tvQuestion;
    private TextView tvIndicator;
    private ProgressBar progressBar;
    private MaterialButton[] answerButtons;
    private ColorStateList defaultButtonTint;
    private String cityName;
    private boolean answerLocked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        cityName = getIntent().getStringExtra(EXTRA_CITY_NAME);
        if (TextUtils.isEmpty(cityName)) {
            Toast.makeText(this, "Ville manquante pour démarrer le quiz.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        quizViewModel = new ViewModelProvider(this).get(QuizViewModel.class);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvIndicator = findViewById(R.id.tvIndicator);
        progressBar = findViewById(R.id.progressLoading);

        answerButtons = new MaterialButton[] {
                findViewById(R.id.btnAnswer1),
                findViewById(R.id.btnAnswer2),
                findViewById(R.id.btnAnswer3),
                findViewById(R.id.btnAnswer4)
        };
        defaultButtonTint = answerButtons[0].getBackgroundTintList();

        initAnswerClicks();
        observeViewModel();
        quizViewModel.loadQuestions(cityName);
    }

    private void initAnswerClicks() {
        for (int i = 0; i < answerButtons.length; i++) {
            final int selectedIndex = i;
            answerButtons[i].setOnClickListener(v -> onAnswerSelected(selectedIndex));
        }
    }

    private void observeViewModel() {
        quizViewModel.getCurrentQuestion().observe(this, this::renderQuestion);

        quizViewModel.getQuestionIndex().observe(this, index -> {
            List<Question> allQuestions = quizViewModel.getQuestions().getValue();
            int total = allQuestions != null ? allQuestions.size() : 0;
            int displayIndex = index != null ? index + 1 : 0;
            tvIndicator.setText("Question " + displayIndex + "/" + total);
        });

        quizViewModel.getIsLoading().observe(this,
                loading -> progressBar.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));

        quizViewModel.getQuizFinished().observe(this, finished -> {
            if (Boolean.TRUE.equals(finished)) {
                Integer finalScore = quizViewModel.getScore().getValue();
                launchResultActivity(finalScore != null ? finalScore : 0);
            }
        });

        quizViewModel.getErrorMessage().observe(this, message -> {
            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderQuestion(Question question) {
        if (question == null) {
            return;
        }

        answerLocked = false;
        tvQuestion.setText(question.getQuestionText());

        List<String> choices = question.getChoices();
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            String text = (choices != null && i < choices.size()) ? choices.get(i) : "Option " + (i + 1);
            button.setText(text);
            button.setEnabled(true);
            resetButtonStyle(button);
        }
    }

    private void onAnswerSelected(int selectedIndex) {
        if (answerLocked) {
            return;
        }
        answerLocked = true;

        Question question = quizViewModel.getCurrentQuestion().getValue();
        if (question == null) {
            return;
        }

        quizViewModel.answerQuestion(selectedIndex);
        int correctIndex = question.getCorrectAnswerIndex();
        markAnswers(selectedIndex, correctIndex);

        handler.postDelayed(() -> {
            resetAllAnswerStyles();
            quizViewModel.nextQuestion();
        }, 1000);
    }

    private void markAnswers(int selectedIndex, int correctIndex) {
        for (int i = 0; i < answerButtons.length; i++) {
            MaterialButton button = answerButtons[i];
            button.setEnabled(false);

            if (i == correctIndex) {
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
                button.setTextColor(Color.WHITE);
            } else if (i == selectedIndex) {
                button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C62828")));
                button.setTextColor(Color.WHITE);
            }
        }
    }

    private void resetAllAnswerStyles() {
        for (MaterialButton button : answerButtons) {
            resetButtonStyle(button);
        }
    }

    private void resetButtonStyle(MaterialButton button) {
        button.setBackgroundTintList(defaultButtonTint);
        button.setTextColor(Color.parseColor("#1F1F1F"));
    }

    private void launchResultActivity(int finalScore) {
        try {
            Class<?> resultClass = Class.forName(RESULT_ACTIVITY_FQCN);
            Intent intent = new Intent(this, resultClass);
            intent.putExtra(EXTRA_SCORE, finalScore);
            intent.putExtra(EXTRA_CITY_NAME, cityName);
            startActivity(intent);
            finish();
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "ResultActivity non disponible pour le moment.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
