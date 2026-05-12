package com.ataoury.youssef.quizappgeo.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ataoury.youssef.quizappgeo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private boolean registrationStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextInputEditText etName = findViewById(R.id.etName);
        TextInputEditText etEmail = findViewById(R.id.etEmail);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        TextInputEditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvLoginLink = findViewById(R.id.tvLoginLink);

        try {
            authViewModel = new ViewModelProvider(this, new AuthViewModel.Factory(getApplication()))
                    .get(AuthViewModel.class);
        } catch (Exception e) {
            Toast.makeText(this, "Configuration Firebase invalide. Vérifiez google-services.json.", Toast.LENGTH_LONG)
                    .show();
            btnRegister.setEnabled(false);
            tvLoginLink.setEnabled(false);
            return;
        }

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            String confirmPassword = etConfirmPassword.getText() != null
                    ? etConfirmPassword.getText().toString()
                    : "";

            registrationStarted = true;
            authViewModel.registerWithEmailAndPassword(name, email, password, confirmPassword);
        });

        tvLoginLink.setOnClickListener(v -> finish());

        observeViewModel();
    }

    private void observeViewModel() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (registrationStarted && user != null) {
                Toast.makeText(this, "Inscription réussie.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        authViewModel.getErrorMessage().observe(this, message -> {
            if (!TextUtils.isEmpty(message)) {
                registrationStarted = false;
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
