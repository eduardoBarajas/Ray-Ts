package com.barajasoft.raites.Entities;

import java.util.UUID;

public class User {
    private String key = UUID.randomUUID().toString().replaceAll("-","");
    private String nombre = "";
    private String telefono = "";
    private int edad = 0;
    private String sexo = "";
    private String correo = "";
    private boolean validadoPasajero = false;
    private boolean validadoConductor = false;
    private String imagenPerfil = "";
    private float rating = 0;
    public User(){}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isValidadoPasajero() {
        return validadoPasajero;
    }

    public void setValidadoPasajero(boolean validadoPasajero) {
        this.validadoPasajero = validadoPasajero;
    }

    public boolean isValidadoConductor() {
        return validadoConductor;
    }

    public void setValidadoConductor(boolean validadoConductor) {
        this.validadoConductor = validadoConductor;
    }

    public String getImagenPerfil() {
        return imagenPerfil;
    }

    public void setImagenPerfil(String imagenPerfil) {
        this.imagenPerfil = imagenPerfil;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
