package com.florencia.pedidossi.activities;

import androidx.annotation.Nullable;
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
import android.widget.TextView;

import com.florencia.pedidossi.MainActivity;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, TextWatcher{
    private static final int ACTIVITY_CODE = 100;
    Toolbar toolbar;
    TextView lblTitle, tvNewUser, tvForgotPassword, lblSubtitle, lblValidated;
    EditText tvEmail, tvPassword;
    ImageButton btnReturn;
    Button btnLogin;
    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init(){
        loading = new LoadingDialog(LoginActivity.this);
        lblTitle = findViewById(R.id.toolbarTitle);
        btnReturn = findViewById(R.id.toolbarReturn);
        tvNewUser = findViewById(R.id.lblNewUser);
        tvForgotPassword = findViewById(R.id.lblForgotPassword);
        btnLogin = findViewById(R.id.btnLogin);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);
        tvEmail = findViewById(R.id.tv_email);
        tvPassword = findViewById(R.id.tv_password);
        lblValidated = findViewById(R.id.lblValidated);

        lblTitle.setText(R.string.login);
        btnReturn.setOnClickListener(this::onClick);
        tvNewUser.setOnClickListener(this::onClick);
        btnLogin.setOnClickListener(this::onClick);
        tvForgotPassword.setOnClickListener(this::onClick);

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
        Intent i;
        switch (v.getId()){
            case R.id.toolbarReturn:
                finish();
                break;
            case R.id.lblNewUser:
                i = new Intent(v.getContext(), RegistroActivity.class);
                startActivityForResult(i, ACTIVITY_CODE);
                break;
            case R.id.btnLogin:
                if(ValidaDatos())
                    Login(tvEmail.getText().toString().trim(), tvPassword.getText().toString());
                break;
            case R.id.lblForgotPassword:
                i = new Intent(v.getContext(), ForgotPasswordActivity.class);
                startActivityForResult(i, ACTIVITY_CODE);
                break;
        }
    }

    private boolean ValidaDatos() {
        if(tvEmail.getText().toString().trim().equals("")){
            lblValidated.setText(R.string.email_empty);
            return false;
        }
        if(tvPassword.getText().toString().trim().equals("")){
            lblValidated.setText(R.string.password_empty);
            return false;
        }
        return true;
    }

    private void Login(String usuario, String password) {
        loading.start();
        IUsuario iUsuario = retrofit.create(IUsuario.class);
        Map<String, Object> data = new HashMap<>();
        data.put("usuario", usuario);
        data.put("password", password);
        Call<JsonObject> call = iUsuario.Login(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(LoginActivity.this, "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if (response.body() != null) {
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean())
                            MyToast.show(LoginActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        else {
                            JsonObject oUser = obj.get("data").getAsJsonObject();
                            if (oUser != null) {
                                Global.usuario = new Usuario();
                                Global.usuario.idusuario = oUser.get("idusuario").getAsInt();
                                Global.usuario.tiponip = oUser.get("tiponip").getAsString();
                                Global.usuario.nip = oUser.get("nip").getAsString();
                                Global.usuario.usuario = oUser.get("usuario").getAsString();
                                Global.usuario.email = oUser.get("email").getAsString();
                                Global.usuario.nombres = oUser.get("nombres").getAsString();
                                Global.usuario.apellidos = oUser.get("apellidos").getAsString();
                                //Global.usuario.fechanacimiento = oUser.get("fechanacimiento").getAsString();
                                Global.usuario.password = password;
                                Global.usuario.fono = oUser.get("fono").getAsString();

                                if(oUser.get("direcciones").isJsonArray()){
                                    JsonArray aDirec = oUser.getAsJsonArray("direcciones");
                                    Global.usuario.direcciones = new ArrayList<>();
                                    if(aDirec != null){
                                        Direccion miD;
                                        for (JsonElement el:aDirec){
                                            if(el.isJsonNull())
                                                continue;
                                            JsonObject oDirec = el.getAsJsonObject();
                                            miD = new Direccion();
                                            miD.iddireccion = oDirec.get("iddireccion").getAsInt();
                                            miD.personaid = oDirec.get("personaid").getAsInt();
                                            miD.provincia = oDirec.get("provincia").getAsString();
                                            miD.ciudad = oDirec.get("ciudad").getAsString();
                                            miD.calle_principal = oDirec.get("calle_principal").getAsString();
                                            miD.calle_secundaria = oDirec.get("calle_secundaria").getAsString();
                                            miD.referencia = oDirec.get("referencia").getAsString();
                                            miD.favorita = oDirec.get("favorita").getAsBoolean();
                                            miD.parroquia = oDirec.get("parroquia").getAsString();
                                            miD.parroquiaid = oDirec.get("parroquiaid").getAsInt();
                                            Global.usuario.direcciones.add(miD);
                                        }
                                    }else
                                        Utils.showMessage(LoginActivity.this, "El array direcciones es null");
                                }else
                                    Utils.showMessage(LoginActivity.this, "No es un array");

                                Global.usuario.GuardarSesionLocal(LoginActivity.this);
                                setResult(Activity.RESULT_OK);
                                onBackPressed();
                            } else
                                Utils.showMessage(LoginActivity.this, "El usuario es null");
                        }
                    } else
                        Utils.showMessage(LoginActivity.this, "el body es null");
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_CODE:
                    setResult(Activity.RESULT_OK);
                    onBackPressed();
                    break;
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        lblValidated.setText("");
    }
    @Override
    public void afterTextChanged(Editable editable) {}
}
