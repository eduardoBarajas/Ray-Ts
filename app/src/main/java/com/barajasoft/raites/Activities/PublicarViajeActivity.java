package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Adapters.ViewPagerAdapter;
import com.barajasoft.raites.Fragments.MapFragment;
import com.barajasoft.raites.R;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class PublicarViajeActivity extends BaseActivity{
    private TextView direccionSalida, direccionDestino;
    private LatLng salidaLocation = new LatLng();
    private LatLng destinoLocation = new LatLng();
    private Button btnConfirmar, btnTrayecto, btnConductor, btnPasajero;
    private String rolViaje = "None";
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
        Button btnCamino = layout.findViewById(R.id.btnCamino);
        btnTrayecto = layout.findViewById(R.id.trayectoButton);
        btnConfirmar = layout.findViewById(R.id.confirmarButton);
        direccionSalida = layout.findViewById(R.id.txtSalida);
        direccionDestino = layout.findViewById(R.id.txtDestino);
        btnPasajero = layout.findViewById(R.id.btnPasajero);
        btnConductor = layout.findViewById(R.id.btnConductor);
        btnPasajero.setOnClickListener(e->{
            btnPasajero.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            btnConductor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0277BD")));
            rolViaje = "Pasajero";
        });
        btnConductor.setOnClickListener(e->{
            btnConductor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            btnPasajero.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0277BD")));
            rolViaje = "Conductor";
        });
        btnCamino.setOnClickListener(e->{
            Intent intent = new Intent(PublicarViajeActivity.this,MapActivity.class);
            startActivityForResult(intent, PICK_DIRECCION);
        });
        btnTrayecto.setOnClickListener(e->{
            Intent intent = new Intent(PublicarViajeActivity.this,VisualizeTravelActivity.class);
            intent.putExtra("direccionDestino",direccionDestino.getText().toString());
            intent.putExtra("direccionSalida",direccionSalida.getText().toString());
            intent.putExtra("latitudSalida", salidaLocation.getLatitude());
            intent.putExtra("longitudSalida", salidaLocation.getLongitude());
            intent.putExtra("latitudDestino", destinoLocation.getLatitude());
            intent.putExtra("longitudDestino", destinoLocation.getLongitude());
            startActivity(intent);
        });
        btnConfirmar.setOnClickListener(e->{
            if(!rolViaje.equals("None")){
                //Agendar viaje
            }else{
                Toast.makeText(getApplicationContext(),"Debes elegir tu rol en este viaje antes de publicarlo",Toast.LENGTH_SHORT).show();
            }
        });
        addContent(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_DIRECCION ){
            if(resultCode == RESULT_OK) {
                direccionSalida.setText(data.getStringExtra("direccionInicio"));
                direccionDestino.setText(data.getStringExtra("direccionDestino"));
                salidaLocation.setLatitude(data.getDoubleExtra("latitudInicio",0));
                salidaLocation.setLongitude(data.getDoubleExtra("longitudInicio",0));
                destinoLocation.setLatitude(data.getDoubleExtra("latitudDestino",0));
                destinoLocation.setLongitude(data.getDoubleExtra("longitudDestino",0));
                btnConfirmar.setVisibility(View.VISIBLE);
                btnTrayecto.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Se cancelo la seleccion de direccion", Toast.LENGTH_SHORT).show();
            }
    }
}
