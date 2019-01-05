package com.barajasoft.raites.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.DatePicker;

import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class DefinirFechaViajeFragment extends Fragment {
    private OnPageChangeListener listener;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy-kk:mm a");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = pref.edit();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT-8:00"));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnPageChangeListener)context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.definir_fecha_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FloatingActionButton btnNext = view.findViewById(R.id.btnNext);
        FloatingActionButton btnBack = view.findViewById(R.id.btnBack);
        DatePicker date = view.findViewById(R.id.datePicker);
        btnNext.setOnClickListener(e->{
            editor.putString("FechaSeleccionada", String.valueOf(date.getDayOfMonth())+"/"+String.valueOf(date.getMonth() + 1)+"/"+String.valueOf(date.getYear()));
            editor.putString("FechaPublicada", simpleDateFormat.format(Calendar.getInstance().getTime()));
            editor.commit();
            listener.pageChanged(3);
        });
        btnBack.setOnClickListener(e->{
            listener.pageChanged(1);
        });
    }
}
