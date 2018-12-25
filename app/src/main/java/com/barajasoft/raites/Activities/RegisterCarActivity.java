package com.barajasoft.raites.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.barajasoft.raites.Entities.Vehiculo;
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
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.io.FileOutputStream;

public class RegisterCarActivity extends BaseActivity {
    private Uri imageDir ;
    private StorageReference storageReference;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referencia = database.getReference("Vehiculos");
    private boolean imageSelected = false;
    private CircularImageView profile;
    private TextInputEditText marca, modelo, matricula, espaciosDisponibles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //deshabilita el menu del fondo definido en la clase padre
        disableBottomMenu();
        disableViewPager();
        disableDrawer();
        disableBottomMenu();
        disableViewPager();
        initDrawer();
        setNavViewMenu("miVehiculo");
        setToolbar("","Registro De Vehiculo");
        View layout = LayoutInflater.from(this).inflate(R.layout.registrar_vehiculo_activity,null);
        profile = layout.findViewById(R.id.profile);
        FloatingActionButton btnCarImage = layout.findViewById(R.id.carImageButton);
        Button btnRegistrar = layout.findViewById(R.id.btnRegistrar);
        marca = layout.findViewById(R.id.editMarca);
        modelo = layout.findViewById(R.id.editModelo);
        matricula = layout.findViewById(R.id.editMatricula);
        espaciosDisponibles = layout.findViewById(R.id.editEspaciosDisponibles);
        imageDir = Uri.EMPTY;
        storageReference = FirebaseStorage.getInstance().getReference();
        btnCarImage.setOnClickListener(e->{
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
                saveProfilePicture(getCurrentUserKey());
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
            archivo = new File(myDir,"vehiculo.jpg");
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
        }else{
            link = Uri.parse("android.resource://"+getApplication().getPackageName()+"/drawable/carro_no_disponible");
        }
        StorageReference arch = storageReference.child(key+"/"+"vehiculo.jpg");
        arch.putFile(link)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.e("State","Subido");
                        storageReference.child(key+"/vehiculo.jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                Vehiculo vehiculo = new Vehiculo();
                                vehiculo.setEspaciosDisponibles(Integer.parseInt(espaciosDisponibles.getText().toString()));
                                vehiculo.setMarca(marca.getText().toString());
                                vehiculo.setMatricula(matricula.getText().toString());
                                vehiculo.setModelo(modelo.getText().toString());
                                vehiculo.setUserKey(key);
                                vehiculo.setImageLink(task.getResult().toString());
                                referencia.child(vehiculo.getKey()).setValue(vehiculo, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        Toast.makeText(getApplicationContext(),"Vehiculo Registrado Exitosamente",Toast.LENGTH_SHORT).show();
                                        setVehiculoSesionData(vehiculo);
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
        sb.append("No se pudo registrar el vehiculo por que cometiste los siguientes errores: \n");
        if(imageDir.toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar una imagen de perfil.\n");
        }
        if(marca.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar la marca.\n");
        }
        if(modelo.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar el modelo.\n");
        }
        if(matricula.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar la matricula.\n");
        }
        if(espaciosDisponibles.getText().toString().isEmpty()){
            valido = false;
            sb.append("*Tienes que ingresar los espacios disponibles tiene.\n");
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
