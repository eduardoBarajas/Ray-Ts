package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.YesOrNoChooserDialog;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.LinkedList;
import java.util.List;

public class ExpandSolicitudViajeActivity extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference usuariosReference = database.getReference("Usuarios");
    private TextView txtSalida, txtDestino, txtEspacios, txtPuntoDeParada, txtEstado, labelMap;
    private TextInputEditText editEspacios;
    private Button btnRegresar, btnConfirmar, btnMapa;
    private SolicitudViaje solicitudActual;
    private Viaje viajeActual;
    private SharedPreferences pref;
    private final int MODIFY_WAYPOINT = 744;
    private Point paradaModificada;
    private boolean isModified = false;
    private ResultListener listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.expanded_solicitud_viaje_activity);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnMapa = findViewById(R.id.btnMap);
        txtEspacios = findViewById(R.id.txtEspacios);
        txtSalida = findViewById(R.id.txtDireccionSalida);
        txtDestino = findViewById(R.id.txtDireccionDestino);
        txtPuntoDeParada = findViewById(R.id.txtPuntoDeParada);
        txtEstado = findViewById(R.id.txtEstado);
        labelMap = findViewById(R.id.labelMap);
        editEspacios = findViewById(R.id.editEspacios);
        listener = new ResultListener() {
            @Override
            public void result(String tag, Object result) {
                if(tag.equals("ModificarSolicitud")){
                    if(((String)result).equals("Accept")){
                        solicitudActual.setAceptada(false);
                        viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot data : dataSnapshot.getChildren()){
                                    if(solicitudActual.getKeyViaje().equals(data.getValue(Viaje.class).getKey())){
                                        if(Integer.parseInt(editEspacios.getText().toString())<=data.getValue(Viaje.class).getEspaciosDisponibles()){
                                            //si se puede editar
                                            solicitudActual.setEspaciosSolicitados(Integer.parseInt(editEspacios.getText().toString()));
                                            solicitudActual.setPuntoDeParada(new LatLng(paradaModificada.latitude(), paradaModificada.longitude()));
                                            solicitudActual.setDireccionDeParada(txtPuntoDeParada.getText().toString());
                                            solicitudesReference.child(solicitudActual.getKey()).setValue(solicitudActual, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    Toast.makeText(getApplicationContext(), "La solicitud fue enviada de nuevo", Toast.LENGTH_LONG).show();
                                                    finish();
                                                }
                                            });
                                        }else{
                                            Snackbar.make(getCurrentFocus(), "El viaje no tiene suficientes espacios, reduce la cantidad antes de continuar", Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                }
            }
        };
        if(getIntent().hasExtra("solicitud_key")){
            solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.getValue(SolicitudViaje.class).getKey().equals(getIntent().getStringExtra("solicitud_key"))){
                            solicitudActual = data.getValue(SolicitudViaje.class);
                        }
                    }
                    viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                if(data.getValue(Viaje.class).getKey().equals(solicitudActual.getKeyViaje())){
                                    viajeActual = data.getValue(Viaje.class);
                                }
                            }
                            txtDestino.setText(viajeActual.getDireccionDestino());
                            txtSalida.setText(viajeActual.getDireccionSalida());
                            txtPuntoDeParada.setText(solicitudActual.getDireccionDeParada());
                            if(solicitudActual.isAceptada()){
                                txtEstado.setText("Aceptada");
                            }else{
                                txtEstado.setText("Pendiente");
                            }
                            btnRegresar.setOnClickListener(e->{
                                finish();
                            });
                            btnMapa.setOnClickListener(e->{
                                Intent intent = new Intent(getApplicationContext() ,VisualizeTravelActivity.class);
                                intent.putExtra("visualizeMapParada", true);
                                if(getIntent().hasExtra("Edicion"))
                                    intent.putExtra("editMapParada", true);
                                intent.putExtra("direccionDestino", viajeActual.getDireccionDestino());
                                intent.putExtra("direccionSalida", viajeActual.getDireccionSalida());
                                intent.putExtra("latitudSalida", viajeActual.getPuntosDeViaje().get(0).getLatitude());
                                intent.putExtra("longitudSalida", viajeActual.getPuntosDeViaje().get(0).getLongitude());
                                intent.putExtra("latitudDestino", viajeActual.getPuntosDeViaje().get(1).getLatitude());
                                intent.putExtra("longitudDestino", viajeActual.getPuntosDeViaje().get(1).getLongitude());
                                String[] puntosArray = new String[viajeActual.getPuntosDeParada().size()];
                                for(int i = 0; i<viajeActual.getPuntosDeParada().size(); i++){
                                    puntosArray[i] = String.valueOf(viajeActual.getPuntosDeParada().get(i).getLatitude())+":"+
                                            String.valueOf(viajeActual.getPuntosDeParada().get(i).getLongitude());
                                }
                                intent.putExtra("puntosParada", puntosArray);
                                solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        List<String> direccionesParada = new LinkedList<>();
                                        for(DataSnapshot data : dataSnapshot.getChildren()){
                                            if(data.getValue(SolicitudViaje.class).getKeyViaje().equals(viajeActual.getKey())){
                                                direccionesParada.add(data.getValue(SolicitudViaje.class).getDireccionDeParada());
                                            }
                                        }
                                        String[] direccionesParadaArray = new String[direccionesParada.size()];
                                        for(int i = 0; i < direccionesParada.size(); i++){
                                            direccionesParadaArray[i] = direccionesParada.get(i);
                                        }
                                        intent.putExtra("direccionesPuntosParada", direccionesParadaArray);
                                        usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                List<String> nombres = new LinkedList<>();
                                                for(DataSnapshot data : dataSnapshot.getChildren()){
                                                    for(String keyUser : viajeActual.getKeysPasajeros()){
                                                        if(data.getValue(User.class).getKey().equals(keyUser)){
                                                            nombres.add(data.getValue(User.class).getNombre());
                                                        }
                                                        if(data.getValue(User.class).getKey().equals(pref.getString("key", null))){
                                                            intent.putExtra("currentUserKey", data.getValue(User.class).getKey());
                                                            intent.putExtra("currentUserName", data.getValue(User.class).getNombre());
                                                        }
                                                    }
                                                }
                                                String[] nombresArray = new String[nombres.size()];
                                                for(int i = 0; i<nombres.size(); i++){
                                                    nombresArray[i] = nombres.get(i);
                                                }
                                                intent.putExtra("usersParadas", nombresArray);
                                                if(getIntent().hasExtra("Edicion"))
                                                    startActivityForResult(intent, MODIFY_WAYPOINT);
                                                else
                                                    startActivity(intent);
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                                        });
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
                            });
                            if(getIntent().hasExtra("Edicion")){
                                txtEspacios.setVisibility(View.GONE);
                                editEspacios.setText(String.valueOf(solicitudActual.getEspaciosSolicitados()));
                                btnConfirmar.setOnClickListener(e->{
                                    if(isModified){
                                        YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(ExpandSolicitudViajeActivity.this, "ModificarSolicitud",
                                                "Modificar Solicitud De Viaje", "Seguro que quieres modificar la solicitud?, sera enviada al conductor" +
                                                " de nuevo para ser revisada de nuevo.", listener);
                                        dlg.show();
                                    }
                                });
                            }else{
                                txtEspacios.setText(String.valueOf(solicitudActual.getEspaciosSolicitados()));
                                editEspacios.setVisibility(View.GONE);
                                labelMap.setText("Necesitas ver donde esta la parada?");
                                btnConfirmar.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MODIFY_WAYPOINT ){
            if(resultCode == RESULT_OK) {
                if(!data.getStringExtra("direccionParada").equals(txtPuntoDeParada.getText().toString())){
                    isModified = true;
                    txtPuntoDeParada.setText(data.getStringExtra("direccionParada"));
                    paradaModificada = Point.fromLngLat(data.getDoubleExtra("longitudParada",0),
                            data.getDoubleExtra("latitudParada",0));
                }
            }
        } else {
            Snackbar.make(getCurrentFocus(), "Se cancelo la seleccion de direccion", Toast.LENGTH_SHORT).show();
        }
    }
}
