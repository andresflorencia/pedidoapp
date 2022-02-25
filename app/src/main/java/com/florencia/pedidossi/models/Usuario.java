package com.florencia.pedidossi.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.florencia.pedidossi.utils.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Usuario {
    public Integer idusuario;
    public String usuario, email, password, tiponip, nip, apellidos, nombres,
            fono, fechanacimiento, fecharegistro;
    public boolean recibealerta;
    public List<Direccion> direcciones;

    public Usuario(){
        this.idusuario = 0;
        this.usuario = this.email = this.password = this.tiponip = this.nip = this.apellidos
                = this.nombres = this.fono = this.fechanacimiento = this.fecharegistro = "";
        this.recibealerta = true;
        this.direcciones = new ArrayList<>();
    }

    public boolean GuardarSesionLocal(Context context) {
        //Crea preferencia
        SharedPreferences sharedPreferences = context.getSharedPreferences("DatosSesion", MODE_PRIVATE);
        String conexionactual = sharedPreferences.getString("conexionactual", "");
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .putString("user", new Gson().toJson(this, Usuario.class))
                .putString("ultimaconexion", conexionactual)
                .putString("conexionactual", Utils.getDateFormat("dd MMM yyyy HH:mm"));
        return editor.commit();
    }

    public static boolean CerrarSesionLocal(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("DatosSesion", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit()
                .clear();
        return editor.commit();
    }

    @Override
    public String toString(){
        return nombres + " " + apellidos;
    }
}
