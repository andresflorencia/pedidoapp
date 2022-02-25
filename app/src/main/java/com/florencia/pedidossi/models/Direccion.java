package com.florencia.pedidossi.models;

public class Direccion {
    public Integer iddireccion, personaid, parroquiaid;
    public String parroquia, ciudad, provincia, calle_principal, calle_secundaria, referencia;
    public Double lat, lon;
    public boolean favorita;

    public Direccion(String ciudad, String provincia, String calle_principal, String calle_secundaria, boolean favorita){
        this.ciudad = ciudad;
        this.provincia = provincia;
        this.calle_principal = calle_principal;
        this.calle_secundaria = calle_secundaria;
        this.favorita = favorita;
    }

    public Direccion(){
        iddireccion = personaid = parroquiaid = 0;
        ciudad = provincia = calle_principal = calle_secundaria = referencia = parroquia = "";
        lat = lon = 0d;
        favorita = false;
    }

    @Override
    public String toString() {
        String retorno = this.calle_principal;
        if(!this.calle_secundaria.equals(""))
            retorno += " y " + this.calle_secundaria;
        if(!this.parroquia.equals(""))
            retorno += ", " + this.parroquia;
        if(!this.ciudad.equals(""))
            retorno += ", " + this.ciudad;
        if(!this.provincia.equals(""))
            retorno += ", " + this.provincia;
        return retorno;
    }
}
