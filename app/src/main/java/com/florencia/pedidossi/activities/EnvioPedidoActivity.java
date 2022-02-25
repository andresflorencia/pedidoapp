package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.florencia.pedidossi.R;
import com.florencia.pedidossi.interfaces.IUsuario;
import com.florencia.pedidossi.models.Direccion;
import com.florencia.pedidossi.models.Usuario;
import com.florencia.pedidossi.utils.Constants;
import com.florencia.pedidossi.utils.Global;
import com.florencia.pedidossi.utils.LoadingDialog;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EnvioPedidoActivity extends AppCompatActivity implements View.OnClickListener {
    TextView lblTitle, lblSubtitle, lblCantCarrito, lblResumen;
    EditText tvObservacion, tvFono;
    Button btnConfirm;
    Toolbar toolbar;
    ImageButton btnReturn;
    Spinner spDirecciones;

    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_envio_pedido);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init() {
        loading = new LoadingDialog(EnvioPedidoActivity.this);
        lblTitle = findViewById(R.id.toolbarTitle);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);
        lblCantCarrito = findViewById(R.id.toolbarIndicador);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnReturn = findViewById(R.id.toolbarReturn);
        spDirecciones = findViewById(R.id.sp_direccion);
        tvObservacion = findViewById(R.id.tv_observacion);
        tvFono = findViewById(R.id.tv_fono);
        lblResumen = findViewById(R.id.lblResumen);

        lblTitle.setText(R.string.confirm_pedido2);
        btnReturn.setVisibility(View.VISIBLE);

        btnReturn.setOnClickListener(this::onClick);
        btnConfirm.setOnClickListener(this::onClick);

        tvFono.setText(Global.usuario.fono);
        String resumen = "<strong>Cant. de ítems: </strong>" + Global.carrito.detalle.size() + "<br>"
                + "<strong>Total a pagar: </strong>" + Utils.FormatoMoneda(Global.carrito.Total(), 2);

        if (Global.empresa.establecimiento != null) {
            resumen += "<br><br>Se despachará desde la sucursal más cercana: <strong>"
                    + Global.empresa.establecimiento.nombreestablecimiento + " (a " + Global.empresa.establecimiento.GetDistance() + " aprox., según tu ubicación actual)</strong>";
        }
        Utils.FormatHtml(lblResumen, resumen);

        initRetrofit();
        LlenarComboDirecciones();
    }

    private void LlenarComboDirecciones() {
        Thread th = new Thread() {
            @Override
            public void run() {
                ArrayAdapter<Direccion> adapter = new ArrayAdapter<>(EnvioPedidoActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, Global.usuario.direcciones);
                runOnUiThread(() -> {
                    spDirecciones.setAdapter(adapter);
                    int pos = 0;
                    for (Direccion dir : Global.usuario.direcciones) {
                        if (dir.favorita) {
                            pos = Global.usuario.direcciones.indexOf(dir);
                            break;
                        }
                    }
                    spDirecciones.setSelection(pos, true);
                });
            }
        };
        th.start();
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

    private void ConfirmPedido() {
        Global.gpsTracker.getLastKnownLocation();

        Global.carrito.observacion = tvObservacion.getText().toString();
        Global.carrito.lat = Global.gpsTracker.getLatitude();
        Global.carrito.lon = Global.gpsTracker.getLongitude();

        Usuario user = new Usuario();
        user.idusuario = Global.usuario.idusuario;
        user.usuario = Global.usuario.usuario;
        user.email = Global.usuario.email;
        user.tiponip = Global.usuario.tiponip;
        user.nip = Global.usuario.nip;
        user.apellidos = Global.usuario.apellidos;
        user.nombres = Global.usuario.nombres;
        user.fono = Global.usuario.fono;
        user.fechanacimiento = Global.usuario.fechanacimiento;

        loading.start();
        IUsuario iUsuario = retrofit.create(IUsuario.class);
        Map<String, Object> data = new HashMap<>();
        data.put("cliente", user);
        data.put("porcentajeiva", Global.empresa.porcentajeiva);
        data.put("pedido", Global.carrito);
        data.put("rucempresa", Global.empresa.rucempresa);
        data.put("direccion", (Direccion) spDirecciones.getSelectedItem());
        data.put("phone", tvFono.getText().toString());
        Call<JsonObject> call = iUsuario.SavePedido(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(EnvioPedidoActivity.this, "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if (response.body() != null) {
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean())
                            MyToast.show(EnvioPedidoActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        else {
                            JsonObject oData = obj.get("data").getAsJsonObject();
                            if (oData != null) {
                                Global.carrito = null;
                                setResult(Activity.RESULT_OK);
                                if(data.get("phone") != "" && !data.get("phone").equals(Global.usuario.fono)) {
                                    Global.usuario.fono = data.get("phone").toString();
                                    Global.usuario.GuardarSesionLocal(EnvioPedidoActivity.this);
                                }
                                MyToast.show(EnvioPedidoActivity.this, "Se registró correctamente el pedido", MyToast.LONG, MyToast.SUCCESS);
                                onBackPressed();
                            } else
                                Utils.showMessage(EnvioPedidoActivity.this, "El usuario es null");
                        }
                    } else
                        Utils.showMessage(EnvioPedidoActivity.this, "el body es null");
                } catch (Exception ex) {
                    Log.d("TAGACTI", ex.getMessage());
                    MyToast.show(EnvioPedidoActivity.this, "try: " + ex.getMessage(), MyToast.LONG, MyToast.ERROR);
                }
                loading.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("TAGACT", t.getMessage());
                MyToast.show(EnvioPedidoActivity.this, "onF: " + t.getMessage(), MyToast.LONG, MyToast.ERROR);
                loading.dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConfirm:
                ConfirmPedido();
                break;
            case R.id.toolbarReturn:
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Global.carrito != null)
            lblCantCarrito.setText("" + Global.carrito.detalle.size());
        if (Global.empresa != null) {
            lblSubtitle.setText(Global.empresa.nombre);
            lblSubtitle.setVisibility(View.VISIBLE);
        }
    }
}
