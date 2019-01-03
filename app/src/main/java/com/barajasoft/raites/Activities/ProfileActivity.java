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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.OptionChooserDialog;
import com.barajasoft.raites.Dialogs.SingleDataEditDialog;
import com.barajasoft.raites.Dialogs.YesOrNoChooserDialog;
import com.barajasoft.raites.Listeners.ResultListener;
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
    private ResultListener resultListener;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referencia = database.getReference("Usuarios");
    private ImageView profile;
    private SharedPreferences pref;
    private TextView name, age, sex, email, phone;
    private TextInputEditText editName, editAge, editPhone;
    private AutoCompleteTextView editSex;
    private Button btnGuardar;
    private boolean editMode = false;
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
        setToolbarTitle("Mi perfil", "");
        View layout = LayoutInflater.from(this).inflate(R.layout.profile_activity,null);
        name = layout.findViewById(R.id.txtName);
        age = layout.findViewById(R.id.txtAge);
        sex = layout.findViewById(R.id.txtSexo);
        email = layout.findViewById(R.id.txtEmail);
        phone = layout.findViewById(R.id.txtTelefono);
        profile = layout.findViewById(R.id.profile);
        btnGuardar = layout.findViewById(R.id.btnGuardar);
        editAge = layout.findViewById(R.id.editAge);
        editName = layout.findViewById(R.id.editName);
        editPhone = layout.findViewById(R.id.editTelefono);
        editSex = layout.findViewById(R.id.editSexo);
        FloatingActionButton btnChangeProfile = layout.findViewById(R.id.btnChangeProfile);
        ArrayAdapter adapterGeneros = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new String[]{"Hombre", "Mujer"});
        editSex.setAdapter(adapterGeneros);
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
        resultListener = new ResultListener() {
            @Override
            public void result(String dlgTag, Object result) {
                if(dlgTag.equals("EditarPerfil")&&((String)result).equals("Accept")){
                    age.setText(editAge.getText().toString());
                    phone.setText(editPhone.getText().toString());
                    sex.setText(editSex.getText().toString());
                    name.setText(editName.getText().toString());
                    Map<String, Object> datosActualizados = new HashMap<>();
                    datosActualizados.put("telefono", editPhone.getText().toString());
                    datosActualizados.put("sexo", editSex.getText().toString());
                    datosActualizados.put("nombre", editName.getText().toString());
                    datosActualizados.put("edad", Integer.parseInt(editAge.getText().toString()));
                    updateInFirebase(datosActualizados);
                    convertViewToEditable(false);
                    editMode = false;
                }
            }
        };
        btnGuardar.setOnClickListener(e->{
            YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(ProfileActivity.this,"EditarPerfil","Guardar Cambios?","Estas seguro"
                    +" de guardar los cambios, recuerda que esto afectara a todos tus viajes que hayas publicado", resultListener);
            dlg.show();
        });
        addContent(layout);
    }

    private void convertViewToEditable(boolean editable){
        if(editable){
            editName.setText(name.getText());
            editAge.setText(age.getText());
            editPhone.setText(phone.getText());
            editSex.setText(sex.getText());
            name.setVisibility(View.GONE);
            age.setVisibility(View.GONE);
            phone.setVisibility(View.GONE);
            sex.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
            editSex.setVisibility(View.VISIBLE);
            editPhone.setVisibility(View.VISIBLE);
            editName.setVisibility(View.VISIBLE);
            editAge.setVisibility(View.VISIBLE);
        }else{
            name.setVisibility(View.VISIBLE);
            age.setVisibility(View.VISIBLE);
            phone.setVisibility(View.VISIBLE);
            sex.setVisibility(View.VISIBLE);
            btnGuardar.setVisibility(View.GONE);
            editSex.setVisibility(View.GONE);
            editPhone.setVisibility(View.GONE);
            editName.setVisibility(View.GONE);
            editAge.setVisibility(View.GONE);
        }
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

    private void updateInFirebase(Map<String, Object> datos){
        referencia.child(pref.getString("key",null)).updateChildren(datos, new DatabaseReference.CompletionListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_editar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_editar) {
            if(!editMode){
                convertViewToEditable(true);
                editMode = true;
                setToolbarTitle("Mi perfil", "(Editando Informacion)");
            }else{
                convertViewToEditable(false);
                editMode = false;
                setToolbarTitle("Mi perfil", "");
            }
            return true;
        }
        if(id == R.id.home){
            openDrawer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
