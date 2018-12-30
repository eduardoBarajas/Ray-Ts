package com.barajasoft.raites.Entities;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class SolicitudViaje {
    private String key = UUID.randomUUID().toString().replaceAll("-","");
    private String keyViaje = "";
    private String keyPasajero = "";
    private String fechaSolicitud = "";
    private LatLng puntoDeParada = new LatLng();
    private String direccionDeParada = "";
    private int espaciosSolicitados = -1;
    private boolean aceptada = false;

    public SolicitudViaje() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyPasajero() {
        return keyPasajero;
    }

    public void setKeyPasajero(String keyPasajero) {
        this.keyPasajero = keyPasajero;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LatLng getPuntoDeParada() {
        return puntoDeParada;
    }

    public void setPuntoDeParada(LatLng puntoDeParada) {
        this.puntoDeParada = puntoDeParada;
    }

    public String getDireccionDeParada() {
        return direccionDeParada;
    }

    public void setDireccionDeParada(String direccionDeParada) {
        this.direccionDeParada = direccionDeParada;
    }

    public int getEspaciosSolicitados() {
        return espaciosSolicitados;
    }

    public void setEspaciosSolicitados(int espaciosSolicitados) {
        this.espaciosSolicitados = espaciosSolicitados;
    }

    public boolean isAceptada() {
        return aceptada;
    }

    public void setAceptada(boolean aceptada) {
        this.aceptada = aceptada;
    }

    public String getKeyViaje() {
        return keyViaje;
    }

    public void setKeyViaje(String keyViaje) {
        this.keyViaje = keyViaje;
    }
}
