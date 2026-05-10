package com.ataoury.youssef.quizappgeo.ui.quiz;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ataoury.youssef.quizappgeo.R;
import com.ataoury.youssef.quizappgeo.model.QuizSession;
import com.ataoury.youssef.quizappgeo.repository.QuizRepository;
import com.ataoury.youssef.quizappgeo.ui.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResultActivity extends AppCompatActivity {

    private static final String EXTRA_SCORE = "EXTRA_SCORE";
    private static final String EXTRA_CITY_NAME = "EXTRA_CITY_NAME";
    private static final String HISTORY_ACTIVITY_FQCN = "com.ataoury.youssef.quizappgeo.ui.history.HistoryActivity";
    private static final String STATE_ALREADY_SAVED = "state_already_saved";

    private final QuizRepository quizRepository = new QuizRepository();

    private TextView tvScoreValue;
    private TextView tvResultMessage;
    private TextView tvCityValue;
    private int score;
    private String cityName;
    private boolean alreadySaved;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvScoreValue = findViewById(R.id.tvScoreValue);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvCityValue = findViewById(R.id.tvCityValue);
        MaterialButton btnReplay = findViewById(R.id.btnReplay);
        MaterialButton btnHistory = findViewById(R.id.btnHistory);

        score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        cityName = getIntent().getStringExtra(EXTRA_CITY_NAME);
        if (TextUtils.isEmpty(cityName)) {
            cityName = "Ville inconnue";
        }

        if (savedInstanceState != null) {
            alreadySaved = savedInstanceState.getBoolean(STATE_ALREADY_SAVED, false);
        }

        tvCityValue.setText(cityName);
        tvResultMessage.setText(buildResultMessage(score));

        animateScore(score);
        animateCard(findViewById(R.id.resultCard));

        if (!alreadySaved) {
            saveQuizSession();
        }

        btnReplay.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnHistory.setOnClickListener(v -> openHistoryScreen());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_ALREADY_SAVED, alreadySaved);
    }

    private void animateScore(int finalScore) {
        ValueAnimator animator = ValueAnimator.ofInt(0, finalScore);
        animator.setDuration(800);
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            tvScoreValue.setText(value + " / 10");
        });
        animator.start();
    }

    private void animateCard(View card) {
        card.setAlpha(0f);
        card.setScaleX(0.94f);
        card.setScaleY(0.94f);
        card.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(420)
                .start();
    }

    private String buildResultMessage(int score) {
        if (score >= 8) {
            return "Excellent travail !";
        }
        if (score >= 5) {
            return "Bien joué ! Continue comme ça.";
        }
        return "À améliorer. Tu peux faire mieux au prochain quiz.";
    }

    private void saveQuizSession() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }

        QuizSession quizSession = new QuizSession(
                currentUser.getUid(),
                cityName,
                score,
                System.currentTimeMillis());

        quizRepository.saveSession(quizSession, new QuizRepository.SaveSessionCallback() {
            @Override
            public void onSuccess() {
                alreadySaved = true;
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ResultActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openHistoryScreen() {
        try {
            Class<?> historyClass = Class.forName(HISTORY_ACTIVITY_FQCN);
            startActivity(new Intent(this, historyClass));
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Historique non disponible pour le moment.", Toast.LENGTH_SHORT).show();
        }
    }
}
