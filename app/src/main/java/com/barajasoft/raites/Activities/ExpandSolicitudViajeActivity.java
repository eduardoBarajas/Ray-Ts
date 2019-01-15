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
import java.util.concurrent.atomic.AtomicInteger;

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
    private List<SolicitudViaje> solicitudes = new LinkedList<>();

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

        //cuando entra por el adaptador de viajes, y se pasa la llave entonces entra
        if(getIntent().hasExtra("KeySolicitud")){
            Toast.makeText(getApplicationContext(), getIntent().getStringExtra("KeySolicitud"), Toast.LENGTH_LONG).show();
            //se obtiene la solicitud actual con la KEY que se obtuvo
            solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.getValue(SolicitudViaje.class).getKey().equals(getIntent().getStringExtra("KeySolicitud"))){
                            solicitudActual = data.getValue(SolicitudViaje.class);
                        }
                        if(data.getValue(SolicitudViaje.class).getKeyViaje().equals(getIntent().getStringExtra("KeyViaje"))
                                &&data.getValue(SolicitudViaje.class).isAceptada()){
                            solicitudes.add(data.getValue(SolicitudViaje.class));
                        }
                    }
                    //una vez que se obtiene la llave se procede a obtener el viaje actual
                    viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                if(data.getValue(Viaje.class).getKey().equals(solicitudActual.getKeyViaje())){
                                    viajeActual = data.getValue(Viaje.class);
                                }
                            }
                            //una vez que se obtiene se procede a configurar los textviews
                            txtDestino.setText(viajeActual.getDireccionDestino());
                            txtSalida.setText(viajeActual.getDireccionSalida());
                            txtPuntoDeParada.setText(solicitudActual.getDireccionDeParada());
                            if(solicitudActual.isAceptada())
                                txtEstado.setText("Aceptada");
                            else
                                txtEstado.setText("Pendiente");
                            btnRegresar.setOnClickListener(e->{
                                finish();
                            });

                            btnMapa.setOnClickListener(e->{
                                Intent intent = new Intent(getApplicationContext() ,VisualizeTravelActivity.class);
                                intent.putExtra("visualizeMapParada", true);
                                intent.putExtra("KeySolicitud", solicitudActual.getKey());
//            if(!solicitud.isAceptada()){
//                //si no esta en modo edicion y la solicitud aun no es aceptada entonces se
//                //deben agregar los puntos manualmente para que puedan ser visualizados
//                intent.putExtra("direccionSolicitud", solicitud.getDireccionDeParada());
//                intent.putExtra("latitudSolicitud", solicitud.getPuntoDeParada().getLatitude());
//                intent.putExtra("longitudSolicitud", solicitud.getPuntoDeParada().getLongitude());
//            }
                                intent.putExtra("direccionDestino", viajeActual.getDireccionDestino());
                                intent.putExtra("direccionSalida", viajeActual.getDireccionSalida());
                                intent.putExtra("latitudSalida", viajeActual.getPuntosDeViaje().get(0).getLatitude());
                                intent.putExtra("longitudSalida", viajeActual.getPuntosDeViaje().get(0).getLongitude());
                                intent.putExtra("latitudDestino", viajeActual.getPuntosDeViaje().get(1).getLatitude());
                                intent.putExtra("longitudDestino", viajeActual.getPuntosDeViaje().get(1).getLongitude());
                                AtomicInteger var = new AtomicInteger(0);
                                //si el la solicitud actual aun no se acepta entonces se debe agregar manualmente
                                //pero debe ser solo para la solicitud actual

                                if(!solicitudActual.isAceptada())
                                    var.set(1);
                                String[] puntosArray = new String[viajeActual.getPuntosDeParada().size() + var.get()];
                                for(int i = 0; i<viajeActual.getPuntosDeParada().size(); i++){
                                    puntosArray[i] = String.valueOf(viajeActual.getPuntosDeParada().get(i).getLatitude())+":"+
                                            String.valueOf(viajeActual.getPuntosDeParada().get(i).getLongitude());
                                }
                                if(var.get() == 1)
                                    puntosArray[viajeActual.getPuntosDeParada().size()] = String.valueOf(solicitudActual.getPuntoDeParada().getLatitude())+":"+
                                            String.valueOf(solicitudActual.getPuntoDeParada().getLongitude());
                                intent.putExtra("puntosParada", puntosArray);
                                List<String> direccionesParada = new LinkedList<>();
                                for(SolicitudViaje s : solicitudes){
                                    direccionesParada.add(s.getDireccionDeParada());
                                }
                                String[] direccionesParadaArray = new String[direccionesParada.size() + var.get()];
                                for(int i = 0; i < direccionesParada.size(); i++){
                                    direccionesParadaArray[i] = direccionesParada.get(i);
                                }
                                if(var.get() == 1)
                                    direccionesParadaArray[direccionesParada.size()] = solicitudActual.getDireccionDeParada();
                                intent.putExtra("direccionesPuntosParada", direccionesParadaArray);
                                usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        List<User> usuarios = new LinkedList<>();
                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                                            if (data.getValue(User.class).getKey().equals(solicitudActual.getKeyPasajero())||
                                                    viajeActual.getKeysPasajeros().contains(data.getValue(User.class).getKey())) {
                                                //si el usuario esta en la lista de pasajeros entonces se agrega
                                                usuarios.add(data.getValue(User.class));
                                            }
                                        }
                                        List<String> nombres = new LinkedList<>();
                                        for(String keyUser : viajeActual.getKeysPasajeros()){
                                            for(User user : usuarios) {
                                                if(user.getKey().equals(keyUser)) {
                                                    nombres.add(user.getNombre());
                                                }
                                                if(user.getKey().equals(pref.getString("key", null))){
                                                    intent.putExtra("currentUserKey", user.getKey());
                                                    intent.putExtra("currentUserName", user.getNombre());
                                                }
                                                if(user.getKey().equals(solicitudActual.getKeyPasajero())){
                                                    intent.putExtra("currentSolicitudUserKey", user.getKey());
                                                    intent.putExtra("currentSolicitudUserName", user.getNombre());
                                                }
                                            }
                                        }
                                        if(var.get() == 1){
                                            for(User u : usuarios){
                                                if(u.getKey().equals(solicitudActual.getKeyPasajero())){
                                                    nombres.add(u.getNombre());
                                                    intent.putExtra("currentSolicitudUserKey", u.getKey());
                                                    intent.putExtra("currentSolicitudUserName", u.getNombre());
                                                }
                                            }
                                        }
                                        String[] nombresArray = new String[nombres.size()];
                                        for(int i = 0; i<nombres.size(); i++){
                                            nombresArray[i] = nombres.get(i);
                                        }
                                        intent.putExtra("usersParadas", nombresArray);
                                        if (getIntent().hasExtra("Edicion")) {
                                            intent.putExtra("editMapParada", true);
                                            startActivityForResult(intent, MODIFY_WAYPOINT);
                                        } else {
                                            startActivity(intent);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                                });
//                                Intent intent = new Intent(getApplicationContext() ,VisualizeTravelActivity.class);
//                                //si el mapa es solo para visualizar entonces
//                                intent.putExtra("visualizeMapParada", true);
//                                //si se quiere mandar toda la solicitud entonces
//                                intent.putExtra("KeySolicitud", solicitudActual.getKey());
//
//                                //se manda la informacion basica del viaje que en los dos casos es necesaria
//                                intent.putExtra("direccionDestino", viajeActual.getDireccionDestino());
//                                intent.putExtra("direccionSalida", viajeActual.getDireccionSalida());
//                                intent.putExtra("latitudSalida", viajeActual.getPuntosDeViaje().get(0).getLatitude());
//                                intent.putExtra("longitudSalida", viajeActual.getPuntosDeViaje().get(0).getLongitude());
//                                intent.putExtra("latitudDestino", viajeActual.getPuntosDeViaje().get(1).getLatitude());
//                                intent.putExtra("longitudDestino", viajeActual.getPuntosDeViaje().get(1).getLongitude());
//                                String[] puntosArray = new String[1];
//                                //se convierte a un array los puntos de parada que ya tiene el viaje
//                                puntosArray[0] = String.valueOf(solicitudActual.getPuntoDeParada().getLatitude())+":"+
//                                            String.valueOf(solicitudActual.getPuntoDeParada().getLongitude());
//                                intent.putExtra("puntosParada", puntosArray);
//
//                                //despues se busca si
//                                String[] direccionesParadaArray = new String[1];
//                                direccionesParadaArray[0] = solicitudActual.getDireccionDeParada();
//                                intent.putExtra("direccionesPuntosParada", direccionesParadaArray);
//                                //despues se busca a todos los usuarios que han solicitado el viaje actual
//                                usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                        String[] nombresArray = new String[1];
//                                        for (DataSnapshot data : dataSnapshot.getChildren()) {
//                                            if (data.getValue(User.class).getKey().equals(solicitudActual.getKeyPasajero())) {
//                                                //si el usuario esta en la lista de pasajeros entonces se agrega
//                                                nombresArray[0] = data.getValue(User.class).getNombre();
//                                            }
//                                            intent.putExtra("usersParadas", nombresArray);
//                                            if (getIntent().hasExtra("Edicion")) {
//                                                intent.putExtra("editMapParada", true);
//                                                startActivityForResult(intent, MODIFY_WAYPOINT);
//                                            } else {
//                                                startActivity(intent);
//                                            }
//                                        }
//                                    }
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError databaseError) { }
//                                });
                            });
                            if(getIntent().hasExtra("Edicion")){
                                txtEspacios.setVisibility(View.GONE);
                                editEspacios.setText(String.valueOf(solicitudActual.getEspaciosSolicitados()));
                                btnConfirmar.setOnClickListener(e->{
                                    if(isModified){
                                        //solo entra aqui si la direccion de la parada ha sido modificada
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
