package com.ataoury.youssef.quizappgeo.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ataoury.youssef.quizappgeo.R;
import com.ataoury.youssef.quizappgeo.ui.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    authViewModel.loginWithGoogleIntent(result.getData());
                } else {
                    Toast.makeText(this, "Connexion Google annulée.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authViewModel = new ViewModelProvider(this, new AuthViewModel.Factory(getApplication()))
                .get(AuthViewModel.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        MaterialButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        TextView tvRegisterLink = findViewById(R.id.tvRegisterLink);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Veuillez remplir email et mot de passe.", Toast.LENGTH_SHORT).show();
                return;
            }
            authViewModel.loginWithEmailAndPassword(email, password);
        });

        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = authViewModel.getGoogleSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        tvRegisterLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        observeViewModel();
    }

    private void observeViewModel() {
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                Toast.makeText(this, "Connexion réussie: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });

        authViewModel.getErrorMessage().observe(this, message -> {
            if (!TextUtils.isEmpty(message)) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
