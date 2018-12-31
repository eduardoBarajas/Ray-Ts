package com.barajasoft.raites.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.barajasoft.raites.Adapters.SolicitudesAdapter;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class SolicitudesViajeActivity extends BaseActivity {
    private RecyclerView rvSolicitudes;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private SolicitudesAdapter adapter;
    private String currentKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra("KeyViaje")){
            currentKey = getIntent().getStringExtra("KeyViaje");
        }
        disableBottomMenu();
        disableDrawer();
        disableViewPager();
        setToolbar("", "Solicitudes De Viajes");
        View layout = LayoutInflater.from(this).inflate(R.layout.solicitudes_viajes_activity, null);
        adapter = new SolicitudesAdapter(SolicitudesViajeActivity.this);
        rvSolicitudes = layout.findViewById(R.id.rvSolicitudes);
        rvSolicitudes.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL, false);
        rvSolicitudes.setLayoutManager(manager);
        rvSolicitudes.setItemAnimator(new DefaultItemAnimator());
        rvSolicitudes.setAdapter(adapter);
        solicitudesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(currentKey.equals(dataSnapshot.getValue(SolicitudViaje.class).getKeyViaje())){
                    adapter.addSolicitud(dataSnapshot.getValue(SolicitudViaje.class));
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(currentKey.equals(dataSnapshot.getValue(SolicitudViaje.class).getKeyViaje())){
                    adapter.removeSolicitud(dataSnapshot.getKey());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        addContent(layout);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }
}
