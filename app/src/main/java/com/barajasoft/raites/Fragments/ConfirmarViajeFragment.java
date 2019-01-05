package com.barajasoft.raites.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.barajasoft.raites.Activities.MainMenuActivity;
import com.barajasoft.raites.Activities.VisualizeTravelActivity;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.LinkedList;
import java.util.List;

public class ConfirmarViajeFragment extends Fragment {
    private OnPageChangeListener listener;
    private SharedPreferences pref;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference ciudadesReference = database.getReference("Ciudades");
    private List<String> cities = new LinkedList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnPageChangeListener)context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.confirmar_viaje_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton btnBack = view.findViewById(R.id.btnBack);
        Button btnTrayecto = view.findViewById(R.id.trayectoButton);
        Button btnConfirmar = view.findViewById(R.id.confirmarButton);
        btnTrayecto.setOnClickListener(e->{
            Intent intent = new Intent(getContext() ,VisualizeTravelActivity.class);
            intent.putExtra("direccionDestino", pref.getString("direccionDestino", null));
            intent.putExtra("direccionSalida", pref.getString("direccionInicio", null));
            intent.putExtra("latitudSalida", Double.parseDouble(pref.getString("latitudInicio", null)));
            intent.putExtra("longitudSalida", Double.parseDouble(pref.getString("longitudInicio", null)));
            intent.putExtra("latitudDestino", Double.parseDouble(pref.getString("latitudDestino", null)));
            intent.putExtra("longitudDestino", Double.parseDouble(pref.getString("longitudDestino", null)));
            startActivity(intent);
        });
        btnConfirmar.setOnClickListener(e->{
            //Agendar viaje
            Viaje viaje = new Viaje();
            viaje.setDireccionDestino(pref.getString("direccionDestino", null));
            viaje.setDireccionSalida(pref.getString("direccionInicio", null));
            viaje.setEspaciosDisponibles(pref.getInt("roomSelected", -1));
            viaje.setFechaViaje(pref.getString("FechaSeleccionada", null));
            viaje.setFechaPublicacion(pref.getString("FechaPublicada", null));
            viaje.setHoraViaje(pref.getString("HoraSeleccionada", null));
            if(pref.getString("typeSelected", null).equals("Conductor")){
                viaje.setKeyConductor(pref.getString("key", null));
            }else{
                viaje.setKeyConductor("No definido");
                List<String> pasajeros = new LinkedList<>();
                pasajeros.add(pref.getString("key", null));
                viaje.setKeysPasajeros(pasajeros);
            }
            List<LatLng> puntos = new LinkedList<>();
            puntos.add(new LatLng(Double.parseDouble(pref.getString("latitudInicio", null)), Double.parseDouble(pref.getString("longitudInicio", null))));
            puntos.add(new LatLng(Double.parseDouble(pref.getString("latitudDestino", null)), Double.parseDouble(pref.getString("longitudDestino", null))));
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
                            if(!cities.contains(pref.getString("direccionInicio", null).split(",")[1].substring(1))){
                                cities.add(pref.getString("direccionInicio", null).split(",")[1].substring(1));
                            }
                            if(!cities.contains(pref.getString("direccionDestino", null).split(",")[1].substring(1))){
                                cities.add(pref.getString("direccionDestino", null).split(",")[1].substring(1));
                            }
                            sizeFinal = cities.size();
                            if(sizeFinal > sizeInicial){
                                ciudadesReference.setValue(cities, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Toast.makeText(getContext(), "Se publico el viaje correctamente",Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getContext(), MainMenuActivity.class));
                                        getActivity().finish();
                                    }
                                });
                            }else{
                                Toast.makeText(getContext(),"Se publico el viaje correctamente",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getContext(), MainMenuActivity.class));
                                getActivity().finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            });
        });
        btnBack.setOnClickListener(e->{
            listener.pageChanged(3);
        });
    }
}
