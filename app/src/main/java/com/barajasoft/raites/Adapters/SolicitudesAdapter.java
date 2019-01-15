package com.barajasoft.raites.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Activities.VisualizeTravelActivity;
import com.barajasoft.raites.Dialogs.OptionChooserDialog;
import com.barajasoft.raites.Dialogs.YesOrNoChooserDialog;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.Listeners.ResultListener;
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
import com.nostra13.universalimageloader.utils.L;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.ViewHolder> {
    private List<SolicitudViaje> solicitudes = new LinkedList<>();
    private Viaje currentViaje = null;
    private List<User> usuarios = new LinkedList<>();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference solicitudViajesReference = database.getReference("SolicitudesDeViaje");
    private Context context;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private ResultListener listener;
    private SolicitudViaje solicitud;
    private SharedPreferences pref;
    private StringBuilder sb = new StringBuilder();

    public SolicitudesAdapter(Context context, ResultListener resultListener){
        listener = resultListener;
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.solicitud_viaje_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        solicitud = solicitudes.get(position);
        //se recupera el usuario de la solicitud
        User currentUser = null;
        for(User u: usuarios)
            if(u.getKey().equals(solicitud.getKeyPasajero())){
                currentUser = u;
                break;
            }
        //si la solicitud esta aceptada entonces se esconde el boton de agregar
        if(solicitud.isAceptada()){
            holder.btnAceptar.setVisibility(View.GONE);
            holder.btnCancelar.setVisibility(View.VISIBLE);
        }else{
            holder.btnAceptar.setVisibility(View.VISIBLE);
            holder.btnCancelar.setVisibility(View.GONE);
        }

        //se configura los holders del view
        holder.txtNombre.setText(currentUser.getNombre());
        imageLoader.displayImage(currentUser.getImagenPerfil(), holder.perfil, options);
        holder.txtDestino.setText(solicitud.getDireccionDeParada());
        holder.txtFecha.setText(solicitud.getFechaSolicitud());
        holder.btnMapa.setOnClickListener(e->{
            Intent intent = new Intent(context ,VisualizeTravelActivity.class);
            intent.putExtra("visualizeMapParada", true);
            intent.putExtra("KeySolicitud", solicitud.getKey());
//            if(!solicitud.isAceptada()){
//                //si no esta en modo edicion y la solicitud aun no es aceptada entonces se
//                //deben agregar los puntos manualmente para que puedan ser visualizados
//                intent.putExtra("direccionSolicitud", solicitud.getDireccionDeParada());
//                intent.putExtra("latitudSolicitud", solicitud.getPuntoDeParada().getLatitude());
//                intent.putExtra("longitudSolicitud", solicitud.getPuntoDeParada().getLongitude());
//            }
            intent.putExtra("direccionDestino", currentViaje.getDireccionDestino());
            intent.putExtra("direccionSalida", currentViaje.getDireccionSalida());
            intent.putExtra("latitudSalida", currentViaje.getPuntosDeViaje().get(0).getLatitude());
            intent.putExtra("longitudSalida", currentViaje.getPuntosDeViaje().get(0).getLongitude());
            intent.putExtra("latitudDestino", currentViaje.getPuntosDeViaje().get(1).getLatitude());
            intent.putExtra("longitudDestino", currentViaje.getPuntosDeViaje().get(1).getLongitude());
            int var = 0;
            //si el la solicitud actual aun no se acepta entonces se debe agregar manualmente
            //pero debe ser solo para la solicitud actual

            if(!solicitudes.get(position).isAceptada())
                var = 1;
            String[] puntosArray = new String[currentViaje.getPuntosDeParada().size() + var];
            for(int i = 0; i<currentViaje.getPuntosDeParada().size(); i++){
                puntosArray[i] = String.valueOf(currentViaje.getPuntosDeParada().get(i).getLatitude())+":"+
                        String.valueOf(currentViaje.getPuntosDeParada().get(i).getLongitude());
            }
            if(var == 1)
                puntosArray[currentViaje.getPuntosDeParada().size()] = String.valueOf(solicitudes.get(position).getPuntoDeParada().getLatitude())+":"+
                        String.valueOf(solicitudes.get(position).getPuntoDeParada().getLongitude());
            intent.putExtra("puntosParada", puntosArray);
            List<String> direccionesParada = new LinkedList<>();
            for(SolicitudViaje s : solicitudes){
                direccionesParada.add(s.getDireccionDeParada());
            }
            String[] direccionesParadaArray = new String[direccionesParada.size() + var];
            for(int i = 0; i < direccionesParada.size(); i++){
                direccionesParadaArray[i] = direccionesParada.get(i);
            }
            if(var == 1)
                direccionesParadaArray[direccionesParada.size()] = solicitudes.get(position).getDireccionDeParada();
            intent.putExtra("direccionesPuntosParada", direccionesParadaArray);
            List<String> nombres = new LinkedList<>();
            for(String keyUser : currentViaje.getKeysPasajeros()){
                for(User user : usuarios) {
                    if(user.getKey().equals(keyUser)) {
                        nombres.add(user.getNombre());
                    }
                    if(user.getKey().equals(pref.getString("key", null))){
                        intent.putExtra("currentUserKey", user.getKey());
                        intent.putExtra("currentUserName", user.getNombre());
                    }
                    if(user.getKey().equals(solicitudes.get(position).getKeyPasajero())){
                        intent.putExtra("currentSolicitudUserKey", user.getKey());
                        intent.putExtra("currentSolicitudUserName", user.getNombre());
                    }
                }
            }
            if(var == 1){
                for(User u : usuarios){
                    if(u.getKey().equals(solicitudes.get(position).getKeyPasajero())){
                        nombres.add(u.getNombre());
                        intent.putExtra("currentSolicitudUserKey", u.getKey());
                        intent.putExtra("currentSolicitudUserName", u.getNombre());
                    }
                }
            }
            String[] nombresArray = new String[nombres.size()];
            for(int i = 0; i<nombres.size(); i++){
                nombresArray[i] = nombres.get(i);
            }
            intent.putExtra("usersParadas", nombresArray);
            context.startActivity(intent);
        });
        holder.btnAceptar.setOnClickListener(e->{
            listener.result("AcceptRequest", solicitudes.get(position));
        });
        holder.btnCancelar.setOnClickListener(e->{
            listener.result("CancelRequest", solicitudes.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return solicitudes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtDestino, txtNombre, txtFecha, txtEspacios;
        public ImageView perfil;
        public Button btnMapa, btnAceptar, btnCancelar;
        public ViewHolder(View itemView) {
            super(itemView);
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtEspacios = itemView.findViewById(R.id.txtAsientosSolicitados);
            perfil = itemView.findViewById(R.id.imgPerfil);
            btnAceptar = itemView.findViewById(R.id.btnAceptar);
            btnMapa = itemView.findViewById(R.id.btnVerMapa);
            btnCancelar = itemView.findViewById(R.id.btnCancelar);
        }
    }

    public void addSolicitud(Viaje viaje, User user, SolicitudViaje solicitudViaje){
        if(!solicitudes.contains(solicitudViaje))
            solicitudes.add(solicitudViaje);
        if(!usuarios.contains(user))
            usuarios.add(user);
        currentViaje = viaje;
    }
    public void removeSolicitud(String keySolicitud){
        int[] indexes = new int[]{-1, -1};
        for(SolicitudViaje v : solicitudes){
            if(v.getKeyViaje().equals(keySolicitud)){
                indexes[0] = solicitudes.indexOf(v);
                break;
            }
        }
        for(User user : usuarios){
            if(indexes[0]!=-1)
                if(user.getKey().equals(solicitudes.get(indexes[0]).getKeyPasajero())){
                    indexes[1] = usuarios.indexOf(user);
                    break;
                }
        }
        if(indexes[0]!=-1)
            solicitudes.remove(indexes[0]);
        if(indexes[1]!=-1)
            usuarios.remove(indexes[1]);
    }

    public void updateSolicitud(SolicitudViaje solicitud){
        int index = -1;
        for(SolicitudViaje s : solicitudes)
            if(s.getKey().equals(solicitud.getKey())) {
                index = solicitudes.indexOf(s);
                break;
            }
        if(index != -1)
            solicitudes.set(index, solicitud);
    }

    public void clear(){
        solicitudes.clear();
        usuarios.clear();
        currentViaje = null;
    }
}
