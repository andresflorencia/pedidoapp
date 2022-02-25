package com.florencia.pedidossi.models;

public class Provincia {
    public Integer idprovincia;
    public String nombreprovincia;

    public Provincia() {
        this.idprovincia = 0;
        this.nombreprovincia = "";
    }

    @Override
    public String toString(){
        return nombreprovincia.toUpperCase();
    }
}
