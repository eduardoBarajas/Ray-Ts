package com.barajasoft.raites.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.barajasoft.raites.Activities.ExpandSolicitudViajeActivity;
import com.barajasoft.raites.Activities.ExpandViajeActivity;
import com.barajasoft.raites.Activities.SolicitudesViajeActivity;
import com.barajasoft.raites.Dialogs.EstadoSolicitudDialog;
import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.Entities.User;
import com.barajasoft.raites.Entities.Viaje;
import com.barajasoft.raites.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ViajesAdapter extends RecyclerView.Adapter<ViajesAdapter.ViajesViewHolder>{
    private List<Viaje> viajeList = new LinkedList<>();
    private Context context;
    private SharedPreferences pref;
    private SolicitudViaje solicitudActual;
    private Map<String, List<SolicitudViaje>> solicitudesViaje = new HashMap<>();
    private Map<String, Integer> usersSolicitud = new HashMap<>();


    public ViajesAdapter(Context context){
        this.context = context;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public ViajesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViajesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viajes_activos_model, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViajesViewHolder holder, int position) {
        Viaje viaje = viajeList.get(position);
        //para ajustar un contraint dinamicamente
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(holder.rootConstraint);
       //si el texto de la direccion de destino es mas largo que el de direccion de salida entonces se coloca el constraint
        //sobre el
        if(viaje.getDireccionDestino().length() > viaje.getDireccionSalida().length())
            constraintSet.connect(holder.divisorCard.getId(), ConstraintSet.TOP, holder.txtDestino.getId(), ConstraintSet.BOTTOM,8);
        else
            constraintSet.connect(holder.divisorCard.getId(), ConstraintSet.TOP, holder.txtSalida .getId(), ConstraintSet.BOTTOM,8);

        constraintSet.applyTo(holder.rootConstraint);
        holder.txtEspacios.setText(String.valueOf(viaje.getEspaciosDisponibles()));
        holder.txtHora.setText("Sale a la(s) "+viaje.getHoraViaje());
        holder.txtFecha.setText(viaje.getFechaViaje());
        holder.txtSalidaHeader.setText(viaje.getDireccionSalida().split(",")[1].substring(1, 4));
        holder.txtDestinoHeader.setText(viaje.getDireccionDestino().split(",")[1].substring(1, 4));
        holder.txtDestino.setText(viaje.getDireccionDestino().split(",")[0]);
        holder.txtSalida.setText(viaje.getDireccionSalida().split(",")[0]);
        //si el usuario actual es el conductor se agrega la etiqueta de conductor, si no lo es se busca entre
        //los pasajeros del viaje, si tampoco entonces se queda vacio
        if(pref.getString("key", null).equals(viaje.getKeyConductor()))
            holder.txtRol.setText("Conductor");
        else
            for(String pasajero : viaje.getKeysPasajeros())
                if(pref.getString("key", null).equals(pasajero))
                    holder.txtRol.setText("Pasajero");

        boolean solicitudFound = false;
        if(solicitudesViaje.get(viaje.getKey())!=null)
            for(int i = 0; i < solicitudesViaje.get(viaje.getKey()).size(); i++){
                solicitudActual = solicitudesViaje.get(viaje.getKey()).get(i);
                if(solicitudActual.getKeyPasajero().equals(pref.getString("key", null))){
                    usersSolicitud.put(pref.getString("key", null), i);
                    solicitudFound = true;
                    holder.btnSolicitudes.setOnClickListener(e->{
                        Intent intent = new Intent(context, ExpandSolicitudViajeActivity.class);
                        intent.putExtra("KeySolicitud", solicitudesViaje.get(viaje.getKey()).get(usersSolicitud.get(pref.getString("key", null))).getKey());
                        intent.putExtra("KeyViaje", viaje.getKey());
                        context.startActivity(intent);
                    });
                }
                //si entra en la siguiente condicion quiere decir que el usuario actual es el conductor de ese viaje
                //por lo que lo redireccionara a las solicitudes hechas para ese viaje
                if(viaje.getKeyConductor().equals(pref.getString("key", null))){
                    solicitudFound = true;
                    holder.btnSolicitudes.setOnClickListener(e->{
                        holder.btnSolicitudes.setText("Solicitudes");
                        Intent intent = new Intent(context, SolicitudesViajeActivity.class);
                        intent.putExtra("KeyViaje", viaje.getKey());
                        context.startActivity(intent);
                    });
                }
            }
        if(solicitudFound)
            holder.btnSolicitudes.setVisibility(View.VISIBLE);
        else
            holder.btnSolicitudes.setVisibility(View.GONE);

        holder.verMas.setOnClickListener(e->{
            Intent intent = new Intent(context, ExpandViajeActivity.class);
            intent.putExtra("Destino", viaje.getDireccionDestino());
            intent.putExtra("Salida", viaje.getDireccionSalida());
            intent.putExtra("FechaSalida", viaje.getFechaViaje());
            intent.putExtra("HoraSalida", viaje.getHoraViaje());
            intent.putExtra("KeyConductor", viaje.getKeyConductor());
            intent.putExtra("KeyViaje", viaje.getKey());
            //si la solicitudActual no es nula se envia como parte del intent
            if(solicitudActual!=null)
                intent.putExtra("KeySolicitud", solicitudActual.getKey());
            String[] pasajeros = new String[viaje.getKeysPasajeros().size()];
            for(int i = 0; i < pasajeros.length; i++)
                pasajeros[i] = viaje.getKeysPasajeros().get(i);
            intent.putExtra("PasajerosKeys", pasajeros);
            intent.putExtra("EspaciosDisponibles", viaje.getEspaciosDisponibles());
            intent.putExtra("latitudSalida", viaje.getPuntosDeViaje().get(0).getLatitude());
            intent.putExtra("longitudSalida", viaje.getPuntosDeViaje().get(0).getLongitude());
            intent.putExtra("latitudDestino", viaje.getPuntosDeViaje().get(1).getLatitude());
            intent.putExtra("longitudDestino", viaje.getPuntosDeViaje().get(1).getLongitude());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return viajeList.size();
    }

    public class ViajesViewHolder extends RecyclerView.ViewHolder {
        public TextView txtSalidaHeader, txtSalida , txtDestino, txtDestinoHeader, txtFecha, txtHora, txtRol, txtEspacios;
        public Button verMas, btnSolicitudes;
        public ConstraintLayout rootConstraint;
        public View divisorCard;
        public ViajesViewHolder(View itemView) {
            super(itemView);
            divisorCard = itemView.findViewById(R.id.divisorCard);
            rootConstraint = itemView.findViewById(R.id.rootConstraint);
            txtSalidaHeader = itemView.findViewById(R.id.txtSalidaHeader);
            txtDestinoHeader = itemView.findViewById(R.id.txtDestinoHeader);
            txtSalida = itemView.findViewById(R.id.txtSalida);
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtHora = itemView.findViewById(R.id.txtHora);
            txtRol = itemView.findViewById(R.id.txtRol);
            verMas = itemView.findViewById(R.id.btnVerMas);
            btnSolicitudes = itemView.findViewById(R.id.btnSolicitud);
            txtEspacios = itemView.findViewById(R.id.txtEspacios);
        }
    }

    public void addViaje(Viaje viaje){
        viajeList.add(viaje);
    }

    public void removeViaje(String key){
        Viaje eliminar = null;
        for(Viaje v : viajeList)
            if(v.getKey().equals(key))
                eliminar = v;
        if(eliminar != null)
            viajeList.remove(eliminar);
    }

    public void modifyViaje(Viaje viaje){
        for(Viaje v : viajeList)
            if(v.getKey().equals(viaje.getKey()))
                viajeList.set(viajeList.indexOf(v), viaje);
    }

    public void clear(){
        viajeList.clear();
        solicitudesViaje.clear();
    }

    public void addSolicitudViaje(String viajeKey, SolicitudViaje solicitud){
        boolean found = false;
        if(solicitudesViaje.get(viajeKey) == null) {
            solicitudesViaje.put(viajeKey, new LinkedList<>());
            solicitudesViaje.get(viajeKey).add(solicitud);
        }else{
            for(int i = 0; i < solicitudesViaje.get(viajeKey).size(); i++)
                if(solicitudesViaje.get(viajeKey).get(i).getKey().equals(solicitud.getKey()))
                    found = true;
            if(!found)
                solicitudesViaje.get(viajeKey).add(solicitud);
        }
    }

    public void removeSolicitudesViaje(String viajeKey){
        solicitudesViaje.remove(viajeKey);
    }
}
