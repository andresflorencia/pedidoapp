package com.florencia.pedidossi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.florencia.pedidossi.activities.HomeActivity;
import com.florencia.pedidossi.adapters.EmpresaAdapter;
import com.florencia.pedidossi.interfaces.IEmpresa;
import com.florencia.pedidossi.models.Canton;
import com.florencia.pedidossi.models.Empresa;
import com.florencia.pedidossi.models.Parroquia;
import com.florencia.pedidossi.models.Provincia;
import com.florencia.pedidossi.models.Usuario;
import com.florencia.pedidossi.repository.CantonRepository;
import com.florencia.pedidossi.repository.ParroquiaRepository;
import com.florencia.pedidossi.repository.ProvinciaRepository;
import com.florencia.pedidossi.services.GPSTracker;
import com.florencia.pedidossi.services.SQLite;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sPreferencesSesion;
    RecyclerView rvEmpresas;
    public Button btnContinuar;
    List<Empresa> empresas = new ArrayList<>();
    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        SQLite.sqlDB = new SQLite(getApplicationContext());
        SQLite.createdb(MainActivity.this);
        rvEmpresas = findViewById(R.id.rvEmpresas);
        btnContinuar = findViewById(R.id.btnContinuar);
        btnContinuar.setOnClickListener(this);
        loading = new LoadingDialog(MainActivity.this);

        Utils.verificarPermisos(MainActivity.this);

        if (Global.gpsTracker == null)
            Global.gpsTracker = new GPSTracker(this);
        if (!Global.gpsTracker.checkGPSEnabled())
            Global.gpsTracker.showSettingsAlert(MainActivity.this);

        initRetrofit();

        sPreferencesSesion = getSharedPreferences("DatosSesion", MODE_PRIVATE);
        if (sPreferencesSesion != null) {
            String json = sPreferencesSesion.getString("user", "");
            if (!json.equals("")) {
                Global.usuario = gson.fromJson(json, Usuario.class);
            }
        }

        cargaEmpresas();

        Thread th = new Thread() {
            @Override
            public void run() {
                runOnUiThread(() -> cargaProvincias());
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

    private void cargaEmpresas() {
        loading.start();
        IEmpresa iEmpresa = retrofit.create(IEmpresa.class);
        Call<JsonObject> call = iEmpresa.getEmpresas("");
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(MainActivity.this, "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if (response.body() != null) {
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean()) {
                            MyToast.show(MainActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        } else {
                            JsonArray arrayEmpresas = obj.get("data").getAsJsonArray();
                            Empresa empresa;
                            if (arrayEmpresas != null) {
                                for (JsonElement element : arrayEmpresas) {
                                    JsonObject oEmpresa = element.getAsJsonObject();
                                    empresa = new Empresa();
                                    empresa.rucempresa = oEmpresa.get("ruc").getAsString();
                                    empresa.nombre = oEmpresa.get("razonsocial").getAsString();
                                    empresa.direccion = oEmpresa.get("direccion").getAsString();
                                    empresa.phone = oEmpresa.get("fono").getAsString();
                                    empresa.email = oEmpresa.get("email").getAsString();
                                    empresa.urlimage = oEmpresa.get("image").getAsString();
                                    empresa.porcentajeiva = oEmpresa.get("iva").getAsDouble();
                                    empresas.add(empresa);
                                }
                                EmpresaAdapter adapter = new EmpresaAdapter(MainActivity.this, empresas);
                                rvEmpresas.setAdapter(adapter);
                            }
                        }
                    }
                } catch (Exception ex) {
                }
                loading.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("TAGACT", t.getMessage());
                loading.dismiss();
            }
        });
    }

    private void cargaProvincias() {
        if (ProvinciaRepository.exists() == 0 || CantonRepository.exists() == 0 || ParroquiaRepository.exists() == 0) {

            IEmpresa iEmpresa = retrofit.create(IEmpresa.class);
            Call<JsonObject> call = iEmpresa.getProvinciaCanton();
            call.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                    if (!response.isSuccessful()) {
                        Utils.showMessage(MainActivity.this, "Error: " + response.code());
                        return;
                    }
                    try {
                        if (response.body() != null) {
                            JsonObject obj = response.body();
                            if (obj.get("error").getAsBoolean()) {
                                MyToast.show(MainActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                            } else {
                                JsonObject array = obj.get("data").getAsJsonObject();
                                List<Provincia> provincias = new ArrayList<>();
                                List<Canton> cantones = new ArrayList<>();
                                List<Parroquia> parroquias = new ArrayList<>();
                                if (array != null) {
                                    JsonArray aPro = array.get("provincias").getAsJsonArray();
                                    Provincia pro;
                                    for (JsonElement ele : aPro) {
                                        JsonObject ob = ele.getAsJsonObject();
                                        pro = new Provincia();
                                        pro.idprovincia = ob.get("idprovincia").getAsInt();
                                        pro.nombreprovincia = ob.get("nombreprovincia").getAsString();
                                        provincias.add(pro);
                                    }
                                    JsonArray aCan = array.get("cantones").getAsJsonArray();
                                    Canton can;
                                    for (JsonElement ele : aCan) {
                                        JsonObject ob = ele.getAsJsonObject();
                                        can = new Canton();
                                        can.idcanton = ob.get("idcanton").getAsInt();
                                        can.nombrecanton = ob.get("nombrecanton").getAsString();
                                        can.provinciaid = ob.get("provinciaid").getAsInt();
                                        cantones.add(can);
                                    }
                                    JsonArray aParr = array.get("parroquias").getAsJsonArray();
                                    Parroquia parr;
                                    for (JsonElement ele : aParr) {
                                        JsonObject ob = ele.getAsJsonObject();
                                        parr = new Parroquia();
                                        parr.idparroquia = ob.get("idparroquia").getAsInt();
                                        parr.nombreparroquia = ob.get("nombreparroquia").getAsString();
                                        parr.cantonid = ob.get("cantonid").getAsInt();
                                        parroquias.add(parr);
                                    }

                                    if (provincias != null)
                                        ProvinciaRepository.SaveLista(provincias);
                                    if (cantones != null)
                                        CantonRepository.SaveLista(cantones);
                                    if (parroquias != null)
                                        ParroquiaRepository.SaveLista(parroquias);
                                } else
                                    MyToast.show(MainActivity.this, "El array es null", MyToast.LONG, MyToast.ERROR);
                            }
                        }
                    } catch (Exception ex) {
                        MyToast.show(MainActivity.this, "Error: " + ex.getLocalizedMessage(), MyToast.LONG, MyToast.ERROR);
                    }
                }

                @Override
                public void onFailure(Call<JsonObject> call, Throwable t) {
                    Log.d("TAGACT", t.getMessage());
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinuar:
                Empresa miEmp = new Empresa();
                for (Empresa emp : empresas) {
                    if (emp.seleccionada) {
                        miEmp = emp;
                        break;
                    }
                }
                Global.empresa = miEmp;
                Intent i = new Intent(view.getContext(), HomeActivity.class);
                startActivity(i);
                break;
        }
    }
}
