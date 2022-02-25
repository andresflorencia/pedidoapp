package com.florencia.pedidossi.models;

import com.florencia.pedidossi.utils.Utils;

public class Empresa {
    public String rucempresa, direccion, phone, email, nombre, urlimage;
    public boolean seleccionada;
    public Double porcentajeiva;
    public Establecimiento establecimiento;

    public Empresa(){
        this.rucempresa = direccion = phone = email = nombre = urlimage = "";
        this.seleccionada = false;
        this.porcentajeiva = 12d;
        this.establecimiento = new Establecimiento();
    }

    public class Establecimiento{
        public Integer idestablecimiento;
        public String nombreestablecimiento, direccion;
        public Double distancia;

        public Establecimiento(){
            idestablecimiento = 0;
            nombreestablecimiento = direccion = "";
            distancia = 0d;
        }

        public String GetDistance(){
            String retorno;
            if(distancia > 999)
                retorno = Utils.RoundDecimal(distancia/1000, 1).toString() + "Km";
            else
                retorno = (int)Double.parseDouble(Utils.RoundDecimal(distancia, 0).toString()) + "m";
            return retorno;
        }
    }
}
