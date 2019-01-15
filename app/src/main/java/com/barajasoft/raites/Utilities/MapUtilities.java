package com.barajasoft.raites.Utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.barajasoft.raites.Listeners.ResultListener;
import com.barajasoft.raites.R;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapUtilities {
    private Context context;
    private String TAG = "MapUtilities";

    public MapUtilities(Context context){
        this.context = context;
    }

    public void getDireccionPosition(String direccion, String ciudad, String estado, MapboxMap map){
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.mapbox_access_token))
                .query(direccion+", "+ciudad+", "+estado)
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
                        //Mover la camara
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(moverCamara(
                                new LatLng(firstResultPoint.latitude(),firstResultPoint.longitude()))), 4000);
                    }else{
                        Log.e("error","No se encontro nada");
                    }
                }else{
                    Toast.makeText(context,"No se encontro esa direccion",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void getDireccionName(LatLng point, ResultListener listener){
        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.mapbox_access_token))
                .query(Point.fromLngLat(point.getLongitude(), point.getLatitude()))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();

        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.body().features() != null) {
                    if (response.body().features().size() > 0) {
                        listener.result("DireccionSelected", response.body().features().get(0).placeName());
                    } else {
                        listener.result("DireccionSelected", "No se pudo encontrar una direccion para este sitio");
                    }
                } else {
                    Log.e("error", "No se encontro nada");
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public CameraPosition moverCamara(LatLng posicion){
        return new CameraPosition.Builder()
                .target(new LatLng(posicion.getLatitude(), posicion.getLongitude())) // Sets the new camera position
                .zoom(18) // Sets the zoom
                .bearing(180) // Rotate the camera
                .tilt(30) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder
    }

    public void getRuta(NavigationMapRoute navMap, Point origen, Point destino) {
        NavigationRoute.builder(context)
                .accessToken(context.getString(R.string.mapbox_access_token))
                .origin(origen)
                .destination(destino)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        if(navMap!=null)
                            navMap.removeRoute();
                        navMap.addRoute(response.body().routes().get(0));
                    }
                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }


    public void getRutaConParada(NavigationMapRoute navMap, Point inicio, Point destino, List<Point> parada) {
        NavigationRoute.Builder builder = NavigationRoute.builder(context)
                .accessToken(context.getString(R.string.mapbox_access_token))
                .origin(inicio)
                .destination(destino)
                .profile("driving");
        for(Point p : parada){
            builder.addWaypoint(p);
        }
        builder.build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response
                if (response.body() == null) {
                    return;
                } else if (response.body().routes().size() < 1) {
                    return;
                }
                // Draw the route on the map
                if (navMap != null)
                    navMap.removeRoute();
                navMap.addRoute(response.body().routes().get(0));
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e("ERROR MAP FRAGMENT", "Error: " + throwable.getMessage());
            }
        });
    }
}
