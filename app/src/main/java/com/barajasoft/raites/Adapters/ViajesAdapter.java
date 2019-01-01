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
    private SolicitudViaje solicitudActual;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference viajesReference = database.getReference("Viajes");
    private DatabaseReference usuariosReference = database.getReference("Usuarios");
    private DatabaseReference solicitudesReference = database.getReference("SolicitudesDeViaje");

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
        holder.txtHora.setText(viaje.getHoraViaje());
        holder.txtFecha.setText(viaje.getFechaViaje());
        holder.txtDestino.setText(viaje.getDireccionDestino());
        holder.txtSalida.setText(viaje.getDireccionSalida());
        if(pref.getString("key", null).equals(viaje.getKeyConductor())){
            holder.txtRol.setText("Conductor");
        }else{
            holder.txtRol.setText("Pasajero");
        }
        holder.btnSolicitudes.setVisibility(View.GONE);
        holder.verMas.setOnClickListener(e->{
            Intent intent = new Intent(context, ExpandViajeActivity.class);
            intent.putExtra("Destino", viaje.getDireccionDestino());
            intent.putExtra("Salida", viaje.getDireccionSalida());
            intent.putExtra("FechaSalida", viaje.getFechaViaje());
            intent.putExtra("HoraSalida", viaje.getHoraViaje());
            intent.putExtra("KeyConductor", viaje.getKeyConductor());
            intent.putExtra("KeyViaje", viaje.getKey());
            String[] pasajeros = new String[viaje.getKeysPasajeros().size()];
            for(int i = 0; i < pasajeros.length; i++)
                pasajeros[i] = viaje.getKeysPasajeros().get(i);
            intent.putExtra("PasajerosKeys", pasajeros);
            intent.putExtra("EspaciosDisponibles", viaje.getEspaciosDisponibles());
            context.startActivity(intent);
        });
        solicitudesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean found = false;
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    SolicitudViaje solicitudViaje = data.getValue(SolicitudViaje.class);
                    if(solicitudViaje.getKeyViaje().equals(viaje.getKey())){
                        solicitudActual = solicitudViaje;
                        if(solicitudViaje.getKeyPasajero().equals(pref.getString("key", null))){
                            holder.btnSolicitudes.setOnClickListener(e->{
                                EstadoSolicitudDialog dlg = new EstadoSolicitudDialog(context, solicitudActual);
                                dlg.show();
                            });
                        }
                        if(viaje.getKeyConductor().equals(pref.getString("key", null))){
                            holder.btnSolicitudes.setOnClickListener(e->{
                                Intent intent = new Intent(context, SolicitudesViajeActivity.class);
                                intent.putExtra("KeyViaje", viaje.getKey());
                                context.startActivity(intent);
                            });
                        }
                        found = true;
                    }
                }
                if(found){
                    holder.btnSolicitudes.setVisibility(View.VISIBLE);
                }else{
                    holder.btnSolicitudes.setVisibility(View.GONE);
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
        public TextView txtSalida, txtDestino, txtFecha, txtHora, txtRol;
        public Button verMas, btnSolicitudes;
        public ViajesViewHolder(View itemView) {
            super(itemView);
            txtSalida = itemView.findViewById(R.id.txtSalida);
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtHora = itemView.findViewById(R.id.txtHora);
            txtRol = itemView.findViewById(R.id.txtRol);
            verMas = itemView.findViewById(R.id.btnVerMas);
            btnSolicitudes = itemView.findViewById(R.id.btnSolicitud);
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

    public void modifyViaje(Viaje viaje){
        for(Viaje v : viajeList){
            if(v.getKey().equals(viaje.getKey())){
                viajeList.set(viajeList.indexOf(v), viaje);
                break;
            }
        }
    }

    public void clear(){
        viajeList.clear();
    }
}
