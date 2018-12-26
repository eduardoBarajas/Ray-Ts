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
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.AlertDialog;
import com.barajasoft.raites.Dialogs.OptionChooserDialog;
import com.barajasoft.raites.Dialogs.SingleDataEditDialog;
import com.barajasoft.raites.Listeners.DialogResultListener;
import com.barajasoft.raites.R;
import com.barajasoft.raites.Utilities.ImageAngleCorrector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class MiVehiculoActivity extends BaseActivity {
    private LinearLayout noCarLayout;
    private ConstraintLayout carAddedLayout;
    private SharedPreferences pref;
    private TextView marca, modelo, matricula, espacio;
    private ImageView editMarca, editModelo, editMatricula, editEspacio, carImage;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private StorageReference storageReference;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referencia = database.getReference("Vehiculos");
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
        setNavViewMenu("miVehiculo");
        setToolbar("","Mi Vehiculo");
        View layout = LayoutInflater.from(this).inflate(R.layout.mi_vehiculo_activity,null);
        Button btnAgregarVehiculo = layout.findViewById(R.id.btnAgregarAuto);
        noCarLayout = layout.findViewById(R.id.noCarLayout);
        carAddedLayout = layout.findViewById(R.id.carAddedLayout);
        marca = layout.findViewById(R.id.Marca);
        modelo = layout.findViewById(R.id.Modelo);
        matricula = layout.findViewById(R.id.Matricula);
        espacio = layout.findViewById(R.id.EspaciosDisponibles);
        editMarca = layout.findViewById(R.id.editMarca);
        editModelo = layout.findViewById(R.id.editModelo);
        editMatricula = layout.findViewById(R.id.editMatricula);
        editEspacio = layout.findViewById(R.id.editEspaciosDisponibles);
        carImage = layout.findViewById(R.id.carImage);
        FloatingActionButton btnCarImage = layout.findViewById(R.id.carImageButton);
        Button btnEliminar = layout.findViewById(R.id.btnEliminarVehiculo);
        DialogResultListener listener = new DialogResultListener() {
            @Override
            public void result(String dlgTag, Object result) {
                switch (dlgTag){
                    case "EditMarca":
                        if(!((String)result).isEmpty()){
                            marca.setText((String)result);
                            updateFieldInFirebase("marca",result,false);
                        }
                        break;
                    case "EditModelo":
                        if(!((String)result).isEmpty()){
                            modelo.setText((String)result);
                            updateFieldInFirebase("modelo",result,false);
                        }
                        break;
                    case "EditMatricula":
                        if(!((String)result).isEmpty()){
                            matricula.setText((String)result);
                            updateFieldInFirebase("matricula",result,false);
                        }
                        break;
                    case "EditEspacio":
                        if(!((String)result).isEmpty()){
                            espacio.setText((String)result);
                            updateFieldInFirebase("espaciosDisponibles",result, true);
                        }
                        break;
                    case "EliminarVehiculo":
                        if(((String)result).equals("Eliminar")){
                            referencia.child(pref.getString("keyVehiculo", null)).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    StorageReference arch = storageReference.child(pref.getString("key", null)+"/"+"vehiculo.jpg");
                                    arch.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            deleteVehiculoSesion();
                                            Toast.makeText(getApplicationContext(),"Se elimino correctamente el vehiculo",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        }else{
                            Toast.makeText(getApplicationContext(),"Cancelaste la eliminacion del vehiculo",Toast.LENGTH_SHORT).show();
                        }
                }
            }
        };
        btnEliminar.setOnClickListener(e->{
            OptionChooserDialog dialog = new OptionChooserDialog(MiVehiculoActivity.this,"EliminarVehiculo",
                    "Eliminar Vehiculo","Eliminar","Cancelar",listener);
            dialog.show();
        });
        btnCarImage.setOnClickListener(e->{
            Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
            intent.setType( "image/*" );
            startActivityForResult( intent, PICK_IMAGE);
        });
        btnAgregarVehiculo.setOnClickListener(e->{
            startActivity(new Intent(MiVehiculoActivity.this,RegisterCarActivity.class));
        });
        editMarca.setOnClickListener(e->{
            SingleDataEditDialog dialog = new SingleDataEditDialog(MiVehiculoActivity.this,
                    "EditMarca","Editar Marca",marca.getText().toString(),listener);
            dialog.show();
        });
        editModelo.setOnClickListener(e->{
            SingleDataEditDialog dialog = new SingleDataEditDialog(MiVehiculoActivity.this,
                    "EditModelo","Editar Modelo",modelo.getText().toString(),listener);
            dialog.show();
        });
        editMatricula.setOnClickListener(e->{
            SingleDataEditDialog dialog = new SingleDataEditDialog(MiVehiculoActivity.this,
                    "EditMatricula","Editar Matricula",matricula.getText().toString(),listener);
            dialog.show();
        });
        editEspacio.setOnClickListener(e->{
            SingleDataEditDialog dialog = new SingleDataEditDialog(MiVehiculoActivity.this,
                    "EditEspacio","Editar Numero De Espacios Disponibles",espacio.getText().toString(),listener);
            dialog.show();
        });
        addContent(layout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(pref.getBoolean("carAdded",false)){
            noCarLayout.setVisibility(View.GONE);
            carAddedLayout.setVisibility(View.VISIBLE);
            updateUI();
        } else {
            noCarLayout.setVisibility(View.VISIBLE);
            carAddedLayout.setVisibility(View.GONE);
        }
    }

    private void updateUI() {
        matricula.setText(pref.getString("matricula",null));
        modelo.setText(pref.getString("modelo",null));
        marca.setText(pref.getString("marca",null));
        espacio.setText(String.valueOf(pref.getInt("espaciosDisponibles",-1)));
        imageLoader.displayImage(pref.getString("imageVehiculoLink",null),carImage,options);
    }

    @Override
    protected void update() {
        super.update();
        if(isVehiculoAgregado()){
            noCarLayout.setVisibility(View.GONE);
            carAddedLayout.setVisibility(View.VISIBLE);
            Log.e("Agregado","Segun entro aqui");
        }else{
            noCarLayout.setVisibility(View.VISIBLE);
            carAddedLayout.setVisibility(View.GONE);
            Log.e("NoAgregado","Segun entro aqui");
        }
        Log.e("Updated","lol");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri imageDir = data.getData();
            carImage.setImageBitmap(ImageAngleCorrector.getFixedBitmap(getContentResolver(),imageDir));
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir, archivo;
            myDir = new File(root + "/Raites/Resources/Temp");
            myDir.mkdirs();
            archivo = new File(myDir,"vehiculo.jpg");
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
            StorageReference arch = storageReference.child(pref.getString("key", null)+"/"+"vehiculo.jpg");
            arch.putFile(link)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.child(pref.getString("key", null)+"/vehiculo.jpg").getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    Map<String,Object> dato = new HashMap<>();
                                    dato.put("imageLink", task.getResult().toString());
                                    referencia.child(pref.getString("keyVehiculo", null)).updateChildren(dato, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            Toast.makeText(getApplicationContext(),"Imagen del vehiculo actualizada",Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getApplicationContext(),"No se pudo actualizar la imagen del vehiculo",Toast.LENGTH_SHORT).show();
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
        referencia.child(pref.getString("keyVehiculo",null)).updateChildren(dato, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(getApplicationContext(),"Cambio realizado con exito",Toast.LENGTH_SHORT).show();
            }
        });
    }

}
