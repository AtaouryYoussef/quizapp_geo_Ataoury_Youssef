package com.ataoury.youssef.quizappgeo.repository;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {

    private static final String USERS_COLLECTION = "users";

    public interface AuthResultCallback {
        void onSuccess(FirebaseUser user);

        void onError(String message);
    }

    public interface OperationCallback {
        void onSuccess();

        void onError(String message);
    }

    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;
    private final FirebaseFirestore firestore;

    public AuthRepository(@NonNull FirebaseAuth firebaseAuth,
            @NonNull GoogleSignInClient googleSignInClient,
            @NonNull FirebaseFirestore firestore) {
        this.firebaseAuth = firebaseAuth;
        this.googleSignInClient = googleSignInClient;
        this.firestore = firestore;
    }

    public static AuthRepository create(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        int webClientIdRes = appContext.getResources()
                .getIdentifier("default_web_client_id", "string", appContext.getPackageName());

        String webClientId = webClientIdRes != 0 ? appContext.getString(webClientIdRes) : "";

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(webClientId)
                .build();

        GoogleSignInClient googleClient = GoogleSignIn.getClient(appContext, gso);
        return new AuthRepository(FirebaseAuth.getInstance(), googleClient, FirebaseFirestore.getInstance());
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void signInWithEmailAndPassword(String email, String password, @NonNull AuthResultCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Connexion réussie, mais utilisateur introuvable.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null
                        ? e.getMessage()
                        : "Échec de la connexion par email."));
    }

    public void signInWithGoogleIntent(@NonNull Intent data, @NonNull AuthResultCallback callback) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null || TextUtils.isEmpty(account.getIdToken())) {
                callback.onError("Token Google invalide.");
                return;
            }
            firebaseAuthWithGoogle(account.getIdToken(), callback);
        } catch (ApiException e) {
            callback.onError("Échec Google Sign-In: " + e.getStatusCode());
        }
    }

    public void createUserWithEmailAndPassword(String email,
            String password,
            @NonNull AuthResultCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Inscription réussie, mais utilisateur introuvable.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null
                        ? e.getMessage()
                        : "Échec de la création du compte."));
    }

    public void saveUserToFirestore(@NonNull FirebaseUser firebaseUser,
            @NonNull String displayName,
            @NonNull OperationCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("email", firebaseUser.getEmail());
        userData.put("displayName", displayName.trim());
        userData.put("createdAt", System.currentTimeMillis());

        firestore.collection(USERS_COLLECTION)
                .document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null
                        ? e.getMessage()
                        : "Impossible d'enregistrer l'utilisateur dans Firestore."));
    }

    private void firebaseAuthWithGoogle(@NonNull String idToken, @NonNull AuthResultCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Connexion Google réussie, mais utilisateur introuvable.");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage() != null
                        ? e.getMessage()
                        : "Échec de l'authentification Firebase avec Google."));
    }
}
