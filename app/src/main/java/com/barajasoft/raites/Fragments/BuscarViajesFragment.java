package com.barajasoft.raites.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Adapters.ViajesAdapter;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BuscarViajesFragment extends BaseFragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference ciudadesReference = database.getReference("Ciudades");
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private ViajesAdapter viajesAdapter;
    private ImageView imageNoViajes;
    private TextView txtNoViajes;
    private RecyclerView rvViajes;
    private String desde = "", hasta = "";
    private ArrayAdapter<String> ciudadesData;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ciudadesData = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new LinkedList<>());
        ciudadesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ciudadesData.add("");
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    ciudadesData.add(data.getValue(String.class));
                }
                ciudadesData.notifyDataSetChanged();
                ciudadesData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        viajesAdapter = new ViajesAdapter(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.buscar_viajes_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageNoViajes = view.findViewById(R.id.imageNoViajes);
        txtNoViajes = view.findViewById(R.id.textNoViajes);
        rvViajes = view.findViewById(R.id.rvViajes);
        rvViajes.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false);
        rvViajes.setLayoutManager(manager);
        rvViajes.setItemAnimator(new DefaultItemAnimator());
        rvViajes.setAdapter(viajesAdapter);
        Spinner ciudadDesde, ciudadHasta;
        ciudadDesde = view.findViewById(R.id.spinnerDesde);
        ciudadHasta = view.findViewById(R.id.spinnerHasta);
        ciudadDesde.setAdapter(ciudadesData);
        ciudadHasta.setAdapter(ciudadesData);
        ciudadDesde.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                desde = adapterView.getItemAtPosition(i).toString();
                viajesAdapter.clear();
                buscarViajes();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        ciudadHasta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                hasta = adapterView.getItemAtPosition(i).toString();
                viajesAdapter.clear();
                buscarViajes();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        isViajesEmpty();
    }

    private void buscarViajes() {
        if(desde.isEmpty()&&hasta.isEmpty()){
            rvViajes.setVisibility(View.GONE);
            txtNoViajes.setText("Selecciona desde donde comienza o hasta donde va el viaje para ver si se encuentran disponibles");
            imageNoViajes.setVisibility(View.VISIBLE);
            txtNoViajes.setVisibility(View.VISIBLE);
           return;
        }
        viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Viaje viajeEncontrado = data.getValue(Viaje.class);
                    if(!desde.isEmpty()){
                        if(!hasta.isEmpty()){
                            //tiene los dos
                            if(viajeEncontrado.getDireccionSalida().contains(desde) && viajeEncontrado.getDireccionDestino().contains(hasta)){
                                viajesAdapter.addViaje(viajeEncontrado);
                                hasSolicitudes(viajeEncontrado);
                            }
                        }else{
                            //solo desde
                            if(viajeEncontrado.getDireccionSalida().contains(desde)){
                                viajesAdapter.addViaje(data.getValue(Viaje.class));
                                hasSolicitudes(viajeEncontrado);
                            }
                        }
                    }else{
                        //solo destino
                        if(viajeEncontrado.getDireccionDestino().contains(hasta)){
                            viajesAdapter.addViaje(data.getValue(Viaje.class));
                            hasSolicitudes(viajeEncontrado);
                        }
                    }
                }
                viajesAdapter.notifyDataSetChanged();
                isViajesEmpty();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void hasSolicitudes(Viaje viajeEncontrado){
        AtomicBoolean solicitudFound = new AtomicBoolean(false);
        solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot solicitud: dataSnapshot.getChildren()){
                    if(solicitud.getValue(SolicitudViaje.class).getKeyViaje().equals(viajeEncontrado.getKey())){
                        viajesAdapter.addSolicitudViaje(viajeEncontrado.getKey(), solicitud.getValue(SolicitudViaje.class));
                        solicitudFound.set(true);
                    }
                }
                if(solicitudFound.get()){
                    viajesAdapter.notifyDataSetChanged();
                    isViajesEmpty();
                    solicitudFound.set(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void isViajesEmpty() {
        if(viajesAdapter.getItemCount()==0){
            rvViajes.setVisibility(View.GONE);
            imageNoViajes.setVisibility(View.VISIBLE);
            txtNoViajes.setVisibility(View.VISIBLE);
            txtNoViajes.setText("No se encontraron viajes para esta trayectoria.");
        }else{
            rvViajes.setVisibility(View.VISIBLE);
            imageNoViajes.setVisibility(View.GONE);
            txtNoViajes.setVisibility(View.GONE);
        }
    }

}
