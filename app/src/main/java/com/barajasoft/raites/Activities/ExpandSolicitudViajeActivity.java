package com.barajasoft.raites.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ExpandSolicitudViajeActivity extends AppCompatActivity {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private TextView txtSalida, txtDestino, txtEspacios, txtPuntoDeParada, txtEstado, labelMap;
    private TextInputEditText editEspacios;
    private Button btnRegresar, btnConfirmar, btnMapa;
    private SolicitudViaje solicitudActual;
    private Viaje viajeActual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expanded_solicitud_viaje_activity);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnMapa = findViewById(R.id.btnMap);
        txtEspacios = findViewById(R.id.txtEspacios);
        txtSalida = findViewById(R.id.txtDireccionSalida);
        txtDestino = findViewById(R.id.txtDireccionDestino);
        txtPuntoDeParada = findViewById(R.id.txtPuntoDeParada);
        txtEstado = findViewById(R.id.txtEstado);
        labelMap = findViewById(R.id.labelMap);
        editEspacios = findViewById(R.id.editEspacios);
        if(getIntent().hasExtra("solicitud_key")){
            solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot data : dataSnapshot.getChildren()){
                        if(data.getValue(SolicitudViaje.class).getKey().equals(getIntent().getStringExtra("solicitud_key"))){
                            solicitudActual = data.getValue(SolicitudViaje.class);
                        }
                    }
                    viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot data : dataSnapshot.getChildren()){
                                if(data.getValue(Viaje.class).getKey().equals(solicitudActual.getKeyViaje())){
                                    viajeActual = data.getValue(Viaje.class);
                                }
                            }
                            txtDestino.setText(viajeActual.getDireccionDestino());
                            txtSalida.setText(viajeActual.getDireccionSalida());
                            txtPuntoDeParada.setText(solicitudActual.getDireccionDeParada());
                            if(solicitudActual.isAceptada()){
                                txtEstado.setText("Aceptada");
                            }else{
                                txtEstado.setText("Pendiente");
                            }
                            btnRegresar.setOnClickListener(e->{
                                finish();
                            });
                            if(getIntent().hasExtra("Edicion")){
                                txtEspacios.setVisibility(View.GONE);
                                editEspacios.setText(String.valueOf(solicitudActual.getEspaciosSolicitados()));
                                btnConfirmar.setOnClickListener(e->{
                                    Toast.makeText(getApplicationContext(), "Confirmar", Toast.LENGTH_SHORT).show();
                                });
                                btnMapa.setOnClickListener(e->{
                                    Toast.makeText(getApplicationContext(), "Mapa", Toast.LENGTH_SHORT).show();
                                });
                            }else{
                                txtEspacios.setText(String.valueOf(solicitudActual.getEspaciosSolicitados()));
                                editEspacios.setVisibility(View.GONE);
                                labelMap.setVisibility(View.GONE);
                                btnConfirmar.setVisibility(View.GONE);
                                btnMapa.setVisibility(View.GONE);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }
}
