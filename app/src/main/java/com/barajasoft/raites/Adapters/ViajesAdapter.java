package com.barajasoft.raites.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Activities.ExpandViajeActivity;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.LinkedList;
import java.util.List;

public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViajesViewHolder>{
    private List<Viaje> viajeList = new LinkedList<>();
    private Context context;
    private SharedPreferences pref;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference usuariosReference = database.getReference("Usuarios");

    public ViajesAdapter(Context context){
        this.context = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.loading_image) // resource or drawable
                .showImageForEmptyUri(R.drawable.no_profile) // resource or drawable
                .resetViewBeforeLoading(false)  // default
                .cacheInMemory(true) // default
                .cacheOnDisk(true) // default
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2) // default
                .build();
        imageLoader.init(ImageLoaderConfiguration.createDefault(context));
    }

    @NonNull
    @Override
    public ViajesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViajesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viajes_activos_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViajesViewHolder holder, int position) {
        Viaje viaje = viajeList.get(position);
        holder.txtCupo.setText(String.valueOf(viaje.getEspaciosDisponibles()));
        holder.txtHora.setText(viaje.getHoraViaje());
        holder.txtFecha.setText(viaje.getFechaViaje());
        holder.txtDestino.setText(viaje.getDireccionDestino());
        holder.txtSalida.setText(viaje.getDireccionSalida());
        if(pref.getString("key", null).equals(viaje.getKeyConductor())){
            holder.txtRol.setText("Conductor");
        }else{
            holder.txtRol.setText("Pasajero");
        }
        holder.verMas.setOnClickListener(e->{
            context.startActivity(new Intent(context, ExpandViajeActivity.class));
        });
        usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    if(viaje.getKeyConductor().equals(data.getValue(User.class).getKey())){
                        Log.e("user",data.getValue(User.class).getCorreo());
                        imageLoader.displayImage(data.getValue(User.class).getImagenPerfil(),holder.perfil,options);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public int getItemCount() {
        return viajeList.size();
    }

    public class ViajesViewHolder extends RecyclerView.ViewHolder {
        public TextView txtSalida, txtDestino, txtCupo, txtFecha, txtHora, txtRol;
        public Button verMas;
        public CircularImageView perfil;
        public ViajesViewHolder(View itemView) {
            super(itemView);
            txtCupo = itemView.findViewById(R.id.txtCupo);
            txtSalida = itemView.findViewById(R.id.txtSalida);
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtHora = itemView.findViewById(R.id.txtHora);
            txtRol = itemView.findViewById(R.id.txtRol);
            verMas = itemView.findViewById(R.id.btnVerMas);
            perfil = itemView.findViewById(R.id.img_perfil);
        }
    }

    public void addViaje(Viaje viaje){
        viajeList.add(viaje);
    }

    public void removeViaje(String key){
        Viaje eliminar = null;
        for(Viaje v : viajeList){
            if(v.getKey().equals(key))
                eliminar = v;
        }
        if(eliminar != null)
            viajeList.remove(eliminar);
    }

    public void clear(){
        viajeList.clear();
    }
}
