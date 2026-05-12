package com.ataoury.youssef.quizappgeo.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.ataoury.youssef.quizappgeo.R;
import com.ataoury.youssef.quizappgeo.ui.auth.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private static final String QUIZ_ACTIVITY_FQCN = "com.ataoury.youssef.quizappgeo.ui.quiz.QuizActivity";
    private static final String HISTORY_ACTIVITY_FQCN = "com.ataoury.youssef.quizappgeo.ui.history.HistoryActivity";

    private HomeViewModel homeViewModel;
    private TextView tvCityValue;
    private TextInputEditText etManualCity;
    private boolean shouldStartQuizWhenCityReady;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                boolean coarseGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                if (fineGranted || coarseGranted) {
                    homeViewModel.detectCurrentCity(getApplicationContext());
                } else {
                    shouldStartQuizWhenCityReady = false;
                    Toast.makeText(this, "Permission de localisation refusée.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvCityValue = findViewById(R.id.tvCityValue);
        etManualCity = findViewById(R.id.etManualCity);
        MaterialButton btnStartQuiz = findViewById(R.id.btnStartQuiz);
        MaterialButton btnUseManualCity = findViewById(R.id.btnUseManualCity);
        MaterialButton btnHistory = findViewById(R.id.btnHistory);
        MaterialButton btnLogout = findViewById(R.id.btnLogout);

        btnStartQuiz.setOnClickListener(v -> requestLocationAndDetectCity());

        btnUseManualCity.setOnClickListener(v -> {
            String manualCity = etManualCity.getText() != null ? etManualCity.getText().toString().trim() : "";
            if (TextUtils.isEmpty(manualCity)) {
                Toast.makeText(this, "Saisissez une ville avant de continuer.", Toast.LENGTH_SHORT).show();
                return;
            }
            startQuizForCity(manualCity);
        });

        btnHistory.setOnClickListener(v -> openScreen(HISTORY_ACTIVITY_FQCN, null));

        btnLogout.setOnClickListener(v -> {
            homeViewModel.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        observeViewModel(tvWelcome);
        animateEntry(findViewById(R.id.homeContainer));
    }

    private void observeViewModel(TextView tvWelcome) {
        homeViewModel.getCurrentUser().observe(this, user -> renderWelcome(tvWelcome, user));

        homeViewModel.getCityName().observe(this, city -> {
            if (!TextUtils.isEmpty(city)) {
                tvCityValue.setText(city);
                if (shouldStartQuizWhenCityReady) {
                    shouldStartQuizWhenCityReady = false;
                    startQuizForCity(city);
                }
            }
        });

        homeViewModel.getErrorMessage().observe(this, message -> {
            if (!TextUtils.isEmpty(message)) {
                shouldStartQuizWhenCityReady = false;
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void renderWelcome(TextView tvWelcome, FirebaseUser user) {
        if (user == null) {
            tvWelcome.setText("Bienvenue");
            return;
        }

        String name = !TextUtils.isEmpty(user.getDisplayName()) ? user.getDisplayName() : user.getEmail();
        if (TextUtils.isEmpty(name)) {
            name = "Utilisateur";
        }
        tvWelcome.setText("Bienvenue, " + name);
    }

    private void requestLocationAndDetectCity() {
        shouldStartQuizWhenCityReady = true;

        boolean fineGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            homeViewModel.detectCurrentCity(getApplicationContext());
            return;
        }

        locationPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void openScreen(String className, @Nullable Bundle extras) {
        try {
            Class<?> targetClass = Class.forName(className);
            Intent intent = new Intent(this, targetClass);
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Écran non disponible pour le moment.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startQuizForCity(@NonNull String city) {
        Bundle extras = new Bundle();
        extras.putString("EXTRA_CITY_NAME", city);
        openScreen(QUIZ_ACTIVITY_FQCN, extras);
    }

    private void animateEntry(View container) {
        container.setAlpha(0f);
        container.setTranslationY(24f);
        container.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(420)
                .start();
    }
}
