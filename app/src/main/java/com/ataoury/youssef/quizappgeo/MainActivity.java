package com.ataoury.youssef.quizappgeo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ataoury.youssef.quizappgeo.ui.auth.LoginActivity;
import com.ataoury.youssef.quizappgeo.ui.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routeToNextScreen();
    }

    private void routeToNextScreen() {
        try {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            Intent destinationIntent;

            if (currentUser != null) {
                destinationIntent = new Intent(this, HomeActivity.class);
            } else {
                destinationIntent = new Intent(this, LoginActivity.class);
            }

            destinationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(destinationIntent);
        } catch (Exception e) {
            Intent fallbackIntent = new Intent(this, LoginActivity.class);
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(fallbackIntent);
        } finally {
            finish();
        }
    }
}
