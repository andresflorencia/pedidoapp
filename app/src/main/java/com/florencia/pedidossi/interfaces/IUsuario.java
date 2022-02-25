package com.florencia.pedidossi.interfaces;

import com.florencia.pedidossi.models.Direccion;
import com.florencia.pedidossi.models.Usuario;
import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IUsuario {
    //@FormUrlEncoded
    @POST("login")
    Call<JsonObject> Login(@Body Map<String, Object> data);

    @POST("saveuser")
    Call<JsonObject> SaveUser(@Body Usuario user);

    @POST("savedirection")
    Call<JsonObject> SaveDirection(@Body Direccion direccion);

    @FormUrlEncoded
    @POST("setdirectionfavorite")
    Call<JsonObject> setDirectionFavorite(@Field("iddireccion") Integer iddireccion);

    @FormUrlEncoded
    @POST("deletedirection")
    Call<JsonObject> DeleteDirection(@Field("iddireccion") Integer iddireccion);

    @POST("savepedido")
    Call<JsonObject> SavePedido(@Body Map<String, Object> data);

    @FormUrlEncoded
    @POST("gethistorypedido")
    Call<JsonObject> HistoryPedido(@Field("idusuario") Integer idusuario);
}
