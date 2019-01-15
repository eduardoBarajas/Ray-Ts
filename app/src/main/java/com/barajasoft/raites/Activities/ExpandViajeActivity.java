package com.barajasoft.raites.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Adapters.OtrosPasajerosAdapter;
import com.barajasoft.raites.Dialogs.AlertDialog;
import com.barajasoft.raites.Dialogs.YesOrNoChooserDialog;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Vehiculo;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExpandViajeActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private final int MODIFY_WAYPOINT = 142;
    private SharedPreferences.Editor editor;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference usuariosReference = database.getReference("Usuarios");
    private DatabaseReference vehiculosReference = database.getReference("Vehiculos");
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private TextView nombre, email, telefono, txtSalida, txtDestino, txtHoraSalida, txtFechaSalida, txtVehiculo,
            txtMatricula, txtAsientos, txtAsientosLibres, labelAsientosSolicitados, txtAsientosSolicitados, labelParadas;
    private Button btnParada, btnViajeCompleto, btnCancelar, btnEditarSolicitud;
    private RecyclerView rvPasajeros;
    private CircularImageView profile;
    private TextInputLayout unassignedTextLayout;
    private TextView labelAsientosUnassigned, lblParadasUnassigned, lblAssigned;
    private TextView labelCambios;
    private final String changedLabel = "Se realizaron cambios relacionados con el viaje, por favor espera a que el conductor lo corrija";
    private ResultListener listener;
    private String viajeStatus = "";
    private SolicitudViaje solicitudActual;
    private Viaje currentViaje;
    private boolean viewAvaible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        setContentView(R.layout.detalles_viaje_fragment);
        //initViewPager("ExpandedViaje");
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_image) // resource or drawable
                .showImageForEmptyUri(R.drawable.no_profile) // resource or drawable
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .build();
        imageLoader.init(ImageLoaderConfiguration.createDefault(getApplicationContext()));


        listener = new ResultListener() {
            @Override
            public void result(String tag, Object result) {
                if(tag.equals("CancelarSolicitud")){
                    if(((String)result).equals("Accept")){
                        solicitudesReference.child(getIntent().getStringExtra("KeySolicitud")).setValue(null, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                Map<String, Object> datos = new HashMap<>();
                                List<LatLng> nuevasParadas = currentViaje.getPuntosDeParada();
                                List<String> nuevasKeysUsuarios = currentViaje.getKeysPasajeros();
                                nuevasParadas.remove(nuevasKeysUsuarios.indexOf(pref.getString("key", null)));
                                nuevasKeysUsuarios.remove(nuevasKeysUsuarios.indexOf(pref.getString("key", null)));
                                datos.put("keysPasajeros", nuevasKeysUsuarios);
                                datos.put("puntosDeParada", nuevasParadas);
                                viajesReference.child(currentViaje.getKey()).updateChildren(datos, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Toast.makeText(getApplicationContext(), "Se cancelo la solicitud", Toast.LENGTH_SHORT);
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                }
            }
        };

//        viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot data : dataSnapshot.getChildren()){
//                    if(data.getValue(Viaje.class).getKey().equals(currentViaje.getKey()))
//                        currentViaje = data.getValue(Viaje.class);
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) { }
//        });



        viajesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(viewAvaible) {
                    if(currentViaje!=null) {
                        Viaje viajeModified = dataSnapshot.getValue(Viaje.class);
                        if (viajeModified.getKey().equals(currentViaje.getKey())) {
                            currentViaje = viajeModified;
                            viajeStatus = "El usuario que publico el viaje lo ha modificado, por favor revisa las especificaciones del viaje de nuevo";
                            showDialogAlert("Viaje modificado");
                            loadViajeDetails(viajeModified);
                        }
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //agregar code por si eliminan el viaje
                if(viewAvaible) {
                    if(currentViaje!=null) {
                        Viaje viajeEliminado = dataSnapshot.getValue(Viaje.class);
                        if (viajeEliminado.getKey().equals(currentViaje.getKey())) {
                            AlertDialog dlg = new AlertDialog(getParent(), "Viaje eliminado", "El usuario que publico el viaje lo ha eliminado, seras direccionado a la actividad anterior");
                            dlg.show();
                            dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    finish();
                                }
                            });
                        }
                    }
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        vehiculosReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Vehiculo vehiculo = dataSnapshot.getValue(Vehiculo.class);
                if(vehiculo.getUserKey().equals(currentViaje.getKeyConductor())){
                    validarVehiculo(vehiculo);
                    loadVehiculo(vehiculo.getMarca()+" "+ vehiculo.getModelo(), vehiculo.getMatricula());
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Vehiculo vehiculoModified = dataSnapshot.getValue(Vehiculo.class);
                if(vehiculoModified.getUserKey().equals(currentViaje.getKeyConductor())){
                    if(validarVehiculo(vehiculoModified)){
                        //es valido
                        viajeStatus = "La informacion del vehiculo ha sido modificada, por favor revisala de nuevo";
                        showDialogAlert("Vehiculo del viaje");
                    }else{
                        //no es valido
                        showDialogAlert("Vehiculo del viaje");
                    }
                    loadVehiculo(vehiculoModified.getMarca()+" "+vehiculoModified.getModelo(), vehiculoModified.getMatricula());
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //agregar code por si eliminan al carro
                Vehiculo vehiculoEliminado = dataSnapshot.getValue(Vehiculo.class);
                if(vehiculoEliminado.getUserKey().equals(currentViaje.getKeyConductor())){
                    viajeStatus = "El usuario hizo elimino el vehiculo de este viaje, por favor espera a que el conductor lo corrija";
                    showDialogAlert("Vehiculo del viaje");
                    loadVehiculo("No disponible", "No disponible");
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        usuariosReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);
                if(user.getKey().equals(currentViaje.getKeyConductor())){
                    validarConductor(user);
                    loadUser(user.getNombre(), user.getImagenPerfil(), user.getTelefono(), user.getCorreo());
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User userModified = dataSnapshot.getValue(User.class);
                if(userModified.getKey().equals(currentViaje.getKeyConductor())){
                    if(validarConductor(userModified)){
                        //es valido
                        viajeStatus = "La informacion del conductor ha sido modificada, por favor revisala de nuevo";
                        showDialogAlert("Informacion del conductor");
                    }else{
                        //no es valido
                        showDialogAlert("Informacion del conductor");
                    }
                    loadUser(userModified.getNombre(), userModified.getImagenPerfil(), userModified.getTelefono(), userModified.getCorreo());
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //agregar code por si se agrega una funcionalidad para eliminar al usuario
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        loadViews();
    }

    private void loadViews() {
        viewAvaible = true;
        btnCancelar = findViewById(R.id.btnCancelarAssigned);
        btnEditarSolicitud = findViewById(R.id.btnEditarSolicitudAssigned);
        labelCambios = findViewById(R.id.labelCambios);
        unassignedTextLayout = findViewById(R.id.txtLayoutUnassigned);
        labelAsientosUnassigned = findViewById(R.id.labelAsientosUnassigned);
        lblParadasUnassigned = findViewById(R.id.lblParadasUnassigned);
        lblAssigned = findViewById(R.id.lblAssigned);
        txtAsientosSolicitados = findViewById(R.id.txtNumeroAsientosSolicitados);
        profile = findViewById(R.id.expandedImage);
        nombre = findViewById(R.id.txtNombreConductor);
        email = findViewById(R.id.txtEmail);
        telefono = findViewById(R.id.txtTelefono);
        txtSalida = findViewById(R.id.txtSalida);
        txtDestino = findViewById(R.id.txtDestino);
        txtHoraSalida = findViewById(R.id.txtHoraSalida);
        txtMatricula = findViewById(R.id.txtMatricula);
        txtAsientosLibres = findViewById(R.id.txtAsientosLibres);
        txtAsientos = findViewById(R.id.txtAsientos);
        txtFechaSalida = findViewById(R.id.txtFechaSalida);
        txtVehiculo = findViewById(R.id.txtVehiculo);
        btnParada = findViewById(R.id.btnParadaUnassigned);
        btnViajeCompleto = findViewById(R.id.btnViajeCompletoUnassigned);
        rvPasajeros = findViewById(R.id.rvPasajeros);
        rvPasajeros.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL, false);
        rvPasajeros.setLayoutManager(manager);
        rvPasajeros.setItemAnimator(new DefaultItemAnimator());
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Detalles del viaje");

        //new loadViajeDataFromServer(this).execute();
        if(getIntent().hasExtra("KeySolicitud")) {
            solicitudActual = new SolicitudViaje();
            solicitudActual.setKey(getIntent().getStringExtra("KeySolicitud"));
        }
//            solicitudActual.setDireccionDeParada(getIntent().getStringExtra("direccionSolicitud"));
//            solicitudActual.setPuntoDeParada(new LatLng(getIntent().getDoubleExtra("latitudSolicitud", 0),
//                    getIntent().getDoubleExtra("longitudSolicitud", 0)));
        if(getIntent().hasExtra("KeyConductor")){
            Intent data = getIntent();
            currentViaje = new Viaje();
            currentViaje.setKey(getIntent().getStringExtra("KeyViaje"));
            currentViaje.setKeyConductor(data.getStringExtra("KeyConductor"));
            currentViaje.setDireccionDestino(data.getStringExtra("Destino"));
            currentViaje.setDireccionSalida(data.getStringExtra("Salida"));
            currentViaje.setFechaViaje(data.getStringExtra("FechaSalida"));
            currentViaje.setHoraViaje(data.getStringExtra("HoraSalida"));
            currentViaje.getPuntosDeViaje().add(new LatLng(data.getDoubleExtra("latitudSalida", 0),
                    data.getDoubleExtra("longitudSalida", 0)));
            currentViaje.getPuntosDeViaje().add(new LatLng(data.getDoubleExtra("latitudDestino", 0),
                    data.getDoubleExtra("longitudDestino", 0)));
            currentViaje.setEspaciosDisponibles(data.getIntExtra("EspaciosDisponibles", 0));
            viajesReference.child(currentViaje.getKey()).child("puntosDeParada").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot d : dataSnapshot.getChildren()){
                        currentViaje.getPuntosDeParada().add(d.getValue(LatLng.class));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            for(String key : data.getStringArrayExtra("PasajerosKeys"))
                currentViaje.getKeysPasajeros().add(key);
        }
        loadViajeDetails(currentViaje);
    }

    private boolean validarVehiculo(Vehiculo value) {
        if(!value.isValidado()){
            viajeStatus = changedLabel;
            if(value.getMatricula().isEmpty())
                value.setMatricula("No disponible");
            if(value.getModelo().isEmpty() || value.getMarca().isEmpty()) {
                value.setMarca("No ");
                value.setModelo("disponible");
            }
            return false;
        }else{
            viajeStatus = "Valid";
            return true;
        }
    }

    private boolean validarConductor(User user) {
        if(!user.isValidadoPasajero()){
            viajeStatus = changedLabel;
            if(user.getTelefono().isEmpty())
                user.setTelefono("No disponible");
            if(user.getNombre().isEmpty())
                user.setNombre("No disponible");
            return false;
        }else{
            viajeStatus = "Valid";
            return true;
        }
    }

    private void loadUser(String name, String imagenPerfil, String tel, String correo) {
        nombre.setText(name);
        imageLoader.displayImage(imagenPerfil, profile, options);
        email.setText(correo);
        telefono.setText(tel);
        updateUIControls();
    }

    private void loadVehiculo(String s, String matricula) {
        txtVehiculo.setText(s);
        txtMatricula.setText(matricula);
        updateUIControls();
    }
    private void loadViajeDetails(Viaje currentViaje) {
        txtDestino.setText(currentViaje.getDireccionDestino());
        txtSalida.setText(currentViaje.getDireccionSalida());
        txtFechaSalida.setText(currentViaje.getFechaViaje());
        txtHoraSalida.setText(currentViaje.getHoraViaje());
        txtAsientos.setText(String.valueOf(currentViaje.getEspaciosDisponibles()));
        btnParada.setOnClickListener(e->{
            if(!txtAsientosSolicitados.getText().toString().isEmpty()){
                //se llama edit map
                Intent intent = new Intent(getApplicationContext() ,VisualizeTravelActivity.class);
                intent.putExtra("visualizeMapParada", true);
                intent.putExtra("editMapParada", true);
                intent.putExtra("direccionDestino", currentViaje.getDireccionDestino());
                intent.putExtra("direccionSalida", currentViaje.getDireccionSalida());
                intent.putExtra("latitudSalida", currentViaje.getPuntosDeViaje().get(0).getLatitude());
                intent.putExtra("longitudSalida", currentViaje.getPuntosDeViaje().get(0).getLongitude());
                intent.putExtra("latitudDestino", currentViaje.getPuntosDeViaje().get(1).getLatitude());
                intent.putExtra("longitudDestino", currentViaje.getPuntosDeViaje().get(1).getLongitude());

                if(currentViaje.getPuntosDeParada()!=null){
                    String[] puntosArray = new String[currentViaje.getPuntosDeParada().size()];
                    for(int i = 0; i<currentViaje.getPuntosDeParada().size(); i++){
                        puntosArray[i] = String.valueOf(currentViaje.getPuntosDeParada().get(i).getLatitude())+":"+
                                String.valueOf(currentViaje.getPuntosDeParada().get(i).getLongitude());
                    }
                    for(String s : puntosArray){
                        Log.e("Puntos Array", s);
                    }
                    intent.putExtra("puntosParada", puntosArray);
                }
                solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> direccionesParada = new LinkedList<>();
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            if(data.getValue(SolicitudViaje.class).getKeyViaje().equals(currentViaje.getKey())){
                                direccionesParada.add(data.getValue(SolicitudViaje.class).getDireccionDeParada());
                            }
                        }
                        String[] direccionesParadaArray = new String[direccionesParada.size()];
                        for(int i = 0; i < direccionesParada.size(); i++){
                            direccionesParadaArray[i] = direccionesParada.get(i);
                        }
                        for(String s : direccionesParadaArray){
                            Log.e("Direccion Array", s);
                        }
                        intent.putExtra("direccionesPuntosParada", direccionesParadaArray);
                        usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                List<String> nombres = new LinkedList<>();
                                for(DataSnapshot data : dataSnapshot.getChildren()){
                                    for(String keyUser : currentViaje.getKeysPasajeros()){
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
                                for(String s : nombresArray){
                                    Log.e("Nombres Array", s);
                                }
                                intent.putExtra("usersParadas", nombresArray);
                                startActivityForResult(intent, MODIFY_WAYPOINT);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }else{
                Toast.makeText(getApplicationContext(), "Primero debes seleccionar el numero de asientos que necesitas", Toast.LENGTH_LONG).show();
            }
        });
        //poner a los pasajeros
        if(currentViaje.getKeysPasajeros().size() > 0){
            OtrosPasajerosAdapter adapter = new OtrosPasajerosAdapter(getApplicationContext(), currentViaje.getKeysPasajeros());
            rvPasajeros.setAdapter(adapter);
            rvPasajeros.setVisibility(View.VISIBLE);
            txtAsientosLibres.setVisibility(View.GONE);
        }else{
            txtAsientosLibres.setVisibility(View.VISIBLE);
            rvPasajeros.setVisibility(View.GONE);
        }
        btnViajeCompleto.setOnClickListener(e->{
            Toast.makeText(getApplicationContext(), "Asignar viaje", Toast.LENGTH_LONG).show();
        });

        btnEditarSolicitud.setOnClickListener(e->{
            Intent intent = new Intent(getApplicationContext(), ExpandSolicitudViajeActivity.class);
            intent.putExtra("Edicion", "true");
            intent.putExtra("KeySolicitud", solicitudActual.getKey());
            startActivity(intent);
        });
        btnCancelar.setOnClickListener(e->{
            YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(ExpandViajeActivity.this, "CancelarSolicitud", "Cancelar Solicitud De VIaje",
                    "Estas seguro que quieres cancelar la solicitud?", listener);
            dlg.show();
        });

        updateUIControls();
    }

    private void updateUIControls(){
        if(viajeStatus.equals(changedLabel)){
            //checa si se ha cambiado algo en la informacion del viaje
            labelCambios.setVisibility(View.VISIBLE);
            setAssignedControls(false, false);
            setUnassignedControls(false);
            Log.e("Entro x1", "Segun entro por que hubo cambios");
        }else{
            //aqui si entra si no ha habido cambios
            if(currentViaje.getKeyConductor().equals(pref.getString("key", null))){
                //si soy el conductor entonces no deben salir los botones de agendar
                labelCambios.setVisibility(View.GONE);
                setAssignedControls(true, true);
                setUnassignedControls(false);
                Log.e("Entro x1", "Segun entro por que Era el conductor");
            } else{
                boolean isAssigned = false;
                for(String keys : currentViaje.getKeysPasajeros()){
                    if(keys.equals(pref.getString("key", null))){
                        Log.e("Entro x1", "Segun entro por que era pasajero");
                        labelCambios.setVisibility(View.GONE);
                        setAssignedControls(true, false);
                        setUnassignedControls(false);
                        isAssigned = true;
                    }
                }
                if(!isAssigned){
                    labelCambios.setVisibility(View.GONE);
                    setAssignedControls(false, false);
                    setUnassignedControls(true);
                    Log.e("Entro x1", "Segun entro por que no es ni pasajero, ni conductor ni hubo cambios");
                }
            }
        }
    }

    private void setAssignedControls(boolean active, boolean isConductor){
        if(active){
            if(isConductor){
                btnEditarSolicitud.setVisibility(View.GONE);
                lblAssigned.setText("Cambio de planes? Puedes cancelar el viaje y publicar uno nuevo.");
            }else{
                btnEditarSolicitud.setVisibility(View.VISIBLE);
                lblAssigned.setText("Cambio de planes? actualiza o cancela la solicitud para este viaje antes de que parta");
            }
            lblAssigned.setVisibility(View.VISIBLE);
            btnCancelar.setVisibility(View.VISIBLE);
        }else {
            lblAssigned.setVisibility(View.GONE);
            btnCancelar.setVisibility(View.GONE);
            btnEditarSolicitud.setVisibility(View.GONE);
        }
    }
    private void setUnassignedControls(boolean active){
        if(active){
            unassignedTextLayout.setVisibility(View.VISIBLE);
            labelAsientosUnassigned.setVisibility(View.VISIBLE);
            lblParadasUnassigned.setVisibility(View.VISIBLE);
            btnParada.setVisibility(View.VISIBLE);
            btnViajeCompleto.setVisibility(View.VISIBLE);
        }else {
            unassignedTextLayout.setVisibility(View.GONE);
            labelAsientosUnassigned.setVisibility(View.GONE);
            lblParadasUnassigned.setVisibility(View.GONE);
            btnParada.setVisibility(View.GONE);
            btnViajeCompleto.setVisibility(View.GONE);
        }
    }

    private void showDialogAlert(String title) {
        if(viewAvaible){
            AlertDialog dlg = new AlertDialog(getParent(), title, viajeStatus);
            dlg.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewAvaible = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MODIFY_WAYPOINT && resultCode == RESULT_OK) {
            SolicitudViaje solicitudViaje = new SolicitudViaje();
            solicitudViaje.setAceptada(false);
            solicitudViaje.setEspaciosSolicitados(Integer.parseInt(txtAsientosSolicitados.getText().toString()));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            solicitudViaje.setFechaSolicitud(simpleDateFormat.format(Calendar.getInstance().getTime()));
            solicitudViaje.setKeyPasajero(pref.getString("key", null));
            solicitudViaje.setPuntoDeParada(new LatLng(data.getDoubleExtra("latitudParada", 0), data.getDoubleExtra("longitudParada", 0)));
            solicitudViaje.setKeyViaje(currentViaje.getKey());
            solicitudViaje.setDireccionDeParada(data.getStringExtra("direccionParada"));
            solicitudesReference.child(solicitudViaje.getKey()).setValue(solicitudViaje, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Toast.makeText(getApplicationContext(), "Se envio la solicitud", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }else {
            Snackbar.make(getCurrentFocus(), "Se cancelo la seleccion de direccion", Toast.LENGTH_SHORT).show();
        }
    }
}