package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.barajasoft.raites.R;
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

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private MapboxGeocoding mapboxGeocoding;
    private int RESULT_CODE = RESULT_CANCELED;
    private EditText txtBuscar;
    private Button btnBuscar;
    private Button btnConfirmar;
    private Spinner ciudades;
    private String ciudad, estado="B.C.", direccionEncontrada;
    private LatLng currentLocationCoordinates = null;
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
    private boolean locationSelected = false;
    private Marker markerActual = null;
    private String selectionType;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra("tipoSeleccion"))
            selectionType = getIntent().getStringExtra("tipoSeleccion");
        //Mapbox Access token
        setContentView(R.layout.map_activity);
        btnBuscar = findViewById(R.id.btnBuscar);
        txtBuscar = findViewById(R.id.txtBuscar);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        ciudades = findViewById(R.id.ciudadesSpinner);
        ciudades.setOnItemSelectedListener(this);
        String[] ciudadesList = new String[]{"Ensenada","Tijuana","Tecate","Rosarito","Mexicali"};
        ArrayAdapter<String> ciudadesData = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ciudadesList);
        ciudadesData.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ciudades.setAdapter(ciudadesData);
        btnBuscar.setOnClickListener(e->{
            if(!txtBuscar.getText().toString().isEmpty()){
                mapboxGeocoding = MapboxGeocoding.builder()
                        .accessToken(getString(R.string.mapbox_access_token))
                        .query(txtBuscar.getText().toString()+", "+ciudad+", "+estado)
                        .autocomplete(true)
                        .build();
                mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
                    @Override
                    public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                        if(response.body().features()!=null){
                            List<CarmenFeature> results = response.body().features();
                            if (results.size() > 0) {
                                // Log the first results Point.
                                Point firstResultPoint = results.get(0).center();
                                moverCamara(new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()));
                            }else{
                                Log.e("error","No se encontro nada");
                            }
                        }else{
                            Toast.makeText(getApplicationContext(),"No se encontro esa direccion",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
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

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // Customize map with markers, polylines, etc.
                MapActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setLatLngBoundsForCameraTarget(BC_BOUNDS);
                mapboxMap.setMaxZoomPreference(14);
                mapboxMap.addOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(@NonNull LatLng point) {
                        if(locationSelected){
                            mapboxMap.removeMarker(markerActual);
                        }
                        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                                .accessToken(getString(R.string.mapbox_access_token))
                                .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                                .build();
                        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
                            @Override
                            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                                if(response.body().features()!=null){
                                    if(response.body().features().size()>0){
                                        String direccion = response.body().features().get(0).placeName();
                                        markerActual = mapboxMap.addMarker(new MarkerOptions().position(point)
                                                .title("Lugar del "+ selectionType).snippet(direccion));
                                        locationSelected = true;
                                        currentLocationCoordinates = point;
                                        btnConfirmar.setVisibility(View.VISIBLE);
                                        direccionEncontrada = direccion;
                                    }else{
                                        Log.e("error","No se encontro nada x2");
                                        Snackbar.make(getCurrentFocus(),"No se encontro una direccion para este lugar, prueba con otra",Snackbar.LENGTH_SHORT).show();
                                        btnConfirmar.setVisibility(View.GONE);
                                        locationSelected = false;
                                    }
                                }else{
                                        Log.e("error","No se encontro nada");
                                    }
                            }

                            @Override
                            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                                throwable.printStackTrace();
                            }
                        });
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
            if(locationSelected){
                RESULT_CODE = RESULT_OK;
                finish();
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
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(mapboxGeocoding!=null)
            mapboxGeocoding.cancelCall();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void moverCamara(LatLng posicion){
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(posicion.getLatitude(), posicion.getLongitude())) // Sets the new camera position
                .zoom(18) // Sets the zoom
                .bearing(180) // Rotate the camera
                .tilt(30) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 4000);
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
            intent.putExtra("direccion",direccionEncontrada);
            intent.putExtra("tipoSeleccion",selectionType);
            intent.putExtra("latitud",currentLocationCoordinates.getLatitude());
            intent.putExtra("longitud",currentLocationCoordinates.getLongitude());
            setResult(RESULT_CODE, intent);
        }else{
            setResult(RESULT_CODE);
        }
        super.finish();
    }
}
