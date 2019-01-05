package com.barajasoft.raites.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;

import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;

public class DefinirHoraViajeFragment extends Fragment {
    private OnPageChangeListener listener;
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
        return inflater.inflate(R.layout.definir_hora_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton btnNext = view.findViewById(R.id.btnNext);
        FloatingActionButton btnBack = view.findViewById(R.id.btnBack);
        TimePicker time = view.findViewById(R.id.timePicker);
        btnNext.setOnClickListener(e->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String hora = String.valueOf(time.getHour())+":"+String.valueOf(time.getMinute());
                if(time.getHour()>=12)
                    hora += " PM";
                else
                    hora += " AM";
                editor.putString("HoraSeleccionada", hora);
                editor.commit();
            }
            listener.pageChanged(4);
        });
        btnBack.setOnClickListener(e->{
            listener.pageChanged(2);
        });
    }
}
