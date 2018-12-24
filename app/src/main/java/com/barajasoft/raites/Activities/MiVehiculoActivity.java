package com.barajasoft.raites.Activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.barajasoft.raites.R;

import java.util.LinkedList;
import java.util.List;

public class MiVehiculoActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> imageUrls = new LinkedList<>();
        imageUrls.add("https://cdn.pixabay.com/photo/2016/11/11/23/34/cat-1817970_960_720.jpg");
        imageUrls.add("https://cdn.pixabay.com/photo/2017/12/21/12/26/glowworm-3031704_960_720.jpg");
        imageUrls.add("https://cdn.pixabay.com/photo/2017/12/24/09/09/road-3036620_960_720.jpg");
        imageUrls.add("https://cdn.pixabay.com/photo/2017/11/07/00/07/fantasy-2925250_960_720.jpg");
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
