package com.florencia.pedidossi.models;

public class ProductoImage{
    public Integer productoid, linea;
    public String url;
    public boolean favorite;

    public ProductoImage(){
        productoid = linea =0;
        url = "";
        favorite = false;
    }
}
