package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.barajasoft.raites.R;
import com.mapbox.mapboxsdk.Mapbox;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);
        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        Runnable timerCode = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        };
        Handler timerHandler = new Handler();
        timerHandler.postDelayed(timerCode,2000);
    }
}
