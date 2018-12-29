package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LauncherActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ciudadesReference = database.getReference("Ciudades");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = pref.edit();
        setContentView(R.layout.launcher_activity);
        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        Runnable timerCode = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        };
        if(!pref.getBoolean("firstSetup", false)) {
            firstSetup();
        }
        Handler timerHandler = new Handler();
        timerHandler.postDelayed(timerCode,2000);
    }

    private void firstSetup() {
        //agregar las ciudades primarias
        List<String> cities = new LinkedList<>();
        Collections.addAll(cities, "Ensenada","Tijuana","Tecate","Playas De Rosarito","Mexicali");
        List<String> obtainedCities = new LinkedList<>();
        ciudadesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    obtainedCities.add(data.getValue(String.class));
                }
                if(obtainedCities.size() < cities.size()){
                    ciudadesReference.setValue(cities, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            editor.putBoolean("firstSetup", true);
                        }
                    });
                }else{
                    editor.putBoolean("firstSetup", true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
