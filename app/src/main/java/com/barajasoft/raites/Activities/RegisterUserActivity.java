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
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.ImageAngleCorrector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;

public class RegisterUserActivity extends BaseActivity {
    private Uri imageDir ;
    private StorageReference storageReference;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referencia = database.getReference("Usuarios");
    private boolean imageSelected = false;
    private FirebaseAuth auth;
    private ImageView profile;
    private TextInputEditText name, sexo, edad, correo, telefono, password;
    private User nuevoUsuario;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        disableDrawer();
        setToolbar("","Registro De Usuario");
        View layout = LayoutInflater.from(this).inflate(R.layout.register_user_activity,null);
        FloatingActionButton btnProfileImage = layout.findViewById(R.id.profileImageButton);
        Button btnRegistrar = layout.findViewById(R.id.btnRegistrar);
        name = layout.findViewById(R.id.editName);
        sexo = layout.findViewById(R.id.editSex);
        edad = layout.findViewById(R.id.editAge);
        correo = layout.findViewById(R.id.editEmail);
        telefono = layout.findViewById(R.id.editTelefono);
        password = layout.findViewById(R.id.editPassword);
        profile = layout.findViewById(R.id.profile);
        imageDir = Uri.EMPTY;
        nuevoUsuario = new User();
        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        btnProfileImage.setOnClickListener(e->{
            Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
            intent.setType( "image/*" );
            startActivityForResult( intent, PICK_IMAGE);
        });
        btnRegistrar.setOnClickListener(e->{
            //para esconder el teclado virtual se usan estas dos lineas siguientes
            InputMethodManager imm = (InputMethodManager) layout.getContext().getSystemService(layout.getContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
            //checar si los datos son validos
            if(checkFields()){
                //son validos
                //checar si ya existe el correo en la base de datos
                auth.createUserWithEmailAndPassword(correo.getText().toString(),password.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            //agregar a la base de datos
                            nuevoUsuario.setCorreo(correo.getText().toString());
                            nuevoUsuario.setEdad(Integer.parseInt(edad.getText().toString()));
                            nuevoUsuario.setNombre(name.getText().toString());
                            nuevoUsuario.setRating(3);
                            nuevoUsuario.setSexo(sexo.getText().toString());
                            nuevoUsuario.setTelefono(telefono.getText().toString());
                            //limpiar textos
                            saveProfilePicture(nuevoUsuario.getKey());
                        } else {
                            // If sign in fails, display a message to the user.
                            //checar si ya existe en la base de datos
                            Snackbar.make(layout, "No se pudo crear la cuenta:" + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        addContent(layout);
    }

    private void saveProfilePicture(String key) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir, archivo;
        Uri link = Uri.EMPTY;
        if(imageSelected){
            myDir = new File(root + "/Raites/Resources/Temp");
            myDir.mkdirs();
            archivo = new File(myDir,"perfil.jpg");
            try {
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(ImageAngleCorrector.getFixedBitmap(getContentResolver(),imageDir), 480, 360, false);
                FileOutputStream out = new FileOutputStream(archivo);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                link = Uri.fromFile(archivo);
            } catch (Exception e) {
                Log.e("ErrorImagen","No se pudo convertir la imagen");
            }
        }else{
            link = Uri.parse("android.resource://"+getApplication().getPackageName()+"/drawable/no_profile");
        }
        StorageReference arch = storageReference.child(key+"/"+"perfil.jpg");
        arch.putFile(link)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.e("State","Subido");
                        nuevoUsuario.setImagenPerfil(arch.getDownloadUrl().toString());
                        storageReference.child(key+"/perfil.jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                nuevoUsuario.setImagenPerfil(task.getResult().toString());
                                referencia.child(key).setValue(nuevoUsuario, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Toast.makeText(getApplicationContext(),"Usuario Creado Exitosamente",Toast.LENGTH_SHORT).show();
                                        setUserSesionData(nuevoUsuario);
                                        finish();
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
                    }
                });
    }

    private boolean checkFields() {
        boolean valido = true;
        StringBuilder sb = new StringBuilder();
        sb.append("No se ha creado el usuario por que cometiste los siguientes errores: \n");
        if(imageDir.toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar una imagen de perfil.\n");
        }
        if(name.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar el nombre.\n");
        }
        if(name.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar el nombre.\n");
        }
        if(password.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar la contraseña.\n");
        }
        if(correo.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar el correo.\n");
        }
        if(edad.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar la edad.\n");
        }
        if(sexo.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar el sexo.\n");
        }
        if(telefono.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar el telefono.\n");
        }
        if(!valido){
            Toast.makeText(getApplicationContext(),sb.toString(),Toast.LENGTH_LONG).show();
        }
        return valido;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            imageDir = data.getData();
            profile.setImageBitmap(ImageAngleCorrector.getFixedBitmap(getContentResolver(),imageDir));
            imageSelected = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
