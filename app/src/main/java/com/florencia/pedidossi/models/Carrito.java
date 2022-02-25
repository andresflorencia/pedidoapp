package com.florencia.pedidossi.models;

import com.florencia.pedidossi.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class Carrito {
    public String fechapedido, usuario, observacion, secuencial;
    public Double total, costo_envio, lat, lon;
    public Integer establecimientoid, parroquiaid;
    public List<DetalleCarrito> detalle;

    public Carrito() {
        this.fechapedido = Utils.getDateFormat("yyyy-MM-dd");
        this.usuario = "";
        this.establecimientoid = 0;
        this.detalle = new ArrayList<>();
        this.total = 0d;
        this.costo_envio = 0d;
        this.observacion = secuencial = "";
        this.parroquiaid = 0;
    }

    public Double SubTotal() {
        this.total = 0d;
        for (DetalleCarrito item : detalle) {
            this.total += item.Total();
        }
        return this.total;
    }

    public Double Total() {
        SubTotal();
        this.total += this.costo_envio;
        return this.total;
    }
}
