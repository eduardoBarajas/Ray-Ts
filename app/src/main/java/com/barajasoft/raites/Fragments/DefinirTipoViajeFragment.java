package com.barajasoft.raites.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;

import org.jetbrains.annotations.TestOnly;

import java.util.concurrent.atomic.AtomicBoolean;

public class DefinirTipoViajeFragment extends Fragment {
    private OnPageChangeListener listener;
    private String typeSelected = "";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = pref.edit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnPageChangeListener)context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.definir_tipo_viaje_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f );
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f );
        FloatingActionButton btnNext = view.findViewById(R.id.btnNext);
        FloatingActionButton btnBack = view.findViewById(R.id.btnBack);
        ImageButton btnPasajero = view.findViewById(R.id.btnPasajero);
        ImageButton btnConductor = view.findViewById(R.id.btnConductor);
        TextView lblCupos = view.findViewById(R.id.cuposTextView);
        TextInputEditText txtEspacios = view.findViewById(R.id.espaciosDisponiblesText);
        fadeOut.setDuration(400);
        fadeIn.setDuration(400);
        fadeIn.setStartOffset(400);
        Animation.AnimationListener animationListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(typeSelected.equals("Pasajero"))
                    lblCupos.setText("Cuantos espacios ocuparas para el viaje?");
                if(typeSelected.equals("Conductor"))
                    lblCupos.setText("Cuantos espacios tienes libres para el viaje?");
            }
            @Override
            public void onAnimationEnd(Animation animation) { }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        };
        fadeIn.setAnimationListener(animationListener);
        btnPasajero.setOnClickListener(e->{
            if(pref.getBoolean("validadoPasajero", false)){
                btnConductor.setSelected(false);
                btnPasajero.setSelected(true);
                lblCupos.startAnimation(fadeOut);
                lblCupos.startAnimation(fadeIn);
                typeSelected = "Pasajero";
            }else{
                Snackbar.make(getView(), "No haz llenado tus datos de perfil, por favor llenar todos los campos primero", Snackbar.LENGTH_LONG).show();
            }
        });
        btnConductor.setOnClickListener(e->{
            if(pref.getBoolean("validadoConductor", false)) {
                if (pref.getBoolean("validadoPasajero", false)) {
                    btnPasajero.setSelected(false);
                    btnConductor.setSelected(true);
                    lblCupos.startAnimation(fadeOut);
                    lblCupos.startAnimation(fadeIn);
                    typeSelected = "Conductor";
                } else {
                    Snackbar.make(getView(), "No haz llenado tus datos de perfil, por favor llenar todos los campos primero", Snackbar.LENGTH_LONG).show();
                }
            }else{
                Snackbar.make(getView(), "No haz agregado un vehiculo, por favor agrega uno primero primero", Snackbar.LENGTH_LONG).show();
            }
        });
        btnNext.setOnClickListener(e->{
            if(!typeSelected.isEmpty()){
                if(typeSelected.equals("Conductor")&&(pref.getInt("espaciosDisponibles", 0) - 1) < Integer.parseInt(txtEspacios.getText().toString())){
                    Snackbar.make(getView(), "Tu vehiculo no tiene tanto espacio, vuelve a intentarlo con otro valor", Snackbar.LENGTH_LONG).show();
                }else{
                    editor.putString("typeSelected", typeSelected);
                    editor.putInt("roomSelected", Integer.parseInt(txtEspacios.getText().toString()));
                    editor.commit();
                    listener.pageChanged(2);
                }
            }else{
                Snackbar.make(getView(), "Primero debes seleccionar algun rol antes de continuar.", Snackbar.LENGTH_LONG).show();
            }
        });
        btnBack.setOnClickListener(e->{
            listener.pageChanged(0);
        });
    }
}
