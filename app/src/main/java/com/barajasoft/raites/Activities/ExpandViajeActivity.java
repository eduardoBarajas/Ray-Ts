package com.barajasoft.raites.Activities;

import android.os.Bundle;

public class ExpandViajeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        //disableBottomMenu();
        disableDrawer();
        disableToolbar();
        setBottomMenu("ExpandedViaje");
        initViewPager("ExpandedViaje");
    }
}
