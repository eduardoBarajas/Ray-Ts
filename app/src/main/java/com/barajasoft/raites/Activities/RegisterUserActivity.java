package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.ImageAngleCorrector;

public class RegisterUserActivity extends BaseActivity {
    private Uri imageDir;
    private ImageView profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        disableDrawer();
        View layout = LayoutInflater.from(this).inflate(R.layout.register_user_activity,null);
        FloatingActionButton btnProfileImage = layout.findViewById(R.id.profileImageButton);
        profile = layout.findViewById(R.id.profile);
        btnProfileImage.setOnClickListener(e->{
            Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
            intent.setType( "image/*" );
            startActivityForResult( intent, PICK_IMAGE);
        });
        addContent(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            imageDir = data.getData();
            profile.setImageBitmap(ImageAngleCorrector.getFixedBitmap(getContentResolver(),imageDir));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
