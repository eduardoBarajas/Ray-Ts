package com.barajasoft.raites.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Activities.MapActivity;
import com.barajasoft.raites.Activities.PublicarViajeActivity;
import com.barajasoft.raites.Dialogs.OptionChooserDialog;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.MapUtilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends BaseFragment implements OnMapReadyCallback{
    private final LatLngBounds BC_BOUNDS = new LatLngBounds.Builder()
            .include(new LatLng(32.506870, -117.148386))
            .include(new LatLng(32.622409, -114.806687))
            .include(new LatLng(29.801709, -114.418654))
            .include(new LatLng(26.779281, -112.394149))
            .include(new LatLng(23.134196, -109.877537))
            .include(new LatLng(25.318742, -112.090141))
            .include(new LatLng(27.866482, -114.326333))
            .include(new LatLng(31.877251, -116.509088))
            .include(new LatLng(32.521699, -117.119577))
            .build();
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Marker currentMarker = null;
    private NavigationMapRoute navigationMapRoute;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private Point inicio, destino, currentParada;
    private Viaje viajeActual;
    private TextView labelTrayectoria;
    private Button btnConfirmar;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SharedPreferences pref;
    private MapUtilities mapUtilities;
    private String direccion;
    private ResultListener listener;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mapUtilities = new MapUtilities(getContext());
        listener = new ResultListener() {
            @Override
            public void result(String dlgTag, Object result) {
                if(dlgTag.equals("DireccionSelected")){
                    direccion = (String) result;
                    if(currentMarker!=null)
                        mapboxMap.removeMarker(currentMarker);
                    currentMarker = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(currentParada.latitude(), currentParada.longitude()))
                            .title("Parada Seleccionada").snippet(direccion.toString()));
                    mapUtilities.getRutaConParada(navigationMapRoute, inicio, destino, currentParada);
                }
            }
        };
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnConfirmar = view.findViewById(R.id.btnConfirmar);
        labelTrayectoria = view.findViewById(R.id.labelTrayectoriaActual);
        mapView = (MapView) view.findViewById(R.id.mapView);
        if (getActivity().getIntent().hasExtra("KeyConductor")) {
            viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.getValue(Viaje.class).getKeyConductor().equals(getActivity().getIntent().getStringExtra("KeyConductor"))) {
                            viajeActual = data.getValue(Viaje.class);
                        }
                        inicio = Point.fromLngLat(viajeActual.getPuntosDeViaje().get(0).getLongitude(), viajeActual.getPuntosDeViaje().get(0).getLatitude());
                        destino = Point.fromLngLat(viajeActual.getPuntosDeViaje().get(1).getLongitude(), viajeActual.getPuntosDeViaje().get(1).getLatitude());
                        mapView.onCreate(savedInstanceState);
                        mapView.setStyleUrl(Style.MAPBOX_STREETS);
                        mapView.getMapAsync(MapFragment.this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
//        listener = new DialogResultListener() {
//            @Override
//            public void result(String dlgTag, Object result) {
//                if (dlgTag.equals("ParadaSelection")) {
//                    if (result.equals("Si")) {
//
//                    }
//                }
//            }
//        };
        btnConfirmar.setOnClickListener(e->{
            SolicitudViaje solicitudViaje = new SolicitudViaje();
            solicitudViaje.setAceptada(false);
            solicitudViaje.setEspaciosSolicitados(pref.getInt("AsientosSolicitados", 0));
            solicitudViaje.setFechaSolicitud(simpleDateFormat.format(Calendar.getInstance().getTime()));
            solicitudViaje.setKeyPasajero(pref.getString("key", null));
            solicitudViaje.setPuntoDeParada(new LatLng(currentParada.latitude(), currentParada.longitude()));
            solicitudViaje.setKeyViaje(getActivity().getIntent().getStringExtra("KeyViaje"));
            solicitudViaje.setDireccionDeParada(direccion.toString());
            solicitudesReference.child(solicitudViaje.getKey()).setValue(solicitudViaje, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    Toast.makeText(getContext(), "Se envio la solicitud", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            });
        });
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        if(navigationMapRoute==null)
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
        mapboxMap.setLatLngBoundsForCameraTarget(BC_BOUNDS);
        mapboxMap.setMaxZoomPreference(14);
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                mapUtilities.moverCamara(new LatLng(inicio.latitude(), inicio.longitude()))), 4000);
        mapUtilities.getRuta(navigationMapRoute, inicio, destino);
        mapboxMap.addMarker(new MarkerOptions().position(viajeActual.getPuntosDeViaje().get(0))
                .title("Direccion Salida").snippet(viajeActual.getDireccionSalida()));
        mapboxMap.addMarker(new MarkerOptions().position(viajeActual.getPuntosDeViaje().get(1))
                .title("Direccion Destino").snippet(viajeActual.getDireccionDestino()));
        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                currentParada = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                btnConfirmar.setVisibility(View.VISIBLE);
                labelTrayectoria.setText("Nueva Ruta");
//                OptionChooserDialog dialog = new OptionChooserDialog(getActivity(), "ParadaSelection",
//                        "Seleccion de parada", "Si", "No", listener);
//                dialog.show();
                mapUtilities.getDireccionName(point, listener);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
