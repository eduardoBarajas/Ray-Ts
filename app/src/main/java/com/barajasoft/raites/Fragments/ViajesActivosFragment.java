package com.barajasoft.raites.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Adapters.ViajesAdapter;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class ViajesActivosFragment extends BaseFragment {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private Button btnAgendarViaje;
    private ImageView imageNoViajes;
    private TextView txtNoViajes;
    private ViajesAdapter viajesAdapter;
    private RecyclerView rvViajes;
    private OnPageChangeListener onPageChangeListener;
    private SharedPreferences pref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        viajesAdapter = new ViajesAdapter(getContext());
        viajesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Viaje currentViaje = dataSnapshot.getValue(Viaje.class);
                if(currentViaje.getKeyConductor().equals(pref.getString("Key", null))){
                    viajesAdapter.addViaje(currentViaje);
                    viajesAdapter.notifyDataSetChanged();
                    isViajesEmpty();
                }else{
                    for(String pasajero : currentViaje.getKeysPasajeros()){
                        if(pasajero.equals(pref.getString("Key", null))){
                            viajesAdapter.addViaje(currentViaje);
                            viajesAdapter.notifyDataSetChanged();
                            isViajesEmpty();
                        }
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Viaje currentViaje = dataSnapshot.getValue(Viaje.class);
                viajesAdapter.removeViaje(currentViaje.getKey());
                viajesAdapter.notifyDataSetChanged();
                isViajesEmpty();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.viajes_activos_fragment, container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnAgendarViaje = view.findViewById(R.id.btnNoViajes);
        imageNoViajes = view.findViewById(R.id.imageNoViajes);
        txtNoViajes = view.findViewById(R.id.textNoViajes);
        rvViajes = view.findViewById(R.id.rvViajesActivos);
        rvViajes.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL, false);
        rvViajes.setLayoutManager(manager);
        rvViajes.setItemAnimator(new DefaultItemAnimator());
        rvViajes.setAdapter(viajesAdapter);
        isViajesEmpty();
        btnAgendarViaje.setOnClickListener(e->{
            onPageChangeListener.pageChanged(1);
        });
    }

    private void isViajesEmpty() {
        if(viajesAdapter.getItemCount()==0){
            rvViajes.setVisibility(View.GONE);
            imageNoViajes.setVisibility(View.VISIBLE);
            txtNoViajes.setVisibility(View.VISIBLE);
            btnAgendarViaje.setVisibility(View.VISIBLE);
        }else{
            rvViajes.setVisibility(View.VISIBLE);
            imageNoViajes.setVisibility(View.GONE);
            txtNoViajes.setVisibility(View.GONE);
            btnAgendarViaje.setVisibility(View.GONE);
        }
    }

    public void setListener(OnPageChangeListener listener){
        onPageChangeListener = listener;
    }
}
