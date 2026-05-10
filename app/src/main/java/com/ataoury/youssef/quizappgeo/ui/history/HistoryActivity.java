package com.ataoury.youssef.quizappgeo.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ataoury.youssef.quizappgeo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HistoryActivity extends AppCompatActivity {

    private HistoryViewModel historyViewModel;
    private HistoryAdapter historyAdapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView recyclerView = findViewById(R.id.recyclerHistory);
        tvEmpty = findViewById(R.id.tvEmpty);

        historyAdapter = new HistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(historyAdapter);

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
        observeViewModel();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        historyViewModel.loadHistory(user.getUid());
    }

    private void observeViewModel() {
        historyViewModel.getHistory().observe(this, sessions -> {
            historyAdapter.submitList(sessions);
            boolean isEmpty = sessions == null || sessions.isEmpty();
            tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        historyViewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
