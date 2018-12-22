package com.barajasoft.raites.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.barajasoft.raites.Listeners.OnPageChangeListener;
import com.barajasoft.raites.R;

public class ViajesActivosFragment extends BaseFragment {
    private OnPageChangeListener onPageChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Toast.makeText(getContext(),"SOS",Toast.LENGTH_SHORT).show();
        View fragment = inflater.inflate(R.layout.viajes_activos_fragment,container,false);
        LinearLayout viajesNotFoundConductor = fragment.findViewById(R.id.viajesNotFoundConductor);
        LinearLayout viajesFoundConductor = fragment.findViewById(R.id.viajesFoundConductor);
        LinearLayout viajesNotFoundPasajero = fragment.findViewById(R.id.viajesNotFoundPasajero);
        LinearLayout viajesFoundPasajero = fragment.findViewById(R.id.viajesFoundPasajero);
        LinearLayout viajesNotFound = fragment.findViewById(R.id.viajesNotFound);
        Button btnAgendarViaje3 = fragment.findViewById(R.id.btnAgendarViaje3);
        viajesNotFound.setVisibility(View.VISIBLE);
        btnAgendarViaje3.setOnClickListener(e->{
            onPageChangeListener.pageChanged(1);
        });
        return fragment;
    }

    public void setListener(OnPageChangeListener listener){
        onPageChangeListener = listener;
    }
}
