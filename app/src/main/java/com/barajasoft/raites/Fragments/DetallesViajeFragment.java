package com.barajasoft.raites.Fragments;

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

import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Vehiculo;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;
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
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private OnPageChangeListener onPageChangeListener;
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detalles_viaje_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView nombre, email, telefono, txtSalida, txtDestino, txtHoraSalida, txtFechaSalida, txtVehiculo,
        txtMatricula, txtAsientos, txtAsientosLibres, labelAsientosSolicitados, txtAsientosSolicitados;
        Button btnParada, btnViajeCompleto;
        RecyclerView rvPasajeros;
        CircularImageView profile;

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
        toolbar.setTitle("Raites");
        toolbar.setSubtitle("Detalles del viaje");
        if(getActivity().getIntent().hasExtra("KeyConductor")){
            Intent data = getActivity().getIntent();
            if(data.getStringExtra("KeyConductor").equals(pref.getString("Key", null))){
                txtAsientosSolicitados.setVisibility(View.GONE);
                labelAsientosSolicitados.setVisibility(View.GONE);
                btnParada.setVisibility(View.GONE);
                btnViajeCompleto.setVisibility(View.GONE);
            }
            //nombre.setText(data.getStringExtra("Nombre"));
            txtDestino.setText(data.getStringExtra("Destino"));
            txtSalida.setText(data.getStringExtra("Salida"));
            txtFechaSalida.setText(data.getStringExtra("FechaSalida"));
            txtHoraSalida.setText(data.getStringExtra("HoraSalida"));
            txtAsientos.setText(String.valueOf(data.getIntExtra("EspaciosDisponibles", 0)));
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
            //get KeyConductor
            //get PasajerosKeys
            usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot user : dataSnapshot.getChildren()){
                        if(user.getValue(User.class).getKey().equals(data.getStringExtra("KeyConductor"))){
                            User current = user.getValue(User.class);
                            nombre.setText(current.getNombre());
                            imageLoader.displayImage(current.getImagenPerfil(), profile, options);
                            email.setText(current.getCorreo());
                            telefono.setText(current.getTelefono());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
            vehiculosReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot v : dataSnapshot.getChildren()){
                        if(v.getValue(Vehiculo.class).getUserKey().equals(data.getStringExtra("KeyConductor"))){
                            Vehiculo vehiculo = v.getValue(Vehiculo.class);
                            txtVehiculo.setText(vehiculo.getMarca()+" "+vehiculo.getModelo());
                            txtMatricula.setText(vehiculo.getMatricula());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
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
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void setListener(OnPageChangeListener listener){
        onPageChangeListener = listener;
    }

}
