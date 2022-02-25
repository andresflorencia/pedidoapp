package com.florencia.pedidossi.models;

import java.util.ArrayList;
import java.util.List;

public class PedidoHistory {
    public String nombreempresa, rucempresa, direccionempresa, emailempresa, aliasempresa, fonoempresa;
    public String fechapedido, observacion;
    public Direccion direccion;
    public Double total, porcentajeiva;
    public Integer idhistory;
    public List<ProductoHistory> detalle;

    public PedidoHistory() {
        idhistory = 0;
        total = porcentajeiva = 0d;
        nombreempresa = rucempresa = direccionempresa = emailempresa = aliasempresa =
                fechapedido = observacion = fonoempresa = "";
        direccion = new Direccion();
        detalle = new ArrayList<>();
    }
}
