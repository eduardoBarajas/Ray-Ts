package com.barajasoft.raites.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.AlertDialog;
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
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private OnPageChangeListener onPageChangeListener;
    private TextView nombre, email, telefono, txtSalida, txtDestino, txtHoraSalida, txtFechaSalida, txtVehiculo,
            txtMatricula, txtAsientos, txtAsientosLibres, labelAsientosSolicitados, txtAsientosSolicitados, labelParadas;
    private Button btnParada, btnViajeCompleto;
    private RecyclerView rvPasajeros;
    private CircularImageView profile;
    private String viajeAvaible = "";
    private boolean viewAvaible = false;
    private String currentConductorKey = "";
    private final String changedLabel = "Se realizaron cambios relacionados con el viaje, por favor espera a que el conductor lo corrija";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    if(getActivity().getIntent().hasExtra("KeyViaje")) {
                        if (dataSnapshot.getValue(Viaje.class).getKey().equals(getActivity().getIntent().getStringExtra("KeyViaje"))) {
                            currentConductorKey = dataSnapshot.getValue(Viaje.class).getKeyConductor();
                            AlertDialog dlg = new AlertDialog(getActivity(), "Viaje modificado", "El usuario que publico el viaje lo ha modificado, por favor revisa las especificaciones del viaje de nuevo");
                            dlg.show();
                            loadViajeDetails(dataSnapshot.getValue(Viaje.class));
                        }
                    }
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //agregar code por si eliminan el viaje
                if(viewAvaible) {
                    if(getActivity().getIntent().hasExtra("KeyViaje")) {
                        if (dataSnapshot.getValue(Viaje.class).getKey().equals(getActivity().getIntent().getStringExtra("KeyViaje"))) {
                            AlertDialog dlg = new AlertDialog(getActivity(), "Viaje eliminado", "El usuario que publico el viaje lo ha eliminado, seras direccionado a la pantalla anterior");
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
                if(dataSnapshot.getValue(Vehiculo.class).getUserKey().equals(currentConductorKey)){
                    validarVehiculo(dataSnapshot.getValue(Vehiculo.class));
                    loadVehiculo(dataSnapshot.getValue(Vehiculo.class).getMarca()+" "+
                            dataSnapshot.getValue(Vehiculo.class).getModelo(), dataSnapshot.getValue(Vehiculo.class).getMatricula());
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue(Vehiculo.class).getUserKey().equals(currentConductorKey)){
                    if(validarVehiculo(dataSnapshot.getValue(Vehiculo.class))){
                        //es valido
                        viajeAvaible = "La informacion del vehiculo ha sido modificada, por favor revisala de nuevo";
                        showDialogAlert("Vehiculo del viaje");
                    }else{
                        //no es valido
                        showDialogAlert("Vehiculo del viaje");
                    }
                    loadVehiculo(dataSnapshot.getValue(Vehiculo.class).getMarca()+" "+
                            dataSnapshot.getValue(Vehiculo.class).getModelo(), dataSnapshot.getValue(Vehiculo.class).getMatricula());
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //agregar code por si eliminan al carro
                if(dataSnapshot.getValue(Vehiculo.class).getUserKey().equals(currentConductorKey)){
                    viajeAvaible = "El usuario hizo elimino el vehiculo de este viaje, por favor espera a que el conductor lo corrija";
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
                if(dataSnapshot.getValue(User.class).getKey().equals(currentConductorKey)){
                    User user = dataSnapshot.getValue(User.class);
                    validarConductor(user);
                    loadUser(user.getNombre(), user.getImagenPerfil(), user.getTelefono(), user.getCorreo());
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getValue(User.class).getKey().equals(currentConductorKey)){
                    User user = dataSnapshot.getValue(User.class);
                    if(validarConductor(user)){
                        //es valido
                        viajeAvaible = "La informacion del conductor ha sido modificada, por favor revisala de nuevo";
                        showDialogAlert("Informacion del conductor");
                    }else{
                        //no es valido

                        showDialogAlert("Informacion del conductor");
                    }
                    loadUser(user.getNombre(), user.getImagenPerfil(), user.getTelefono(), user.getCorreo());
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
            AlertDialog dlg = new AlertDialog(getActivity(), title, viajeAvaible);
            dlg.show();
        }
    }

    private boolean validarVehiculo(Vehiculo value) {
        if(!value.isValidado()){
            if(value.getMatricula().isEmpty())
                value.setMatricula("No disponible");
            if(value.getModelo().isEmpty() || value.getMarca().isEmpty()) {
                value.setMarca("No ");
                value.setModelo("disponible");
            }
            return false;
        }else{
            viajeAvaible = "Not Changed";
            return true;
        }
    }

    private boolean validarConductor(User user) {
        if(!user.isValidadoPasajero()){
            viajeAvaible = changedLabel;
            if(user.getTelefono().isEmpty())
                user.setTelefono("No disponible");
            if(user.getNombre().isEmpty())
                user.setNombre("No disponible");
            return false;
        }else{
            viajeAvaible = "Not Changed";
            return true;
        }
    }

    private void loadUser(String name, String imagenPerfil, String tel, String correo) {
        nombre.setText(name);
        imageLoader.displayImage(imagenPerfil, profile, options);
        email.setText(correo);
        telefono.setText(tel);
        if(viajeAvaible.equals(changedLabel))
            disableAgendarButtons(false);
        else
            disableAgendarButtons(true);
    }

    private void loadVehiculo(String s, String matricula) {
        txtVehiculo.setText(s);
        txtMatricula.setText(matricula);
        if(viajeAvaible.equals(changedLabel))
            disableAgendarButtons(false);
        else
            disableAgendarButtons(true);
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
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("Detalles del viaje");
        Viaje viaje = new Viaje();
        if(getActivity().getIntent().hasExtra("KeyConductor")){
            Intent data = getActivity().getIntent();
            viaje.setKeyConductor(data.getStringExtra("KeyConductor"));
            viaje.setDireccionDestino(data.getStringExtra("Destino"));
            viaje.setDireccionSalida(data.getStringExtra("Salida"));
            viaje.setFechaViaje(data.getStringExtra("FechaSalida"));
            viaje.setHoraViaje(data.getStringExtra("HoraSalida"));
            viaje.setEspaciosDisponibles(data.getIntExtra("EspaciosDisponibles", 0));
        }
        currentConductorKey = viaje.getKeyConductor();
        loadViajeDetails(viaje);
    }

    private void loadViajeDetails(Viaje currentViaje) {
        if(currentViaje.getKeyConductor().equals(pref.getString("Key", null))){
            txtAsientosSolicitados.setVisibility(View.GONE);
            labelAsientosSolicitados.setVisibility(View.GONE);
            btnParada.setVisibility(View.GONE);
            btnViajeCompleto.setVisibility(View.GONE);
        }
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
        btnViajeCompleto.setOnClickListener(e->{
            Toast.makeText(getContext(), "Asignar viaje", Toast.LENGTH_LONG).show();
        });
        if(viajeAvaible.equals(changedLabel))
            disableAgendarButtons(false);
        else
            disableAgendarButtons(true);
    }

    private void disableAgendarButtons(boolean enabled){
        if(!enabled){
            labelParadas.setText("El viaje esta siendo revisado por el conductor, por favor vuelve a revisar mas tarde.");
            btnParada.setVisibility(View.GONE);
            btnViajeCompleto.setVisibility(View.GONE);
        }else{
            labelParadas.setText("No viajas hasta destino del conductor? Solicita un punto de parada");
            btnParada.setVisibility(View.VISIBLE);
            btnViajeCompleto.setVisibility(View.VISIBLE);
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
