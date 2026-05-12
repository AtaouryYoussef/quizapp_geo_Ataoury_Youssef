package com.ataoury.youssef.quizapp_geo_ataoury_youssef;

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent destinationIntent;

        if (currentUser != null) {
            destinationIntent = new Intent(this, HomeActivity.class);
        } else {
            destinationIntent = new Intent(this, LoginActivity.class);
        }

        startActivity(destinationIntent);
        finish();
    }
}