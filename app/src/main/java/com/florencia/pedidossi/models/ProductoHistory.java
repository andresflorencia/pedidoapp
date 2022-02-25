package com.florencia.pedidossi.models;

public class ProductoHistory {
    public Double cantidad, precio, total;
    public Integer iva;
    public String unidad, abreunidad, categoria, marca, codigo, nombreproducto, image;

    public ProductoHistory(){
        cantidad = precio = total = 0.d;
        iva = 0;
        unidad = abreunidad = categoria = marca = codigo = nombreproducto = image = "";
    }
}
