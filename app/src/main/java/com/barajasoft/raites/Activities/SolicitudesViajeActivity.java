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
import android.widget.Toast;

import com.barajasoft.raites.Adapters.SolicitudesAdapter;
import com.barajasoft.raites.Dialogs.YesOrNoChooserDialog;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class SolicitudesViajeActivity extends BaseActivity {
    private RecyclerView rvSolicitudes;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private DatabaseReference usuariosReference = database.getReference("Usuarios");
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private SolicitudesAdapter adapter;
    private String currentKey;
    private Viaje currentViaje;
    private ResultListener listener;
    private SolicitudViaje request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //se recupera la llave del viaje
        if(getIntent().hasExtra("KeyViaje")){
            currentKey = getIntent().getStringExtra("KeyViaje");
        }
        disableBottomMenu();
        disableDrawer();
        disableViewPager();
        setToolbar("", "Solicitudes De Viajes");
        View layout = LayoutInflater.from(this).inflate(R.layout.solicitudes_viajes_activity, null);
        listener = new ResultListener() {
            @Override
            public void result(String tag, Object result) {
                if(tag.equals("Accepted")&&((String)result).equals("Accept")){
                    Map<String, Object> dato = new HashMap<>();
                    dato.put("aceptada", true);
                    solicitudesReference.child(request.getKey()).updateChildren(dato, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            Map<String, Object> datos = new HashMap<>();
                            datos.put("espaciosDisponibles", currentViaje.getEspaciosDisponibles() - request.getEspaciosSolicitados());
                            currentViaje.getPuntosDeParada().add(request.getPuntoDeParada());
                            datos.put("puntosDeParada", currentViaje.getPuntosDeParada());
                            currentViaje.getKeysPasajeros().add(request.getKeyPasajero());
                            datos.put("keysPasajeros", currentViaje.getKeysPasajeros());
                            viajesReference.child(request.getKeyViaje()).updateChildren(datos, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    Toast.makeText(getApplicationContext(), "Se agrego correctamente al viaje", Toast.LENGTH_LONG).show();
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }
                if(tag.equals("CancelRequest")){
                    request = (SolicitudViaje) result;
                    YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(SolicitudesViajeActivity.this, "Cancelled", "Cancelar solicitud?",
                            "Estas seguro que quieres cancelar esta solicitud?", listener);
                    dlg.show();
                }
                if(tag.equals("AcceptRequest")){
                    request = (SolicitudViaje) result;
                    YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(SolicitudesViajeActivity.this, "Accepted", "Aceptar solicitud?",
                            "Estas seguro que quieres aceptar esta solicitud?", listener);
                    dlg.show();
                }
                if(tag.equals("Cancelled")&&((String)result).equals("Accept")){
                    Map<String, Object> dato = new HashMap<>();
                    dato.put("aceptada", false);
                    solicitudesReference.child(request.getKey()).updateChildren(dato, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            Map<String, Object> datos = new HashMap<>();
                            datos.put("espaciosDisponibles", currentViaje.getEspaciosDisponibles() - request.getEspaciosSolicitados());
                            currentViaje.getPuntosDeParada().remove(request.getPuntoDeParada());
                            datos.put("puntosDeParada", currentViaje.getPuntosDeParada());
                            currentViaje.getKeysPasajeros().remove(request.getKeyPasajero());
                            datos.put("keysPasajeros", currentViaje.getKeysPasajeros());
                            viajesReference.child(request.getKeyViaje()).updateChildren(datos, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    Toast.makeText(getApplicationContext(), "Se cancelo correctamente el viaje", Toast.LENGTH_LONG).show();
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }
            }
        };
        adapter = new SolicitudesAdapter(SolicitudesViajeActivity.this, listener);
        rvSolicitudes = layout.findViewById(R.id.rvSolicitudes);
        rvSolicitudes.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL, false);
        rvSolicitudes.setLayoutManager(manager);
        rvSolicitudes.setItemAnimator(new DefaultItemAnimator());
        rvSolicitudes.setAdapter(adapter);
        solicitudesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                SolicitudViaje solicitudActual = dataSnapshot.getValue(SolicitudViaje.class);
                if(currentKey.equals(solicitudActual.getKeyViaje())){
                    //se encontro una solicitud que pertenece al viaje actual
                    //se recupera el viaje
                    //TODO hacer que solo se recupere el viaje una sola vez//

                    viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot viajes : dataSnapshot.getChildren()){
                                Viaje viajeActual = viajes.getValue(Viaje.class);
                                if(solicitudActual.getKeyViaje().equals(viajeActual.getKey())){
                                    currentViaje = viajeActual;
                                    //si entra aqui es que se encontro el viaje para la solicitud
                                    usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot u : dataSnapshot.getChildren()){
                                                User currentUser = u.getValue(User.class);
                                                if(currentUser.getKey().equals(solicitudActual.getKeyPasajero())){
                                                    //si entra aqui es por que se encontro al usuario que hizo la solcitud
                                                    //se agregan las 3 entidades al adaptador
                                                    adapter.addSolicitud(viajeActual, currentUser, solicitudActual);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.updateSolicitud(dataSnapshot.getValue(SolicitudViaje.class));
                adapter.notifyDataSetChanged();
            }
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
