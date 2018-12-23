package com.barajasoft.raites.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.barajasoft.raites.R;

public class ProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        initDrawer();
        setNavViewMenu("perfil");
        setToolbar("","Mi Perfil");
        View layout = LayoutInflater.from(this).inflate(R.layout.profile_activity,null);
        TextView name, age, sex, email, phone;
        name = layout.findViewById(R.id.txtName);
        age = layout.findViewById(R.id.txtAge);
        sex = layout.findViewById(R.id.txtSexo);
        email = layout.findViewById(R.id.txtEmail);
        phone = layout.findViewById(R.id.txtTelefono);
        name.setText(pref.getString("nombre",null));
        age.setText(String.valueOf(pref.getInt("edad",-1)));
        sex.setText(pref.getString("sexo",null));
        email.setText(pref.getString("correo",null));
        phone.setText(pref.getString("telefono",null));
        addContent(layout);
    }
}
