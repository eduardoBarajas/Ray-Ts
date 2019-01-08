package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.MapUtilities;
import com.google.android.gms.common.api.Response;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.DirectionsWaypoint;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;

public class VisualizeTravelActivity extends BaseActivity implements OnMapReadyCallback{

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
    private Point inicio, destino, currentPuntoParada;
    private String currentDireccion;
    private MapUtilities mapUtilities;
    private NavigationMapRoute navigationMapRoute;
    private TextView txtSalida, txtDestino, labelParada, txtParada;
    private Marker puntoDeParadaMarker;
    private List<String> direccionesParadas = new LinkedList<>();
    private List<String> nombresParadas = new LinkedList<>();
    private List<LatLng> posicionesParadas = new LinkedList<>();
    private String currentUserName, currentUserKey;
    private ResultListener listener;
    private Button btnConfirmar;
    private boolean isEditing = false;
    private ConstraintLayout constraintLayout;
    private int RESULT_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapUtilities = new MapUtilities(getApplicationContext());
        disableBottomMenu();
        disableDrawer();
        disableViewPager();
        setToolbar("","Trayecto Viaje Actual");
        View layout = LayoutInflater.from(this).inflate(R.layout.visualize_travel_activity,null);
        constraintLayout = layout.findViewById(R.id.constraintLayout);
        txtSalida = layout.findViewById(R.id.txtSalida);
        txtDestino = layout.findViewById(R.id.txtDestino);
        txtParada = layout.findViewById(R.id.txtParada);
        labelParada = layout.findViewById(R.id.labelParada);
        btnConfirmar = layout.findViewById(R.id.confirmarButton);
        mapView = (MapView) layout.findViewById(R.id.mapView);

        if(getIntent().hasExtra("direccionSalida")){
            txtSalida.setText(getIntent().getStringExtra("direccionSalida"));
            txtDestino.setText(getIntent().getStringExtra("direccionDestino"));
            inicio = Point.fromLngLat(getIntent().getDoubleExtra("longitudSalida",0), getIntent().getDoubleExtra("latitudSalida",0));
            destino = Point.fromLngLat(getIntent().getDoubleExtra("longitudDestino",0), getIntent().getDoubleExtra("latitudDestino",0));
        }
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        if(getIntent().hasExtra("visualizeMapParada")) {
            //aqui el text de parada esta visible por eso se usa ese constraint
            constraintSet.connect(mapView.getId(), ConstraintSet.TOP, txtParada.getId(), ConstraintSet.BOTTOM,8);
            for (int i = 0; i < getIntent().getStringArrayExtra("usersParadas").length; i++) {
                direccionesParadas.add(getIntent().getStringArrayExtra("direccionesPuntosParada")[i]);
                nombresParadas.add(getIntent().getStringArrayExtra("usersParadas")[i]);
                posicionesParadas.add(new LatLng(Double.parseDouble(getIntent().getStringArrayExtra("puntosParada")[i].split(":")[0]),
                        Double.parseDouble(getIntent().getStringArrayExtra("puntosParada")[i].split(":")[1])));
            }
            if(getIntent().hasExtra("currentUserName")){
                currentUserKey = getIntent().getStringExtra("currentUserKey");
                currentUserName = getIntent().getStringExtra("currentUserName");
                txtParada.setText(direccionesParadas.get(nombresParadas.indexOf(currentUserName)));
            }
        }else{
            labelParada.setVisibility(View.GONE);
            txtParada.setVisibility(View.GONE);
            //aqui no esta visible por eso se usa el otro constraint de destino
            constraintSet.connect(mapView.getId(), ConstraintSet.TOP, txtDestino.getId(), ConstraintSet.BOTTOM,8);
        }
        if(getIntent().hasExtra("editMapParada")){
            isEditing = true;
        }
        constraintSet.applyTo(constraintLayout);

        mapView.onCreate(savedInstanceState);
        mapView.setStyleUrl(Style.MAPBOX_STREETS);
        mapView.getMapAsync(this);
        addContent(layout);
        btnConfirmar.setOnClickListener(e->{
            RESULT_CODE = RESULT_OK;
            finish();
        });
        listener = new ResultListener() {
            @Override
            public void result(String tag, Object result) {
                if(tag.equals("DireccionSelected")){
                    currentDireccion = (String) result;
                    txtParada.setText(currentDireccion);
                    if(puntoDeParadaMarker!=null)
                        mapboxMap.removeMarker(puntoDeParadaMarker);
                    puntoDeParadaMarker = mapboxMap.addMarker(new MarkerOptions().position(new LatLng(currentPuntoParada.latitude(), currentPuntoParada.longitude()))
                            .title("Nueva Parada Seleccionada").snippet(currentDireccion));
                    List<Point> paradasPoints = new LinkedList<>();
                    for(LatLng pos : posicionesParadas){
                        paradasPoints.add(Point.fromLngLat(pos.getLongitude(), pos.getLatitude()));
                    }
                    paradasPoints.set(nombresParadas.indexOf(currentUserName), currentPuntoParada);
                    mapUtilities.getRutaConParada(navigationMapRoute, inicio, destino, paradasPoints);
                    btnConfirmar.setVisibility(View.VISIBLE);
                }
            }
        };
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        if(navigationMapRoute==null)
            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
        mapboxMap.setLatLngBoundsForCameraTarget(BC_BOUNDS);
        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(inicio.latitude(), inicio.longitude()))
                .title("Direccion Salida").snippet(txtSalida.getText().toString()));
        mapboxMap.addMarker(new MarkerOptions().position(new LatLng(destino.latitude(), destino.longitude()))
                .title("Direccion Destino").snippet(txtDestino.getText().toString()));
        for(int i = 0; i < posicionesParadas.size(); i++){
            if(nombresParadas.indexOf(currentUserName)==i){
                puntoDeParadaMarker =  mapboxMap.addMarker(new MarkerOptions().position(new LatLng(posicionesParadas.get(i).getLatitude(),
                        posicionesParadas.get(i).getLongitude())).title("Parada #"+i).snippet("Direccion de la parada seleccionada " +
                        "por el usuario \n\n"+nombresParadas.get(i)+"\n\n" +direccionesParadas.get(i)));
            }else{
                mapboxMap.addMarker(new MarkerOptions().position(new LatLng(posicionesParadas.get(i).getLatitude(),
                        posicionesParadas.get(i).getLongitude())).title("Parada #"+i).snippet("Direccion de la parada seleccionada " +
                        "por el usuario \n\n"+nombresParadas.get(i)+"\n\n" +direccionesParadas.get(i)));
            }
        }
        List<Point> paradasPoints = new LinkedList<>();
        for(LatLng pos : posicionesParadas){
            paradasPoints.add(Point.fromLngLat(pos.getLongitude(), pos.getLatitude()));
        }
        mapUtilities.getRutaConParada(navigationMapRoute, inicio, destino, paradasPoints);
        mapboxMap.setCameraPosition(mapUtilities.moverCamara(new LatLng(inicio.latitude(),inicio.longitude())));
        if(isEditing){
            mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng point) {
                    currentPuntoParada = Point.fromLngLat(point.getLongitude(), point.getLatitude());
                    mapUtilities.getDireccionName(point, listener);
                }
            });
        }
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
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void finish() {
        if(RESULT_CODE == RESULT_OK){
            Intent intent = new Intent();
            intent.putExtra("direccionParada", currentDireccion);
            intent.putExtra("latitudParada", currentPuntoParada.latitude());
            intent.putExtra("longitudParada", currentPuntoParada.longitude());
            setResult(RESULT_CODE, intent);
        }else{
            setResult(RESULT_CODE);
        }
        super.finish();
    }
}
