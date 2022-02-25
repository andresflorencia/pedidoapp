package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.florencia.pedidossi.R;
import com.florencia.pedidossi.interfaces.IUsuario;
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

public class RegistroActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher {
    Toolbar toolbar;
    TextView lblTitle, tvLogin, lblSubtitle, lblValidated;
    RadioButton rbCedula, rbRuc, rbPasaporte;
    EditText tvNip, tvNombres, tvApellidos, tvFono, tvEmail, tvPassword;
    ImageButton btnReturn;
    Button btnRegistrar;
    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init() {
        loading = new LoadingDialog(RegistroActivity.this);
        lblTitle = findViewById(R.id.toolbarTitle);
        btnReturn = findViewById(R.id.toolbarReturn);
        tvLogin = findViewById(R.id.lblLogin);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);
        rbCedula = findViewById(R.id.rbCedula);
        rbRuc = findViewById(R.id.rbRuc);
        rbPasaporte = findViewById(R.id.rbPasaporte);
        tvNip = findViewById(R.id.tv_nip);
        tvNombres = findViewById(R.id.tv_nombres);
        tvApellidos = findViewById(R.id.tv_apellidos);
        tvEmail = findViewById(R.id.tv_email);
        tvFono = findViewById(R.id.tv_fono);
        tvPassword = findViewById(R.id.tv_password);
        lblValidated = findViewById(R.id.lblValidated);

        lblTitle.setText(R.string.new_user);
        btnReturn.setOnClickListener(this::onClick);
        tvLogin.setOnClickListener(this::onClick);
        btnRegistrar.setOnClickListener(this::onClick);

        tvNip.addTextChangedListener(this);
        tvNombres.addTextChangedListener(this);
        tvApellidos.addTextChangedListener(this);
        tvEmail.addTextChangedListener(this);
        tvPassword.addTextChangedListener(this);

        initRetrofit();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarReturn:
            case R.id.lblLogin:
                finish();
                break;
            case R.id.btnRegistrar:
                if (ValidaDatos())
                    Registrar();
                break;
        }
    }

    private boolean ValidaDatos() {
        if (tvNip.getText().toString().trim().equals("")) {
            lblValidated.setText("Ingrese la identificación.");
            return false;
        }
        if (tvNombres.getText().toString().trim().equals("")) {
            lblValidated.setText("Ingrese los nombres.");
            return false;
        }
        if (tvApellidos.getText().toString().trim().equals("")) {
            lblValidated.setText("Ingrese los apellidos.");
            return false;
        }
        if (tvEmail.getText().toString().trim().equals("")) {
            lblValidated.setText("Ingrese el correo electrónico.");
            return false;
        }
        if (tvPassword.getText().toString().trim().equals("")) {
            lblValidated.setText("Especifique la contraseña.");
            return false;
        }
        return true;
    }

    private void Registrar() {
        Usuario user = new Usuario();
        if (rbCedula.isChecked())
            user.tiponip = "05";
        else if (rbRuc.isChecked())
            user.tiponip = "04";
        else if (rbPasaporte.isChecked())
            user.tiponip = "06";
        user.nip = tvNip.getText().toString().trim();
        user.nombres = tvNombres.getText().toString().trim();
        user.apellidos = tvApellidos.getText().toString().trim();
        user.fono = tvFono.getText().toString().trim();
        user.email = tvEmail.getText().toString().trim();
        user.usuario = user.email;
        user.password = tvPassword.getText().toString().trim();

        EnviarDatos(user);
    }

    private void EnviarDatos(Usuario usuario) {
        loading.start();
        IUsuario iUsuario = retrofit.create(IUsuario.class);
        Call<JsonObject> call = iUsuario.SaveUser(usuario);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(RegistroActivity.this, "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if (response.body() != null) {
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean())
                            MyToast.show(RegistroActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        else {
                            Integer id = obj.get("data").getAsInt();
                            if (id != null && id > 0) {
                                usuario.idusuario = id;
                                Global.usuario = usuario;
                                Global.usuario.GuardarSesionLocal(RegistroActivity.this);
                                setResult(Activity.RESULT_OK);
                                onBackPressed();
                            } else
                                Utils.showMessage(RegistroActivity.this, "El usuario es null");
                        }
                    } else
                        Utils.showMessage(RegistroActivity.this, "el body es null");
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

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        lblValidated.setText("");
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
