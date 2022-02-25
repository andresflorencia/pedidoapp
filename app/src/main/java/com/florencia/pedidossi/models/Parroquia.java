package com.florencia.pedidossi.models;

public class Parroquia {
    public Integer idparroquia, cantonid;
    public String nombreparroquia;

    public Parroquia() {
        this.idparroquia = this.cantonid = 0;
        this.nombreparroquia = "";
    }
    @Override
    public String toString(){
        return nombreparroquia.toUpperCase();
    }
}
