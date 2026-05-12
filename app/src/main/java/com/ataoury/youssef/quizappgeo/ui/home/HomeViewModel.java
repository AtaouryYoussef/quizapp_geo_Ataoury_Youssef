package com.ataoury.youssef.quizappgeo.ui.home;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ataoury.youssef.quizappgeo.service.LocationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> cityName = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final LocationService locationService;
    private final FirebaseAuth firebaseAuth;

    public HomeViewModel() {
        this.locationService = new LocationService();
        FirebaseAuth authInstance;
        try {
            authInstance = FirebaseAuth.getInstance();
            currentUser.setValue(authInstance.getCurrentUser());
        } catch (Exception e) {
            authInstance = null;
            currentUser.setValue(null);
            errorMessage.setValue("Firebase non initialisé.");
        }
        this.firebaseAuth = authInstance;
    }

    public LiveData<String> getCityName() {
        return cityName;
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void detectCurrentCity(@NonNull Context appContext) {
        locationService.getCurrentLocation(appContext, new LocationService.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                String resolvedCity = locationService.getCityFromLocation(appContext, latitude, longitude);
                if (TextUtils.isEmpty(resolvedCity)) {
                    errorMessage.postValue("Ville introuvable.");
                    return;
                }
                cityName.postValue(resolvedCity);
            }

            @Override
            public void onError(String message) {
                errorMessage.postValue(message);
            }
        });
    }

    public void signOut() {
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }
        currentUser.setValue(null);
    }
}
