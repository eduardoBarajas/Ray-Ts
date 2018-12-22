package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainMenuActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        //disableBottomMenu();
        setBottomMenu("MainMenu");
        initDrawer();
        setNavViewMenu("MainMenu");
        initViewPager("MainMenu");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(signOut()){
                    startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                    finish();
                }
            }
        },8000);
    }
}
