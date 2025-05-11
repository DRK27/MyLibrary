package com.example.mylibrary;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MyLibraryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
} 