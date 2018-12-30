package com.barajasoft.raites.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.LinkedList;
import java.util.List;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.ViewHolder> {
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference usuariosReference = database.getReference("Usuarios");
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private List<SolicitudViaje> solicitudes = new LinkedList<>();
    private Context context;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    public SolicitudesAdapter(Context context){
        this.context = context;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.solicitud_viaje_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudViaje solicitud = solicitudes.get(position);
        usuariosReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    User user = data.getValue(User.class);
                    if(user.getKey().equals(solicitud.getKeyPasajero())){
                        holder.txtNombre.setText(user.getNombre());
                        imageLoader.displayImage(user.getImagenPerfil(), holder.perfil, options);
                    }
                }
                holder.btnMapa.setOnClickListener(e->{
                    Toast.makeText(context, "Esta solicitando mas espacios de los disponibles", Toast.LENGTH_LONG).show();
                    //actualizar la solicitud de denegada
                });
                holder.btnAceptar.setOnClickListener(e->{
                    Toast.makeText(context, "Esta solicitando mas espacios de los disponibles", Toast.LENGTH_LONG).show();
                    //actualizar la solicitud de denegada
                });
                holder.txtDestino.setText(solicitud.getDireccionDeParada());
                holder.txtFecha.setText(solicitud.getFechaSolicitud());
                viajesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            Viaje viaje = child.getValue(Viaje.class);
                            if(viaje.getKey().equals(solicitud.getKeyViaje())){
                                if(viaje.getEspaciosDisponibles() >= solicitud.getEspaciosSolicitados()){
                                    holder.btnMapa.setOnClickListener(e->{
                                        Toast.makeText(context, "Si pudiera aceptar este", Toast.LENGTH_LONG).show();
                                    });
                                    holder.btnAceptar.setOnClickListener(e->{
                                        Toast.makeText(context, "Si pudiera aceptar este", Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtDestino, txtNombre, txtFecha, txtEspacios;
        public ImageView perfil;
        public Button btnMapa, btnAceptar;
        public ViewHolder(View itemView) {
            super(itemView);
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtEspacios = itemView.findViewById(R.id.txtAsientosSolicitados);
            perfil = itemView.findViewById(R.id.imgPerfil);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
            btnMapa = itemView.findViewById(R.id.btnVerMapa);
        }
    }
}
