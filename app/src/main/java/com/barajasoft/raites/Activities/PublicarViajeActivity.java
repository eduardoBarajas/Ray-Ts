package com.barajasoft.raites.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.barajasoft.raites.R;

public class PublicarViajeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        initDrawer();
        setNavViewMenu("publicar_viaje");
        setToolbar("","Agendar Viaje");
        View layout = LayoutInflater.from(this).inflate(R.layout.publicar_viaje_activity,null);
        addContent(layout);
    }
}
