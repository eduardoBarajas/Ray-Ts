package com.barajasoft.raites.Activities;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.barajasoft.raites.R;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class SolicitudesViajeActivity extends BaseActivity {
    private RecyclerView rvSolicitudes;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableBottomMenu();
        disableDrawer();
        disableViewPager();
        setToolbar("", "Solicitudes De Viajes");
        View layout = LayoutInflater.from(this).inflate(R.layout.solicitudes_viajes_activity, null);
        rvSolicitudes = layout.findViewById(R.id.rvSolicitudes);
        rvSolicitudes.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL, false);
        rvSolicitudes.setLayoutManager(manager);
        rvSolicitudes.setItemAnimator(new DefaultItemAnimator());
    }
}
