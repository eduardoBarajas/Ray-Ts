package com.barajasoft.raites.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.barajasoft.raites.R;

public class MiVehiculoActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        initDrawer();
        setNavViewMenu("miVehiculo");
        setToolbar("","Mi Vehiculo");
        View layout = LayoutInflater.from(this).inflate(R.layout.mi_vehiculo_activity,null);
        addContent(layout);
    }
}
