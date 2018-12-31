package com.barajasoft.raites.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.barajasoft.raites.Entities.SolicitudViaje;
import com.barajasoft.raites.R;

public class EstadoSolicitudDialog extends Dialog{
    public EstadoSolicitudDialog(@NonNull Context context, SolicitudViaje solicitud) {
        super(context);
        setContentView(R.layout.estado_solicitud_dialog);
        TextView fecha, destino, espacios, estadoActual;
        fecha = findViewById(R.id.txtFecha);
        destino = findViewById(R.id.txtDestino);
        espacios = findViewById(R.id.txtEspacios);
        estadoActual = findViewById(R.id.lblSituacion);
        fecha.setText(solicitud.getFechaSolicitud());
        destino.setText(solicitud.getDireccionDeParada());
        espacios.setText(String.valueOf(solicitud.getEspaciosSolicitados()));
        if(solicitud.isAceptada())
            estadoActual.setText("Aceptada");
        else
            estadoActual.setText("Pendiente");
    }
}
