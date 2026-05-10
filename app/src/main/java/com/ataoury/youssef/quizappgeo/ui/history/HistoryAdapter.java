package com.ataoury.youssef.quizappgeo.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ataoury.youssef.quizappgeo.R;
import com.ataoury.youssef.quizappgeo.model.QuizSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<QuizSession> sessions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        QuizSession session = sessions.get(position);

        String city = session.getCityName() != null ? session.getCityName() : "Ville inconnue";
        holder.tvCity.setText(city);
        holder.tvScore.setText(session.getScore() + " / 10");
        holder.tvDate.setText(dateFormat.format(new Date(session.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public void submitList(List<QuizSession> newSessions) {
        sessions.clear();
        if (newSessions != null) {
            sessions.addAll(newSessions);
        }
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCity;
        private final TextView tvScore;
        private final TextView tvDate;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
