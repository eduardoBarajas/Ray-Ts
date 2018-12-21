package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.barajasoft.raites.R;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher_activity);
        Runnable timerCode = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),MainMenuActivity.class));
                finish();
            }
        };
        Handler timerHandler = new Handler();
        timerHandler.postDelayed(timerCode,2000);
    }
}
