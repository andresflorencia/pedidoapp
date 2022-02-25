package com.florencia.pedidossi.models;

public class Canton {
    public Integer idcanton, provinciaid;
    public String nombrecanton;

    public Canton() {
        this.idcanton = 0;
        this.provinciaid = 0;
        this.nombrecanton = "";
    }
    @Override
    public String toString(){
        return nombrecanton.toUpperCase();
    }
}