package com.florencia.pedidossi.interfaces;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IEmpresa {
    @FormUrlEncoded
    @POST("getempresas")
    Call<JsonObject> getEmpresas(@Field("ruc") String ruc);

    @POST("getprovincia_canton")
    Call<JsonObject> getProvinciaCanton();
}
