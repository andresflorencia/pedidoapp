package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.florencia.pedidossi.MainActivity;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.adapters.HistoryAdapter;
import com.florencia.pedidossi.interfaces.IUsuario;
import com.florencia.pedidossi.models.PedidoHistory;
import com.florencia.pedidossi.models.ProductoHistory;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HistoryActivity extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    RecyclerView rvHistory;
    TextView toolbarTitle;
    ImageButton toolbarReturn;

    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;
    List<PedidoHistory> pedidos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init() {
        loading = new LoadingDialog(HistoryActivity.this);
        rvHistory = findViewById(R.id.rvHistory);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarReturn = findViewById(R.id.toolbarReturn);

        toolbarTitle.setText(R.string.history_pedido);
        toolbarReturn.setVisibility(View.VISIBLE);

        toolbarReturn.setOnClickListener(this::onClick);
        initRetrofit();
        CargarDatos();
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

    private void CargarDatos() {
        if(Global.usuario == null)
            return;

        loading.start();

        IUsuario iUsuario= retrofit.create(IUsuario.class);
        Call<JsonObject> call = iUsuario.HistoryPedido(Global.usuario.idusuario);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(HistoryActivity.this, "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if(response.body() != null){
                        pedidos.clear();
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean()) {
                            MyToast.show(HistoryActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        }else{
                            JsonArray data = obj.get("data").getAsJsonArray();
                            PedidoHistory pedido;
                            if(data != null){
                                for (JsonElement element:data){
                                    JsonObject oPedido = element.getAsJsonObject();
                                    pedido = new PedidoHistory();

                                    pedido.rucempresa = oPedido.get("rucempresa").getAsString();
                                    pedido.nombreempresa = oPedido.get("nombreempresa").getAsString();
                                    pedido.aliasempresa = oPedido.get("alias").getAsString();
                                    pedido.direccionempresa = oPedido.get("direccionempresa").getAsString();
                                    pedido.emailempresa = oPedido.get("emailempresa").getAsString();
                                    pedido.fonoempresa = oPedido.get("fonoempresa").getAsString();
                                    pedido.idhistory = oPedido.get("idhistorial").getAsInt();
                                    JsonObject oDetalle = oPedido.get("pedidojson").getAsJsonObject();
                                    pedido.fechapedido = oDetalle.get("fechapedido").getAsString();
                                    pedido.observacion = oDetalle.get("observacion").getAsString();
                                    pedido.porcentajeiva = oDetalle.get("porcentajeiva").getAsDouble();
                                    pedido.total = oDetalle.get("total").getAsDouble();

                                    JsonObject oDireccion = oDetalle.get("direccion").getAsJsonObject();
                                    pedido.direccion.calle_principal = oDireccion.get("calle_principal").getAsString();
                                    pedido.direccion.calle_secundaria = oDireccion.get("calle_secundaria").getAsString();
                                    pedido.direccion.ciudad = oDireccion.get("ciudad").getAsString();
                                    pedido.direccion.parroquia = oDireccion.get("parroquia").getAsString();
                                    pedido.direccion.referencia = oDireccion.get("referencia").getAsString();


                                    if(oDetalle.get("detalle").isJsonArray()){
                                        JsonArray aProductos = oDetalle.get("detalle").getAsJsonArray();
                                        ProductoHistory producto;
                                        for(JsonElement ePro:aProductos){
                                            JsonObject oPro = ePro.getAsJsonObject();
                                            producto = new ProductoHistory();
                                            producto.cantidad = oPro.get("cantidad").getAsDouble();
                                            producto.precio = oPro.get("precio").getAsDouble();
                                            producto.total = oPro.get("total").getAsDouble();
                                            JsonObject oDescPro = oPro.get("producto").getAsJsonObject();
                                            producto.abreunidad = oDescPro.get("abreunidad").getAsString();
                                            producto.unidad = oDescPro.get("unidad").getAsString();
                                            producto.categoria = oDescPro.get("categoria").getAsString();
                                            producto.codigo = oDescPro.get("codigo").getAsString();
                                            producto.image = oDescPro.get("image_default").getAsString();
                                            producto.iva = oDescPro.get("iva").getAsInt();
                                            producto.marca = oDescPro.get("marca").getAsString();
                                            producto.nombreproducto = oDescPro.get("nombreproducto").getAsString();

                                            if(oDescPro.get("images").isJsonArray()){
                                                JsonArray aImage = oDescPro.getAsJsonArray("images");
                                                if(aImage != null){
                                                    for (JsonElement el:aImage){
                                                        if(el.isJsonNull())
                                                            continue;
                                                        JsonObject oImage = el.getAsJsonObject();
                                                        if(!oImage.get("url").getAsString().equals(""))
                                                            producto.image = oImage.get("url").getAsString();
                                                    }
                                                }
                                            }
                                            pedido.detalle.add(producto);
                                        }
                                    }
                                    pedidos.add(pedido);
                                }
                                HistoryAdapter adapter = new HistoryAdapter(HistoryActivity.this, pedidos);
                                rvHistory.setAdapter(adapter);
                            }else
                                Utils.showMessage(HistoryActivity.this, "El array es nulo");
                        }
                    }else
                        Utils.showMessage(HistoryActivity.this, "El body es null");
                }catch (Exception ex){
                    Log.d("TAGACT", ex.getMessage());
                }
                loading.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("TAGACT", "onFailure: " + t.getMessage());
                loading.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.toolbarReturn:
                onBackPressed();
                return;
        }
    }
}
