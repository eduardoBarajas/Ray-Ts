package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.R;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class PublicarViajeActivity extends BaseActivity {
    private TextView direccionSalida, direccionDestino;
    private LatLng salidaLocation = new LatLng();
    private LatLng destinoLocation = new LatLng();
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
        Button btnSalida = layout.findViewById(R.id.btnSalida);
        Button btnDestino = layout.findViewById(R.id.btnDestino);
        direccionSalida = layout.findViewById(R.id.txtSalida);
        direccionDestino = layout.findViewById(R.id.txtDestino);
        btnSalida.setOnClickListener(e->{
            Intent intent = new Intent(PublicarViajeActivity.this,MapActivity.class);
            intent.putExtra("tipoSeleccion","Punto Salida");
            startActivityForResult(intent, PICK_DIRECCION_SALIDA);
        });
        btnDestino.setOnClickListener(e->{
            Intent intent = new Intent(PublicarViajeActivity.this,MapActivity.class);
            intent.putExtra("tipoSeleccion","Punto Destino");
            startActivityForResult(intent, PICK_DIRECCION_DESTINO);
        });
        addContent(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_DIRECCION_SALIDA ){
            if(resultCode == RESULT_OK) {
                direccionSalida.setText(data.getStringExtra("direccion"));
                salidaLocation.setLatitude(data.getDoubleExtra("latitud",0));
                salidaLocation.setLongitude(data.getDoubleExtra("longitud",0));
                direccionSalida.setText(direccionSalida.getText()+"\n\n"+salidaLocation.getLatitude()+" "+salidaLocation.getLongitude());
            } else {
                Toast.makeText(getApplicationContext(), "Se cancelo la seleccion de direccion inicial", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == PICK_DIRECCION_DESTINO){
            if(resultCode == RESULT_OK) {
                direccionDestino.setText(data.getStringExtra("direccion"));
                destinoLocation.setLatitude(data.getDoubleExtra("latitud",0));
                destinoLocation.setLongitude(data.getDoubleExtra("longitud",0));
                direccionDestino.setText(direccionDestino.getText()+"\n\n"+destinoLocation.getLatitude()+" "+destinoLocation.getLongitude());
            } else {
                Toast.makeText(getApplicationContext(), "Se cancelo la seleccion de direccion inicial", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
