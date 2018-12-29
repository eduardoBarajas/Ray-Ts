package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.barajasoft.raites.Adapters.ViewPagerAdapter;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Fragments.MapFragment;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class PublicarViajeActivity extends BaseActivity{
    private TextView direccionSalida, direccionDestino, cuposTextView;
    private LatLng salidaLocation = new LatLng();
    private LatLng destinoLocation = new LatLng();
    private Button btnConfirmar, btnTrayecto, btnConductor, btnPasajero;
    private String rolViaje = "None";
    private DatePicker datePicker;
    private TimePicker timePicker;
    private TextInputEditText cuposDisponiblesText;
    private LinearLayout cuposLayout;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy-kk:mm a");
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference ciudadesReference = database.getReference("Ciudades");
    private List<String> cities = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
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
        datePicker = layout.findViewById(R.id.date);
        timePicker = layout.findViewById(R.id.hora);
        cuposDisponiblesText = layout.findViewById(R.id.cuposDisponiblesText);
        cuposLayout = layout.findViewById(R.id.cuposLayout);
        cuposTextView = layout.findViewById(R.id.cuposTextView);
        btnPasajero.setOnClickListener(e->{
            btnPasajero.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            btnConductor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0277BD")));
            rolViaje = "Pasajero";
            cuposTextView.setText("Cuantos asientos necesitas?");
            cuposLayout.setVisibility(View.VISIBLE);
        });
        btnConductor.setOnClickListener(e->{
            btnConductor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            btnPasajero.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#0277BD")));
            rolViaje = "Conductor";
            cuposTextView.setText("Cuantos asientos disponibles tendras?");
            cuposLayout.setVisibility(View.VISIBLE);
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
            if(!rolViaje.equals("None") && validateDate() && !cuposDisponiblesText.getText().toString().isEmpty()){
                //Agendar viaje
                Viaje viaje = new Viaje();
                viaje.setDireccionDestino(direccionDestino.getText().toString());
                viaje.setDireccionSalida(direccionSalida.getText().toString());
                viaje.setEspaciosDisponibles(Integer.parseInt(cuposDisponiblesText.getText().toString()));
                viaje.setFechaViaje(String.valueOf(datePicker.getDayOfMonth())+"/"+String.valueOf(datePicker.getMonth())+"/"+String.valueOf(datePicker.getYear()));
                viaje.setFechaPublicacion(simpleDateFormat.format(Calendar.getInstance().getTime()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String hora = String.valueOf(timePicker.getHour())+":"+String.valueOf(timePicker.getMinute());
                    if(timePicker.getHour()>=12)
                        hora += " PM";
                    else
                        hora += " AM";
                    viaje.setHoraViaje(hora);
                }
                if(rolViaje.equals("Conductor")){
                    viaje.setKeyConductor(getCurrentUserKey());
                }else{
                    List<String> pasajeros = new LinkedList<>();
                    pasajeros.add(getCurrentUserKey());
                    viaje.setKeysPasajeros(pasajeros);
                }
                List<LatLng> puntos = new LinkedList<>();
                puntos.add(salidaLocation);
                puntos.add(destinoLocation);
                viaje.setPuntosDeViaje(puntos);
                viajesReference.child(viaje.getKey()).setValue(viaje, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        ciudadesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot data : dataSnapshot.getChildren()) {
                                    cities.add(data.getValue(String.class));
                                }
                                int sizeInicial = cities.size(), sizeFinal = -1;
                                if(!cities.contains(direccionSalida.getText().toString().split(",")[1].substring(1))){
                                    cities.add(direccionSalida.getText().toString().split(",")[1].substring(1));
                                }
                                if(!cities.contains(direccionDestino.getText().toString().split(",")[1].substring(1))){
                                    cities.add(direccionDestino.getText().toString().split(",")[1].substring(1));
                                }
                                sizeFinal = cities.size();
                                if(sizeFinal > sizeInicial){
                                    ciudadesReference.setValue(cities, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            Toast.makeText(getApplicationContext(),"Se publico el viaje correctamente",Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(PublicarViajeActivity.this, MainMenuActivity.class));
                                            finish();
                                        }
                                    });
                                }else{
                                    Toast.makeText(getApplicationContext(),"Se publico el viaje correctamente",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(PublicarViajeActivity.this, MainMenuActivity.class));
                                    finish();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                });
            }else{
                if(rolViaje.equals("None"))
                    Toast.makeText(getApplicationContext(),"Debes elegir tu rol en este viaje antes de publicarlo",Toast.LENGTH_SHORT).show();
                if(!validateDate())
                    Toast.makeText(getApplicationContext(),"Debes elegir una fecha valida para el viaje",Toast.LENGTH_SHORT).show();
                if(cuposDisponiblesText.getText().toString().isEmpty())
                    Toast.makeText(getApplicationContext(),"Debes definir el numero de espacios que se necesitaran",Toast.LENGTH_SHORT).show();
            }
        });
        addContent(layout);
    }

    private boolean validateDate() {
        int year, month, day;
        year = datePicker.getYear();
        month = datePicker.getMonth();
        day = datePicker.getDayOfMonth();
        String currentDate = simpleDateFormat.format(Calendar.getInstance().getTime());
        String[] date = currentDate.split("/");
        return ((Integer.parseInt(date[0]+Integer.parseInt(date[1])+Integer.parseInt(date[2].split("-")[0])))>=(year+month+day));
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
