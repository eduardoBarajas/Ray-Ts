package com.barajasoft.raites.Activities;

import android.os.Bundle;

public class MainMenuActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        //disableBottomMenu();
        setBottomMenu("MainMenu");
        setNavViewMenu("MainMenu");
    }
}
