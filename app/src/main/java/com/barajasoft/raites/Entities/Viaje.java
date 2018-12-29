package com.barajasoft.raites.Entities;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Viaje {
    private String key = UUID.randomUUID().toString().replaceAll("-","");
    private String keyConductor = "";
    private int espaciosDisponibles = -1;
    private List<String> keysPasajeros = new LinkedList<>();
    private String fechaPublicacion = "";
    private String fechaViaje = "";
    private String horaViaje = "";
    private List<LatLng> puntosDeViaje = new LinkedList<>();
    private List<LatLng> puntosDeParada = new LinkedList<>();
    private String direccionSalida = "";
    private String direccionDestino = "";
    public Viaje(){}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyConductor() {
        return keyConductor;
    }

    public void setKeyConductor(String keyConductor) {
        this.keyConductor = keyConductor;
    }

    public int getEspaciosDisponibles() {
        return espaciosDisponibles;
    }

    public void setEspaciosDisponibles(int espaciosDisponibles) {
        this.espaciosDisponibles = espaciosDisponibles;
    }

    public List<String> getKeysPasajeros() {
        return keysPasajeros;
    }

    public void setKeysPasajeros(List<String> keysPasajeros) {
        this.keysPasajeros = keysPasajeros;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getFechaViaje() {
        return fechaViaje;
    }

    public void setFechaViaje(String fechaViaje) {
        this.fechaViaje = fechaViaje;
    }

    public List<LatLng> getPuntosDeViaje() {
        return puntosDeViaje;
    }

    public void setPuntosDeViaje(List<LatLng> puntosDeViaje) {
        this.puntosDeViaje = puntosDeViaje;
    }

    public String getDireccionSalida() {
        return direccionSalida;
    }

    public void setDireccionSalida(String direccionSalida) {
        this.direccionSalida = direccionSalida;
    }

    public String getDireccionDestino() {
        return direccionDestino;
    }

    public void setDireccionDestino(String direccionDestino) {
        this.direccionDestino = direccionDestino;
    }

    public String getHoraViaje() {
        return horaViaje;
    }

    public void setHoraViaje(String horaViaje) {
        this.horaViaje = horaViaje;
    }

    public List<LatLng> getPuntosDeParada() {
        return puntosDeParada;
    }

    public void setPuntosDeParada(List<LatLng> puntosDeParada) {
        this.puntosDeParada = puntosDeParada;
    }
}
