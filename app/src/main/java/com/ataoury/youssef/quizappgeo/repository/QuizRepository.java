package com.ataoury.youssef.quizappgeo.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ataoury.youssef.quizappgeo.model.QuizSession;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    public interface SaveSessionCallback {
        void onSuccess();

        void onError(String message);
    }

    private static final String QUIZ_SESSIONS_COLLECTION = "quizSessions";

    private final FirebaseFirestore firestore;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public QuizRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void saveSession(@NonNull QuizSession session, @NonNull SaveSessionCallback callback) {
        firestore.collection(QUIZ_SESSIONS_COLLECTION)
                .add(session)
                .addOnSuccessListener(documentReference -> {
                    session.setSessionId(documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null
                        ? e.getMessage()
                        : "Impossible de sauvegarder la session de quiz."));
    }

    public LiveData<List<QuizSession>> getUserHistory(@NonNull String userId) {
        MutableLiveData<List<QuizSession>> historyLiveData = new MutableLiveData<>(new ArrayList<>());

        firestore.collection(QUIZ_SESSIONS_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        errorMessage.postValue(error.getMessage() != null
                                ? error.getMessage()
                                : "Impossible de récupérer l'historique.");
                        return;
                    }

                    List<QuizSession> sessions = new ArrayList<>();
                    if (snapshot != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot document : snapshot.getDocuments()) {
                            QuizSession session = document.toObject(QuizSession.class);
                            if (session != null) {
                                session.setSessionId(document.getId());
                                sessions.add(session);
                            }
                        }
                    }

                    historyLiveData.postValue(sessions);
                });

        return historyLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
}
