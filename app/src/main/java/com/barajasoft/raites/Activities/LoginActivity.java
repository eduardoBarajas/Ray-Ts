package com.barajasoft.raites.Activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.barajasoft.raites.R;

public class LoginActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableBottomMenu();
        disableDrawer();
        disableViewPager();
        View layout = LayoutInflater.from(this).inflate(R.layout.login_activity,null);
        Button button = layout.findViewById(R.id.button);
        button.setOnClickListener(e->{
            Toast.makeText(getApplicationContext(),"Estas seguro que no es una pieza de pollo?",Toast.LENGTH_SHORT).show();
        });
        addContent(layout);
    }
}
