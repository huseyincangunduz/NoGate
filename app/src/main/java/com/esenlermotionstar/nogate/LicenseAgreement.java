package com.esenlermotionstar.nogate;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LicenseAgreement extends AppCompatActivity {
    private static final String PREFERENCE_DEFAULT_NAME = "NoGatePrefs";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_agreement);
    }

    public void agree_lcs(View view) {
        SharedPreferences sharedPrefs = getApplicationContext().getSharedPreferences(PREFERENCE_DEFAULT_NAME, Context.MODE_PRIVATE);
       sharedPrefs.edit().putBoolean("first_opening",false).apply();
       finish();
    }

    public void cancel_lcs(View view) {
        this.finishAffinity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finishAffinity();
    }
}
