package com.barajasoft.raites.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Activities.BaseActivity;
import com.barajasoft.raites.Activities.MapActivity;
import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;

import static android.app.Activity.RESULT_OK;
import static com.barajasoft.raites.Activities.BaseActivity.PICK_DIRECCION;

public class DefinirTrayectoFragment extends Fragment {

    private OnPageChangeListener listener;
    private TextView txtSalida, txtDestino;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnPageChangeListener)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = pref.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.definir_trayecto_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton btnNext = view.findViewById(R.id.btnNext);
        Button btnCamino = view.findViewById(R.id.btnCamino);
        txtSalida  = view.findViewById(R.id.txtSalida);
        txtDestino = view.findViewById(R.id.txtDestino);
        btnNext.setOnClickListener(e->{
            if(pref.getBoolean("pageOneCompleted", false))
                listener.pageChanged(1);
            else
                Snackbar.make(getView(), "Tienes que seleccionar una direccion antes de continuar", Toast.LENGTH_SHORT).show();
        });
        btnCamino.setOnClickListener(e->{
            Intent intent = new Intent(getContext(), MapActivity.class);
            startActivityForResult(intent, PICK_DIRECCION);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_DIRECCION ){
            if(resultCode == RESULT_OK) {
                txtSalida.setText(data.getStringExtra("direccionInicio"));
                txtDestino.setText(data.getStringExtra("direccionDestino"));
                editor.putString("direccionInicio", data.getStringExtra("direccionInicio"));
                editor.putString("direccionDestino", data.getStringExtra("direccionDestino"));
                editor.putString("latitudInicio", String.valueOf(data.getDoubleExtra("latitudInicio",0)));
                editor.putString("latitudDestino", String.valueOf(data.getDoubleExtra("latitudDestino",0)));
                editor.putString("longitudInicio", String.valueOf(data.getDoubleExtra("longitudInicio",0)));
                editor.putString("longitudDestino", String.valueOf(data.getDoubleExtra("longitudDestino",0)));
                editor.putBoolean("pageOneCompleted", true);
                editor.commit();
            }
        } else {
            Snackbar.make(getView(), "Se cancelo la seleccion de direccion", Toast.LENGTH_SHORT).show();
        }
    }
}
