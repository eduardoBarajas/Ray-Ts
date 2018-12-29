package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.OptionChooserDialog;
import com.barajasoft.raites.Dialogs.SingleDataEditDialog;
import com.barajasoft.raites.Listeners.DialogResultListener;
import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.ImageAngleCorrector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends BaseActivity {
    private ImageLoader imageLoader;
    private StorageReference storageReference;
    private Uri imageDir;
    private boolean imageSelected = false;
    private DisplayImageOptions options;
    private DialogResultListener dialogResultListener;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referencia = database.getReference("Usuarios");
    private ImageView profile;
    private SharedPreferences pref;
    private TextView name, age, sex, email, phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageReference = FirebaseStorage.getInstance().getReference();
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
        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        initDrawer();
        setNavViewMenu("perfil");
        setToolbar("","Mi Perfil");
        View layout = LayoutInflater.from(this).inflate(R.layout.profile_activity,null);
        ImageView editName, editAge, editSex, editEmail, editPhone;
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
        FloatingActionButton btnChangeProfile = layout.findViewById(R.id.btnChangeProfile);
        btnChangeProfile.setOnClickListener(e->{
            Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
            intent.setType( "image/*" );
            startActivityForResult( intent, PICK_IMAGE);
        });
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
                    age.setText((String)result);
                    updateFieldInFirebase("edad",result,true);
                }
                if(dlgTag.equals("EditarNombreDlg")&&!((String)result).isEmpty()){
                    name.setText((String)result);
                    updateFieldInFirebase("nombre",result,false);
                }
                if(dlgTag.equals("EditarTelefonoDlg")&&!((String)result).isEmpty()){
                    phone.setText((String)result);
                    updateFieldInFirebase("telefono",result,false);
                }
                if(dlgTag.equals("EditarSexoDlg")){
                    sex.setText((String)result);
                    updateFieldInFirebase("sexo",result,false);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            imageDir = data.getData();
            profile.setImageBitmap(ImageAngleCorrector.getFixedBitmap(getContentResolver(),imageDir));
            imageSelected = true;
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir, archivo;
            myDir = new File(root + "/Raites/Resources/Temp");
            myDir.mkdirs();
            archivo = new File(myDir,"perfil.jpg");
            Uri link = Uri.EMPTY;
            try {
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(ImageAngleCorrector.getFixedBitmap(getContentResolver(),imageDir), 1024, 768, false);
                FileOutputStream out = new FileOutputStream(archivo);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                link = Uri.fromFile(archivo);
            } catch (Exception e) {
                    Log.e("ErrorImagen","No se pudo convertir la imagen");
            }
            StorageReference arch = storageReference.child(pref.getString("key", null)+"/"+"perfil.jpg");
            arch.putFile(link)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.child(pref.getString("key", null)+"/perfil.jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Map<String,Object> dato = new HashMap<>();
                                    dato.put("imagenPerfil", task.getResult().toString());
                                    referencia.child(pref.getString("key", null)).updateChildren(dato, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            Toast.makeText(getApplicationContext(),"Imagen de perfil actualizada",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("State","No Subido");
                            Toast.makeText(getApplicationContext(),"No se pudo actualizar la imagen de perfil",Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateFieldInFirebase(String field, Object data, boolean isNumber){
        Map<String,Object> dato = new HashMap<>();
        if(isNumber)
            dato.put(field,Integer.parseInt(data.toString()));
        else
            dato.put(field,data);
        referencia.child(pref.getString("key",null)).updateChildren(dato, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(getApplicationContext(),"Cambio realizado con exito",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void update() {
        super.update();
        updateUI();
    }

    private void updateUI() {
        name.setText(pref.getString("nombre",null));
        sex.setText(pref.getString("sexo",null));
        age.setText(String.valueOf(pref.getInt("edad",-1)));
        phone.setText(pref.getString("telefono", null));
        imageLoader.displayImage(pref.getString("linkPerfil",null), profile, options);
    }
}
