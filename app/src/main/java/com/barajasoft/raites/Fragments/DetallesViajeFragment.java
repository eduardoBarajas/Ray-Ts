package com.barajasoft.raites.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Activities.ExpandSolicitudViajeActivity;
import com.barajasoft.raites.Adapters.OtrosPasajerosAdapter;
import com.barajasoft.raites.Dialogs.AlertDialog;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Vehiculo;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class DetallesViajeFragment extends BaseFragment {

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference usuariosReference = database.getReference("Usuarios");
    private DatabaseReference vehiculosReference = database.getReference("Vehiculos");
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private OnPageChangeListener onPageChangeListener;
    private TextView nombre, email, telefono, txtSalida, txtDestino, txtHoraSalida, txtFechaSalida, txtVehiculo,
            txtMatricula, txtAsientos, txtAsientosLibres, labelAsientosSolicitados, txtAsientosSolicitados, labelParadas;
    private Button btnParada, btnViajeCompleto, btnCancelar, btnEditarSolicitud;
    private RecyclerView rvPasajeros;
    private CircularImageView profile;
    private LinearLayout unassigned, assigned;
    private TextView labelCambios;
    private final String changedLabel = "Se realizaron cambios relacionados con el viaje, por favor espera a que el conductor lo corrija";

    private String viajeStatus = "";
    private Viaje currentViaje;
    private boolean viewAvaible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentViaje = new Viaje();
        currentViaje.setKey(getActivity().getIntent().getStringExtra("KeyViaje"));
        viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(data.getValue(Viaje.class).getKey().equals(currentViaje.getKey()))
                        currentViaje = data.getValue(Viaje.class);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
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
        imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
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
                            AlertDialog dlg = new AlertDialog(getActivity(), "Viaje eliminado", "El usuario que publico el viaje lo ha eliminado, seras direccionado a la actividad anterior");
                            dlg.show();
                            dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    getActivity().finish();
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
    }

    private void showDialogAlert(String title) {
        if(viewAvaible){
            AlertDialog dlg = new AlertDialog(getActivity(), title, viajeStatus);
            dlg.show();
        }
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detalles_viaje_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewAvaible = true;
        btnCancelar = view.findViewById(R.id.btnCancelar);
        btnEditarSolicitud = view.findViewById(R.id.btnEditarSolicitud);
        labelCambios = view.findViewById(R.id.labelCambios);
        unassigned = view.findViewById(R.id.unassigned);
        assigned = view.findViewById(R.id.assigned);
        labelParadas = view.findViewById(R.id.lblParadas);
        txtAsientosSolicitados = view.findViewById(R.id.txtNumeroAsientosSolicitados);
        labelAsientosSolicitados = view.findViewById(R.id.labelCuantosAsientos);
        profile = view.findViewById(R.id.expandedImage);
        nombre = view.findViewById(R.id.txtNombreConductor);
        email = view.findViewById(R.id.txtEmail);
        telefono = view.findViewById(R.id.txtTelefono);
        txtSalida = view.findViewById(R.id.txtSalida);
        txtDestino = view.findViewById(R.id.txtDestino);
        txtHoraSalida = view.findViewById(R.id.txtHoraSalida);
        txtMatricula = view.findViewById(R.id.txtMatricula);
        txtAsientosLibres = view.findViewById(R.id.txtAsientosLibres);
        txtAsientos = view.findViewById(R.id.txtAsientos);
        txtFechaSalida = view.findViewById(R.id.txtFechaSalida);
        txtVehiculo = view.findViewById(R.id.txtVehiculo);
        btnParada = view.findViewById(R.id.btnParada);
        btnViajeCompleto = view.findViewById(R.id.btnViajeCompleto);
        rvPasajeros = view.findViewById(R.id.rvPasajeros);
        rvPasajeros.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
        rvPasajeros.setLayoutManager(manager);
        rvPasajeros.setItemAnimator(new DefaultItemAnimator());
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Detalles del viaje");
        if(getActivity().getIntent().hasExtra("KeyConductor")){
            Intent data = getActivity().getIntent();
            currentViaje.setKeyConductor(data.getStringExtra("KeyConductor"));
            currentViaje.setDireccionDestino(data.getStringExtra("Destino"));
            currentViaje.setDireccionSalida(data.getStringExtra("Salida"));
            currentViaje.setFechaViaje(data.getStringExtra("FechaSalida"));
            currentViaje.setHoraViaje(data.getStringExtra("HoraSalida"));
            currentViaje.setEspaciosDisponibles(data.getIntExtra("EspaciosDisponibles", 0));
            for(String key : data.getStringArrayExtra("PasajerosKeys"))
                currentViaje.getKeysPasajeros().add(key);
        }
        loadViajeDetails(currentViaje);
    }

    private void loadViajeDetails(Viaje currentViaje) {
        txtDestino.setText(currentViaje.getDireccionDestino());
        txtSalida.setText(currentViaje.getDireccionSalida());
        txtFechaSalida.setText(currentViaje.getFechaViaje());
        txtHoraSalida.setText(currentViaje.getHoraViaje());
        txtAsientos.setText(String.valueOf(currentViaje.getEspaciosDisponibles()));
        btnParada.setOnClickListener(e->{
            if(!txtAsientosSolicitados.getText().toString().isEmpty()){
                editor.putInt("AsientosSolicitados", Integer.parseInt(txtAsientosSolicitados.getText().toString()));
                editor.commit();
                onPageChangeListener.pageChanged(1);
            }else{
                Toast.makeText(getContext(), "Primero debes seleccionar el numero de asientos que necesitas", Toast.LENGTH_LONG).show();
            }
        });
        //poner a los pasajeros
        if(currentViaje.getKeysPasajeros().size() > 0){
            OtrosPasajerosAdapter adapter = new OtrosPasajerosAdapter(getContext(), currentViaje.getKeysPasajeros());
            rvPasajeros.setAdapter(adapter);
            rvPasajeros.setVisibility(View.VISIBLE);
            txtAsientosLibres.setVisibility(View.GONE);
        }else{
            txtAsientosLibres.setVisibility(View.VISIBLE);
            rvPasajeros.setVisibility(View.GONE);
        }
        btnViajeCompleto.setOnClickListener(e->{
            Toast.makeText(getContext(), "Asignar viaje", Toast.LENGTH_LONG).show();
        });
        btnEditarSolicitud.setOnClickListener(e->{
            solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.getValue(SolicitudViaje.class).getKeyPasajero().equals(pref.getString("key", null))){
                            Intent intent = new Intent(getContext(), ExpandSolicitudViajeActivity.class);
                            intent.putExtra("Edicion", "true");
                            intent.putExtra("solicitud_key", data.getValue(SolicitudViaje.class).getKey());
                            startActivity(intent);
                            break;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        });
        btnCancelar.setOnClickListener(e->{
        });
        updateUIControls();
    }

    private void updateUIControls(){
        if(viajeStatus.equals(changedLabel)){
            //checa si se ha cambiado algo en la informacion del viaje
            labelCambios.setVisibility(View.VISIBLE);
            assigned.setVisibility(View.GONE);
            unassigned.setVisibility(View.GONE);
            Log.e("Entro x1", "Segun entro por que hubo cambios");
        }else{
            //aqui si entra si no ha habido cambios
            if(currentViaje.getKeyConductor().equals(pref.getString("key", null))){
                //si soy el conductor entonces no deben salir los botones de agendar
                labelCambios.setVisibility(View.GONE);
                unassigned.setVisibility(View.GONE);
                assigned.setVisibility(View.VISIBLE);
                Log.e("Entro x1", "Segun entro por que Era el conductor");
            } else{
                boolean isAssigned = false;
                for(String keys : currentViaje.getKeysPasajeros()){
                    if(keys.equals(pref.getString("key", null))){
                        Log.e("Entro x1", "Segun entro por que era pasajero");
                        labelCambios.setVisibility(View.GONE);
                        unassigned.setVisibility(View.GONE);
                        assigned.setVisibility(View.VISIBLE);
                        isAssigned = true;
                    }
                }
                if(!isAssigned){
                    labelCambios.setVisibility(View.GONE);
                    unassigned.setVisibility(View.VISIBLE);
                    assigned.setVisibility(View.GONE);
                    Log.e("Entro x1", "Segun entro por que no es ni pasajero, ni conductor ni hubo cambios");
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewAvaible = false;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void setListener(OnPageChangeListener listener){
        onPageChangeListener = listener;
    }

}
