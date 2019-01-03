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
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Dialogs.AlertDialog;
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
    private LinearLayout carAddedLayout;
    private SharedPreferences pref;
    private TextView marca, modelo, matricula, espacio;
    private TextInputEditText editMarca, editModelo, editMatricula, editEspacio;
    private ImageView  carImage;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private StorageReference storageReference;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference referencia = database.getReference("Vehiculos");
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
        editEspacio = layout.findViewById(R.id.editEspacios);
        carImage = layout.findViewById(R.id.carImage);
        FloatingActionButton btnCarImage = layout.findViewById(R.id.carImageButton);
        Button btnEliminar = layout.findViewById(R.id.btnEliminarVehiculo);
        btnGuardar = layout.findViewById(R.id.btnGuardarVehiculo);
        ResultListener listener = new ResultListener() {
            @Override
            public void result(String dlgTag, Object result) {
                switch (dlgTag){
                    case "EditarVehiculo":
                        if(((String)result).equals("Accept")){
                            espacio.setText(editEspacio.getText().toString());
                            marca.setText(editMarca.getText().toString());
                            modelo.setText(editModelo.getText().toString());
                            matricula.setText(editMatricula.getText().toString());
                            Map<String, Object> datosActualizados = new HashMap<>();
                            datosActualizados.put("marca", editMarca.getText().toString());
                            datosActualizados.put("modelo", editModelo.getText().toString());
                            datosActualizados.put("matricula", editMatricula.getText().toString());
                            if(editEspacio.getText().toString().equals(""))
                                datosActualizados.put("espaciosDisponibles", 0);
                            else
                                datosActualizados.put("espaciosDisponibles", Integer.parseInt(editEspacio.getText().toString()));
                            updateInFirebase(datosActualizados);
                            convertViewToEditable(false);
                            editMode = false;
                        }
                        break;
                    case "EliminarVehiculo":
                        if(((String)result).equals("Accept")){
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
            YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(MiVehiculoActivity.this,"EliminarVehiculo","Eliminar Vehiculo?","Estas seguro"
                    +" de guardar los cambios, recuerda que esto afectara a todos tus viajes que hayas publicado", listener);
            dlg.show();
        });
        btnGuardar.setOnClickListener(e->{
            YesOrNoChooserDialog dlg = new YesOrNoChooserDialog(MiVehiculoActivity.this,"EditarVehiculo","Guardar Cambios?","Estas seguro"
                    +" de guardar los cambios, recuerda que esto afectara a todos tus viajes que hayas publicado", listener);
            dlg.show();
        });
        btnCarImage.setOnClickListener(e->{
            Intent intent = new Intent( Intent.ACTION_GET_CONTENT );
            intent.setType( "image/*" );
            startActivityForResult( intent, PICK_IMAGE);
        });
        btnAgregarVehiculo.setOnClickListener(e->{
            startActivity(new Intent(MiVehiculoActivity.this,RegisterCarActivity.class));
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
            updateUI();
            Log.e("Agregado","Segun entro aqui");
        }else{
            noCarLayout.setVisibility(View.VISIBLE);
            carAddedLayout.setVisibility(View.GONE);
            Log.e("NoAgregado","Segun entro aqui");
        }

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

    private void convertViewToEditable(boolean editable){
        if(editable){
            editEspacio.setText(espacio.getText());
            editMatricula.setText(matricula.getText());
            editModelo.setText(modelo.getText());
            editMarca.setText(marca.getText());
            espacio.setVisibility(View.GONE);
            matricula.setVisibility(View.GONE);
            modelo.setVisibility(View.GONE);
            marca.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.VISIBLE);
            editMarca.setVisibility(View.VISIBLE);
            editModelo.setVisibility(View.VISIBLE);
            editEspacio.setVisibility(View.VISIBLE);
            editMatricula.setVisibility(View.VISIBLE);
        }else{
            espacio.setVisibility(View.VISIBLE);
            marca.setVisibility(View.VISIBLE);
            modelo.setVisibility(View.VISIBLE);
            matricula.setVisibility(View.VISIBLE);
            btnGuardar.setVisibility(View.GONE);
            editMatricula.setVisibility(View.GONE);
            editEspacio.setVisibility(View.GONE);
            editModelo.setVisibility(View.GONE);
            editMarca.setVisibility(View.GONE);
        }
    }

    private void updateInFirebase(Map<String, Object> datos){
        referencia.child(pref.getString("keyVehiculo",null)).updateChildren(datos, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(getApplicationContext(),"Cambio realizado con exito",Toast.LENGTH_SHORT).show();
            }
        });
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
                setToolbarTitle("Mi Vehiculo", "(Editando Informacion)");
            }else{
                convertViewToEditable(false);
                editMode = false;
                setToolbarTitle("Mi Vehiculo", "");
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
