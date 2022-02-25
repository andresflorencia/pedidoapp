package com.florencia.pedidossi.interfaces;

import com.google.gson.JsonObject;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IProducto {
    @POST("getproductos")
    Call<JsonObject> getProductos(@Body Map<String, Object> datos);
}
