package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.florencia.pedidossi.R;
import com.florencia.pedidossi.interfaces.IUsuario;
import com.florencia.pedidossi.models.Canton;
import com.florencia.pedidossi.models.Direccion;
import com.florencia.pedidossi.models.Parroquia;
import com.florencia.pedidossi.models.Provincia;
import com.florencia.pedidossi.repository.CantonRepository;
import com.florencia.pedidossi.repository.ParroquiaRepository;
import com.florencia.pedidossi.repository.ProvinciaRepository;
import com.florencia.pedidossi.utils.Constants;
import com.florencia.pedidossi.utils.Global;
import com.florencia.pedidossi.utils.LoadingDialog;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class DirectionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TAGDIRECCION";
    Toolbar toolbar;
    TextView lblTitle, lblSubtitle;
    ImageButton btnReturn;
    Button btnSave;
    EditText tvCalleP, tvCalleS, tvReferencia;
    Spinner cbProvincia, cbCanton, cbParroquia;
    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;
    Integer position = -1;
    boolean band = false;
    Provincia provActual = new Provincia();
    Canton canActual = new Canton();
    List<Provincia> provincias = new ArrayList<>();
    List<Canton> cantones = new ArrayList<>();
    List<Parroquia> parroquias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init() {
        loading = new LoadingDialog(DirectionActivity.this);
        cbProvincia = findViewById(R.id.cbProvincia);
        cbCanton = findViewById(R.id.cbCanton);
        cbParroquia = findViewById(R.id.cbParroquia);
        tvCalleP = findViewById(R.id.tv_calleprincipal);
        tvCalleS = findViewById(R.id.tv_callesecundaria);
        tvReferencia = findViewById(R.id.tv_referencia);
        btnReturn = findViewById(R.id.toolbarReturn);
        btnSave = findViewById(R.id.btnSave);
        lblTitle = findViewById(R.id.toolbarTitle);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);

        btnReturn.setOnClickListener(this::onClick);
        btnSave.setOnClickListener(this::onClick);

        initRetrofit();

        LlenarComboProvincias(0);
        LlenarComboCantones(0, -1);
        LlenarComboParroquias(0, -1);

        cbProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (cbProvincia.getAdapter() != null) {
                    Provincia provincia = ((Provincia) cbProvincia.getItemAtPosition(position));
                    if (provincia.idprovincia != provActual.idprovincia)
                        band = false;
                    if (!band)
                        LlenarComboCantones(provincia.idprovincia, -1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cbCanton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (cbCanton.getAdapter() != null) {
                    Canton canton = ((Canton) cbCanton.getItemAtPosition(position));
                    if (!band)
                        LlenarComboParroquias(canton.idcanton, -1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lblTitle.setText("Nueva dirección");
        if (getIntent().getExtras() != null) {
            position = getIntent().getExtras().getInt("position", -1);
            if(position>=0)
                CargaDireccion(position);
        }
    }

    private void CargaDireccion(Integer pos) {
        lblTitle.setText("Editar dirección");
        tvCalleP.setText(Global.usuario.direcciones.get(pos).calle_principal);
        tvCalleS.setText(Global.usuario.direcciones.get(pos).calle_secundaria);
        tvReferencia.setText(Global.usuario.direcciones.get(pos).referencia);
        band = true;
        Parroquia miparroquia = ParroquiaRepository.get(Global.usuario.direcciones.get(pos).parroquiaid);
        canActual = CantonRepository.get(miparroquia.cantonid);
        provActual = ProvinciaRepository.get(canActual.provinciaid);

        for (Provincia miP : provincias) {
            if (provActual.idprovincia.equals(miP.idprovincia)) {
                cbProvincia.setSelection(provincias.indexOf(miP));
                break;
            }
        }
        LlenarComboCantones(provActual.idprovincia, canActual.idcanton);
        LlenarComboParroquias(canActual.idcanton, miparroquia.idparroquia);
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

    private void SaveDirection(Direccion newD) {
        loading.start();
        IUsuario iUsuario = retrofit.create(IUsuario.class);
        Call<JsonObject> call = iUsuario.SaveDirection(newD);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(DirectionActivity.this, "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if (response.body() != null) {
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean())
                            MyToast.show(DirectionActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        else {
                            Integer id = obj.get("data").getAsInt();
                            if (id != null && id > 0) {
                                if (newD.iddireccion == 0) {
                                    newD.iddireccion = id;
                                    Global.usuario.direcciones.add(newD);
                                }
                                Global.usuario.GuardarSesionLocal(DirectionActivity.this);
                                setResult(RESULT_OK);
                                onBackPressed();
                            } else
                                Utils.showMessage(DirectionActivity.this, "El usuario es null");
                        }
                    } else
                        Utils.showMessage(DirectionActivity.this, "el body es null");
                } catch (Exception ex) {
                    Log.d("TAGACTI", ex.getMessage());
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

    private void LlenarComboProvincias(Integer idprovincia) {
        try {
            provincias = ProvinciaRepository.getList();
            ArrayAdapter<Provincia> adapterProvincia = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provincias);
            adapterProvincia.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbProvincia.setAdapter(adapterProvincia);
            int position = 0;
            for (int i = 0; i < provincias.size(); i++) {
                if (provincias.get(i).idprovincia == idprovincia) {
                    position = i;
                    break;
                }
            }
            if (idprovincia >= 0)
                cbProvincia.setSelection(position, true);
        } catch (Exception e) {
            Log.d(TAG, "LlenarComboProvincias(): " + e.getMessage());
        }
    }

    private void LlenarComboCantones(Integer idprovincia, Integer idcanton) {
        try {
            cantones.clear();
            cantones = CantonRepository.getList(idprovincia);
            ArrayAdapter<Canton> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cantones);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbCanton.setAdapter(adapter);
            int position = 0;
            for (int i = 0; i < cantones.size(); i++) {
                if (cantones.get(i).idcanton.equals(idcanton)) {
                    position = i;
                    break;
                }
            }
            if (idcanton >= 0)
                cbCanton.setSelection(position, true);
        } catch (Exception e) {
            Log.d(TAG, "LlenarComboCantones(): " + e.getMessage());
        }
    }

    private void LlenarComboParroquias(Integer idcanton, Integer idparroquia) {
        try {
            parroquias.clear();
            parroquias = ParroquiaRepository.getList(idcanton);
            ArrayAdapter<Parroquia> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parroquias);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cbParroquia.setAdapter(adapter);
            int position = 0;
            for (int i = 0; i < parroquias.size(); i++) {
                if (parroquias.get(i).idparroquia.equals(idparroquia)) {
                    position = i;
                    break;
                }
            }
            if (idparroquia >= 0) {
                cbParroquia.setSelection(position, true);
                //band = false;
            }
        } catch (Exception e) {
            Log.d(TAG, "LlenarComboProvincias(): " + e.getMessage());
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarReturn:
                onBackPressed();
                break;
            case R.id.btnSave:
                Direccion newD;
                if (position >= 0)
                    newD = Global.usuario.direcciones.get(position);
                else
                    newD = new Direccion();

                newD.personaid = Global.usuario.idusuario;
                newD.provincia = ((Provincia)cbProvincia.getSelectedItem()).nombreprovincia;
                newD.ciudad = ((Canton)cbCanton.getSelectedItem()).nombrecanton;
                newD.parroquia = ((Parroquia)cbParroquia.getSelectedItem()).nombreparroquia;
                newD.parroquiaid = ((Parroquia)cbParroquia.getSelectedItem()).idparroquia;
                newD.calle_principal = tvCalleP.getText().toString();
                newD.calle_secundaria = tvCalleS.getText().toString();
                newD.referencia = tvReferencia.getText().toString();
                SaveDirection(newD);
                break;
        }
    }
}
