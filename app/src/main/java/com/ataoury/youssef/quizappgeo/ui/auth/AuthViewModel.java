package com.ataoury.youssef.quizappgeo.ui.auth;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.ataoury.youssef.quizappgeo.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel(@NonNull AuthRepository authRepository) {
        this.authRepository = authRepository;
        currentUser.setValue(authRepository.getCurrentUser());
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loginWithEmailAndPassword(String email, String password) {
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Veuillez saisir un email valide.");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            errorMessage.setValue("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        authRepository.signInWithEmailAndPassword(email.trim(), password, new AuthRepository.AuthResultCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                currentUser.setValue(user);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public Intent getGoogleSignInIntent() {
        return authRepository.getGoogleSignInIntent();
    }

    public void loginWithGoogleIntent(@NonNull Intent data) {
        authRepository.signInWithGoogleIntent(data, new AuthRepository.AuthResultCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                currentUser.setValue(user);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void registerWithEmailAndPassword(String displayName,
            String email,
            String password,
            String confirmPassword) {
        if (TextUtils.isEmpty(displayName)) {
            errorMessage.setValue("Veuillez saisir votre nom.");
            return;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Veuillez saisir un email valide.");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            errorMessage.setValue("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Les mots de passe ne correspondent pas.");
            return;
        }

        authRepository.createUserWithEmailAndPassword(email.trim(), password, new AuthRepository.AuthResultCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                authRepository.saveUserToFirestore(user, displayName, new AuthRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        currentUser.setValue(user);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.setValue(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Application application;

        public Factory(@NonNull Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AuthViewModel.class)) {
                AuthRepository repository = AuthRepository.create(application);
                return (T) new AuthViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }
}
