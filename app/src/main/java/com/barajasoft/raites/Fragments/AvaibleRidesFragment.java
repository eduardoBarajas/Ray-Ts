package com.barajasoft.raites.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.barajasoft.raites.R;

public class AvaibleRidesFragment extends BaseFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Toast.makeText(getContext(),"Estas seguro que no es una pieza de pollo?",Toast.LENGTH_SHORT).show();
        return inflater.inflate(R.layout.avaible_rides_fragment,container,false);
    }
}
