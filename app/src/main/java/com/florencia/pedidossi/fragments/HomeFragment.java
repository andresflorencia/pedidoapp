package com.florencia.pedidossi.fragments;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.provider.ImageHeaderParserRegistry;
import com.florencia.pedidossi.MainActivity;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.activities.HomeActivity;
import com.florencia.pedidossi.adapters.ProductoAdapter;
import com.florencia.pedidossi.interfaces.IProducto;
import com.florencia.pedidossi.models.Empresa;
import com.florencia.pedidossi.models.Producto;
import com.florencia.pedidossi.models.ProductoImage;
import com.florencia.pedidossi.services.GPSTracker;
import com.florencia.pedidossi.utils.Constants;
import com.florencia.pedidossi.utils.Global;
import com.florencia.pedidossi.utils.LoadingDialog;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    View view;
    RecyclerView rvProductos;
    List<Producto> productos = new ArrayList<>();

    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        rvProductos = view.findViewById(R.id.rvProductos);

        init();

        return view;
    }

    private void init() {
        try{
            loading = new LoadingDialog(getActivity());
            initRetrofit();
            if(Global.empresa != null)
                cargarProductos(Global.empresa.rucempresa, "");
        }catch (Exception e){
            Log.d("TAG", e.getMessage());
        }
    }

    private void initRetrofit() {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URLBASE)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }

    public void cargarProductos(String rucempresa, String filter) {
        loading.start();
        Global.gpsTracker.getLastKnownLocation();
        Map<String, Object> data = new HashMap<>();
        data.put("rucempresa", rucempresa);
        data.put("idproducto", 0);
        data.put("latitude", Global.gpsTracker.getLatitude());
        data.put("longitude", Global.gpsTracker.getLongitude());
        data.put("filter", filter);

        IProducto iProducto = retrofit.create(IProducto.class);
        Call<JsonObject> call = iProducto.getProductos(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(getActivity(), "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if(response.body() != null){
                        productos.clear();
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean()) {
                            MyToast.show(getActivity(), obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        }else{
                            JsonObject data = obj.get("data").getAsJsonObject();
                            JsonArray array = data.get("productos").getAsJsonArray();
                            Producto producto;
                            if(array != null){
                                for (JsonElement element:array){
                                    JsonObject oProducto = element.getAsJsonObject();
                                    producto = new Producto();
                                    producto.codigo = oProducto.get("codigoproducto").getAsString();
                                    producto.nombreproducto = oProducto.get("nombreproducto").getAsString();
                                    producto.descripcion = oProducto.get("detalleproducto").getAsString();
                                    producto.descripcion_corta = oProducto.get("detalleproducto").getAsString();
                                    producto.precio = oProducto.get("pvpref").getAsDouble();
                                    producto.marca = oProducto.get("marca").getAsString();
                                    producto.categoria = oProducto.get("categoria").getAsString();
                                    producto.unidad = oProducto.get("unidad").getAsString();
                                    producto.image_default = oProducto.get("image_default").getAsString();
                                    producto.idproducto = oProducto.get("idproducto").getAsInt();
                                    producto.rucempresa = oProducto.get("rucempresa").getAsString();
                                    producto.abreunidad = oProducto.get("abreunidad").getAsString();
                                    producto.iva = oProducto.get("iva").getAsInt();

                                    if(oProducto.get("images").isJsonArray()){
                                        JsonArray aImage = oProducto.getAsJsonArray("images");
                                        if(aImage != null){
                                            producto.images = new ArrayList<>();
                                            ProductoImage imagen;
                                            for (JsonElement el:aImage){
                                                if(el.isJsonNull())
                                                    continue;
                                                imagen = new ProductoImage();
                                                JsonObject oImage = el.getAsJsonObject();
                                                imagen.url = oImage.get("url").getAsString();
                                                imagen.favorite = oImage.get("favorite").getAsBoolean();
                                                imagen.productoid = oImage.get("productoid").getAsInt();
                                                imagen.linea = oImage.get("linea").getAsInt();
                                                producto.images.add(imagen);
                                            }
                                        }
                                    }
                                    productos.add(producto);
                                }
                                ProductoAdapter adapter = new ProductoAdapter((HomeActivity) getActivity(), productos);
                                rvProductos.setAdapter(adapter);
                            }else
                                Utils.showMessage(getActivity(), "El array es nulo");

                            JsonObject oSuc = data.get("sucursal").getAsJsonObject();
                            if(oSuc != null) {
                                Global.empresa.establecimiento.idestablecimiento = oSuc.get("id").getAsInt();
                                Global.empresa.establecimiento.nombreestablecimiento = oSuc.get("nombreestablecimiento").getAsString();
                                Global.empresa.establecimiento.direccion = oSuc.get("direccion").getAsString();
                                Global.empresa.establecimiento.distancia = oSuc.get("distancia").getAsDouble();
                            }
                        }
                    }else
                        Utils.showMessage(getActivity(), "El body es null");
                }catch (Exception ex){}
                loading.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("TAGACT", t.getMessage());
                loading.dismiss();
            }
        });
    }
}
