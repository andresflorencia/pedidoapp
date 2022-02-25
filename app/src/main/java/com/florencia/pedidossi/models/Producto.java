package com.florencia.pedidossi.models;

import com.florencia.pedidossi.utils.Global;

import java.util.List;

public class Producto {
    public String rucempresa, codigo, codigoaux, nombreproducto, descripcion_corta, descripcion, categoria, marca, unidad, image_default, abreunidad;
    public Double precio, stock;
    public Integer idproducto, iva;
    public List<ProductoImage> images;

    public Producto(String codigo, String nombreproducto, String descripcion_corta, String categoria, Double precio) {
        this.codigo = codigo;
        this.nombreproducto = nombreproducto;
        this.descripcion_corta = descripcion_corta;
        this.categoria = categoria;
        this.precio = precio;
    }

    public Producto() {
        rucempresa = codigo = codigoaux = nombreproducto = descripcion = descripcion_corta = categoria = marca = unidad = image_default = abreunidad = "";
        idproducto = iva = 0;
        precio = stock = 0d;
    }

    public Double Precio() {
        return this.precio + (this.iva == 1 ? this.precio * Global.empresa.porcentajeiva / 100 : 0d);
    }
}
