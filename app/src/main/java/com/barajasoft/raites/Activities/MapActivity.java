package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.OptionChooserDialog;
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
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ciudadesReference = database.getReference("Ciudades");
    private int RESULT_CODE = RESULT_CANCELED;
    private EditText txtBuscar;
    private Button btnBuscar, btnConfirmar;
    private Spinner ciudades;
    private String direccion;
    private String ciudad, estado="B.C.", direccionInicio, direccionDestino;
    private LatLng posicionInicio = null, posicionDestino = null, posicionActual = null;
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
    private Marker markerInicio = null, markerDestino = null;
    private NavigationMapRoute navigationMapRoute;
    private ArrayAdapter<String> ciudadesData;
    private MapUtilities mapUtilities;
    private ResultListener listener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Mapbox Access token
        setContentView(R.layout.map_activity);
        mapUtilities = new MapUtilities(getApplicationContext());
        btnBuscar = findViewById(R.id.btnBuscar);
        txtBuscar = findViewById(R.id.txtBuscar);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        ciudades = findViewById(R.id.ciudadesSpinner);
        ciudades.setOnItemSelectedListener(this);
        ciudadesData = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new LinkedList<>());
        ciudadesData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ciudadesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    ciudadesData.add(data.getValue(String.class));
                }
                ciudadesData.notifyDataSetChanged();
                ciudades.setAdapter(ciudadesData);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        btnBuscar.setOnClickListener(e->{
            //para esconder el teclado virtual se usan estas dos lineas siguientes
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), 0);
            if(!txtBuscar.getText().toString().isEmpty()){
                mapUtilities.getDireccionPosition(txtBuscar.getText().toString(), ciudad, estado ,mapboxMap);
            }else{
                Toast.makeText(getApplicationContext(),"Debes ingresar una direccion para buscar",Toast.LENGTH_SHORT).show();
            }
        });
        /*
            btnMiPosicion.setOnClickListener(e -> {
            if (mapLoaded && locationEngine != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastLocation = locationEngine.getLastLocation();
                if(lastLocation != null){
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude())) // Sets the new camera position
                            .zoom(18) // Sets the zoom
                            .bearing(180) // Rotate the camera
                            .tilt(30) // Set the camera tilt
                            .build(); // Creates a CameraPosition from the builder

                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), 1000);
                }else{
                    Toast.makeText(getApplicationContext(), "No se pudo obtener tu posicion actual.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(), "No se pudo obtener tu posicion actual.", Toast.LENGTH_SHORT).show();
            }
        });


         */

        listener = new ResultListener() {
            @Override
            public void result(String dlgTag, Object result) {
                if(dlgTag.equals("DireccionSelected")){
                    direccion = (String) result;
                }
                if (dlgTag.equals("PointChooser")) {
                    if ((result).equals("Salida")) {
                        if(markerInicio != null)
                            mapboxMap.removeMarker(markerInicio);
                        posicionInicio = posicionActual;
                        direccionInicio = direccion.toString();
                        markerInicio = mapboxMap.addMarker(new MarkerOptions().position(posicionInicio)
                                .title("Direccion Salida").snippet(direccion.toString()));
                        if(posicionDestino != null){
                            // es por que ya se seleccionaron los dos
                            btnConfirmar.setVisibility(View.VISIBLE);
                            mapUtilities.getRuta(navigationMapRoute, Point.fromLngLat(posicionInicio.getLongitude(),posicionInicio.getLatitude()), Point.fromLngLat(posicionDestino.getLongitude(),posicionDestino.getLatitude()));
                        }
                    }
                    if ((result).equals("Destino")) {
                        if(markerDestino != null)
                            mapboxMap.removeMarker(markerDestino);
                        posicionDestino = posicionActual;
                        direccionDestino = direccion.toString();
                        markerDestino = mapboxMap.addMarker(new MarkerOptions().position(posicionDestino)
                                .title("Direccion Destino").snippet(direccion.toString()));
                        if(posicionInicio != null){
                            // es por que ya se seleccionaron los dos
                            btnConfirmar.setVisibility(View.VISIBLE);
                            mapUtilities.getRuta(navigationMapRoute, Point.fromLngLat(posicionInicio.getLongitude(),posicionInicio.getLatitude()), Point.fromLngLat(posicionDestino.getLongitude(),posicionDestino.getLatitude()));
                        }
                    }
                }
            }
        };
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // Customize map with markers, polylines, etc.
                MapActivity.this.mapboxMap = mapboxMap;
                navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                mapboxMap.setLatLngBoundsForCameraTarget(BC_BOUNDS);
                mapboxMap.setMaxZoomPreference(14);
                mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng point) {
                        mapUtilities.getDireccionName(point, listener);
                        posicionActual = point;
                        OptionChooserDialog dlg = new OptionChooserDialog(MapActivity.this, "PointChooser", "Selecciona Punto", "Salida", "Destino", listener);
                        dlg.setCanceledOnTouchOutside(false);
                        dlg.show();
                    }
                });
                /*
                    LocationComponent locationComponent = mapboxMap.getLocationComponent();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    //El componente de location es el que permite usar la localizacion del usuario en el mapa.
                    locationComponent.activateLocationComponent(this);
                    locationComponent.setLocationComponentEnabled(true);
                    locationComponent.setRenderMode(RenderMode.COMPASS);
                    locationComponent.setCameraMode(CameraMode.NONE);
                    LocationEngineProvider locationEngineProvider = new LocationEngineProvider(this);

                    //El location engine es el que nos permite conocer las posiciones del usuario.
                    locationEngine = locationEngineProvider.obtainBestLocationEngineAvailable();
                    locationEngine.setPriority(LocationEnginePriority.BALANCED_POWER_ACCURACY);
                    locationEngine.activate();
                    //se agrega el locationEngine en el componente de location.
                    locationComponent.activateLocationComponent(this,locationEngine);
                */

                /*
                    for(LatLng centro : loader.getCentro_edificios()){
                        centros.add(centro);
                        markers.add(mapboxMap.addMarker(new MarkerOptions().position(centro).icon(getIcon(loader.getIconos().get(markers.size()))).title(reversePolygonsKeys.get(markers.size()))));
                    }
                    mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        LocationExpandedDlg dlg = new LocationExpandedDlg(MapActivity.this,marker.getTitle(),polygonsKeys.get(marker.getTitle()));
                        dlg.show();
                        return true;
                    }
                });
                //se hace zoom al DIA por default
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(31.865665,-116.666274)) // Sets the new camera position
                        .zoom(22) // Sets the zoom
                        .bearing(180) // Rotate the camera
                        .tilt(30) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 4000);

                 */
            }
        });
        btnConfirmar.setOnClickListener(e->{
                RESULT_CODE = RESULT_OK;
                finish();
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ciudad = adapterView.getItemAtPosition(i).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void finish() {
        if(RESULT_CODE == RESULT_OK){
            Intent intent = new Intent();
            intent.putExtra("direccionInicio", direccionInicio);
            intent.putExtra("direccionDestino", direccionDestino);
            intent.putExtra("latitudInicio", posicionInicio.getLatitude());
            intent.putExtra("longitudInicio", posicionInicio.getLongitude());
            intent.putExtra("latitudDestino", posicionDestino.getLatitude());
            intent.putExtra("longitudDestino", posicionDestino.getLongitude());
            setResult(RESULT_CODE, intent);
        }else{
            setResult(RESULT_CODE);
        }
        super.finish();
    }
}
