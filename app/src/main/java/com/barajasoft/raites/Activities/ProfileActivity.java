package com.barajasoft.raites.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.OptionChooserDialog;
import com.barajasoft.raites.Dialogs.SingleDataEditDialog;
import com.barajasoft.raites.Listeners.DialogResultListener;
import com.barajasoft.raites.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class ProfileActivity extends BaseActivity {
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private DialogResultListener dialogResultListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_image) // resource or drawable
                .showImageForEmptyUri(R.drawable.no_profile) // resource or drawable
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .build();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        initDrawer();
        setNavViewMenu("perfil");
        setToolbar("","Mi Perfil");
        View layout = LayoutInflater.from(this).inflate(R.layout.profile_activity,null);
        TextView name, age, sex, email, phone;
        ImageView editName, editAge, editSex, editEmail, editPhone, profile;
        editName = layout.findViewById(R.id.editName);
        editAge = layout.findViewById(R.id.editAge);
        editSex = layout.findViewById(R.id.editSex);
        editPhone = layout.findViewById(R.id.editTelefono);
        name = layout.findViewById(R.id.txtName);
        age = layout.findViewById(R.id.txtAge);
        sex = layout.findViewById(R.id.txtSexo);
        email = layout.findViewById(R.id.txtEmail);
        phone = layout.findViewById(R.id.txtTelefono);
        profile = layout.findViewById(R.id.profile);
        name.setText(pref.getString("nombre",null));
        age.setText(String.valueOf(pref.getInt("edad",-1)));
        sex.setText(pref.getString("sexo",null));
        email.setText(pref.getString("correo",null));
        phone.setText(pref.getString("telefono",null));
        imageLoader.displayImage(pref.getString("linkPerfil",null),profile,options);
        dialogResultListener = new DialogResultListener() {
            @Override
            public void result(String dlgTag, Object result) {
                if(dlgTag.equals("EditarEdadDlg")&&!((String)result).isEmpty()){
                    Toast.makeText(getApplicationContext(),"Nueva edad: "+(String)result,Toast.LENGTH_SHORT).show();
                }
                if(dlgTag.equals("EditarNombreDlg")&&!((String)result).isEmpty()){
                    Toast.makeText(getApplicationContext(),"Nuevo nombre: "+(String)result,Toast.LENGTH_SHORT).show();
                }
                if(dlgTag.equals("EditarTelefonoDlg")&&!((String)result).isEmpty()){
                    Toast.makeText(getApplicationContext(),"Nuevo telefono: "+(String)result,Toast.LENGTH_SHORT).show();
                }
                if(dlgTag.equals("EditarSexoDlg")){
                    if(((String)result).equals("Hombre")){
                        Toast.makeText(getApplicationContext(),"Elegiste hombre",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"Elegiste Mujer",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        editAge.setOnClickListener(e->{
            SingleDataEditDialog dlg = new SingleDataEditDialog(ProfileActivity.this,"EditarEdadDlg","Editar Edad",age.getText().toString(),dialogResultListener);
            dlg.show();
        });
        editName.setOnClickListener(e->{
            SingleDataEditDialog dlg = new SingleDataEditDialog(ProfileActivity.this,"EditarNombreDlg","Editar Nombre",name.getText().toString(),dialogResultListener);
            dlg.show();
        });
        editPhone.setOnClickListener(e->{
            SingleDataEditDialog dlg = new SingleDataEditDialog(ProfileActivity.this,"EditarTelefonoDlg","Editar Telefono",phone.getText().toString(),dialogResultListener);
            dlg.show();
        });
        editSex.setOnClickListener(e->{
            OptionChooserDialog dlg = new OptionChooserDialog(ProfileActivity.this,"EditarSexoDlg","Elige el sexo","Hombre","Mujer",dialogResultListener);
            dlg.show();
        });
        addContent(layout);
    }
}
