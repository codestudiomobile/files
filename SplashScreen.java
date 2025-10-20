package com.codestudio.mobile.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.codestudio.mobile.R;

public class SplashScreen extends AppCompatActivity {
    static {
        // Load the C++ runtime dependency first (REQUIRED)
        try {
            System.loadLibrary("c++_shared");
        } catch (UnsatisfiedLinkError ignored) {
        }

        // Load libraries that set up the environment or provide core services
        // Ensure these are all present in your app/src/main/jniLibs/<ABI> folders!

        // 1. Shared Utility/Communication Library (If it exists)
        try {
            System.loadLibrary("local-socket");
        } catch (UnsatisfiedLinkError ignored) {
        }

        // 2. Core JNI Implementation (Contains createSubprocess)
        System.loadLibrary("termux");

        // 3. Environment Setup Dependency
        System.loadLibrary("termux-bootstrap");

        // 4. Your main integration layer
        System.loadLibrary("native_bridge");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_code_studio);
        new Handler().postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }, 2000L);
    }
}
