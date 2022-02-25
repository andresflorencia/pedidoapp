package com.florencia.pedidossi.models;

public class DetalleCarrito {
    public Producto producto;
    public Double precio, cantidad, total;

    public DetalleCarrito(Producto producto, Double precio, Double cantidad){
        this.producto = producto;
        this.precio = precio;
        this.cantidad = cantidad;
        this.total = precio * cantidad;
    }
    public DetalleCarrito(){
        this.producto = new Producto();
        this.precio = 0d;
        this.cantidad = 0d;
        this.total = 0d;
    }

    public Double Total(){
        this.total = this.cantidad * this.precio;
        return this.total;
    }
}
