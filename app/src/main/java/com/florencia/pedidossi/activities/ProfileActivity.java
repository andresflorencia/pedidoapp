package com.florencia.pedidossi.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.florencia.pedidossi.MainActivity;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.adapters.DireccionAdapter;
import com.florencia.pedidossi.interfaces.IUsuario;
import com.florencia.pedidossi.models.Direccion;
import com.florencia.pedidossi.models.Provincia;
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

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int ACTIVITY_DIRECTION = 100;
    Toolbar toolbar;
    TextView lblTitle, lblSubtitle;
    ImageButton btnReturn, btnGuardar, btnCollapseInfo, btnCollapseAddress;
    Button btnNewDireccion;
    LinearLayout lyCollapseInfo, lyCollapseAddress, lyInfoPersonal, lyAddress;
    RecyclerView rvDirecciones;
    RadioButton rbCedula, rbRuc, rbPasaporte;
    EditText tvNip, tvNombres, tvApellidos, tvFono, tvEmail;
    DireccionAdapter adapter;

    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();

        cargaDirecciones();
    }

    private void init() {
        loading = new LoadingDialog(ProfileActivity.this);
        lblTitle = findViewById(R.id.toolbarTitle);
        btnReturn = findViewById(R.id.toolbarReturn);
        btnGuardar = findViewById(R.id.toolbarAction);
        rvDirecciones = findViewById(R.id.rvDirecciones);
        lyCollapseInfo = findViewById(R.id.lyCollapseInfoPersonal);
        lyCollapseAddress = findViewById(R.id.lyCollapseAddress);
        lyInfoPersonal = findViewById(R.id.lyInfoPersonal);
        lyAddress = findViewById(R.id.lyAddress);
        btnCollapseInfo = findViewById(R.id.btnCollapseInfo);
        btnCollapseAddress = findViewById(R.id.btnCollapseAddress);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);
        rbCedula = findViewById(R.id.rbCedula);
        rbRuc = findViewById(R.id.rbRuc);
        rbPasaporte = findViewById(R.id.rbPasaporte);
        tvNip = findViewById(R.id.tv_nip);
        tvNombres = findViewById(R.id.tv_nombres);
        tvApellidos = findViewById(R.id.tv_apellidos);
        tvFono = findViewById(R.id.tv_fono);
        tvEmail = findViewById(R.id.tv_email);
        btnNewDireccion = findViewById(R.id.btnNewDireccion);
        btnGuardar.setVisibility(View.VISIBLE);

        lblTitle.setText(R.string.profile);
        btnReturn.setOnClickListener(this::onClick);
        btnGuardar.setOnClickListener(this::onClick);
        lyCollapseInfo.setOnClickListener(this::onClick);
        lyCollapseAddress.setOnClickListener(this::onClick);
        btnCollapseInfo.setOnClickListener(this::onClick);
        btnCollapseAddress.setOnClickListener(this::onClick);
        btnNewDireccion.setOnClickListener(this::onClick);

        initRetrofit();
        CargaDatos();
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

    private void CargaDatos() {
        rbCedula.setChecked(Global.usuario.tiponip.equals("05"));
        rbRuc.setChecked(Global.usuario.tiponip.equals("04"));
        rbPasaporte.setChecked(Global.usuario.tiponip.equals("06"));
        tvNip.setText(Global.usuario.nip);
        tvNombres.setText(Global.usuario.nombres);
        tvApellidos.setText(Global.usuario.apellidos);
        tvFono.setText(Global.usuario.fono);
        tvEmail.setText(Global.usuario.email);
    }

    void cargaDirecciones() {
        if (Global.usuario.direcciones == null)
            Global.usuario.direcciones = new ArrayList<>();
        adapter = new DireccionAdapter(ProfileActivity.this, Global.usuario.direcciones, retrofit);
        rvDirecciones.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarAction:
                SaveProfile();
                break;
            case R.id.toolbarReturn:
                onBackPressed();
                break;
            case R.id.lyCollapseInfoPersonal:
            case R.id.btnCollapseInfo:
                Utils.EfectoLayout(lyInfoPersonal, btnCollapseInfo);
                break;
            case R.id.lyCollapseAddress:
            case R.id.btnCollapseAddress:
                Utils.EfectoLayout(lyAddress, btnCollapseAddress);
                break;
            case R.id.btnNewDireccion:
                //OpenModalDireccion(-1);
                newDireccion(-1);
                break;
        }
    }

    private void SaveProfile() {

    }

    public void newDireccion(Integer pos){
        Intent i = new Intent(ProfileActivity.this, DirectionActivity.class);
        i.putExtra("position", pos);
        startActivityForResult(i, ACTIVITY_DIRECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_DIRECTION:
                    if (Global.usuario.direcciones != null) {
                        adapter.notifyDataSetChanged();
                        MyToast.show(ProfileActivity.this, R.string.address_save, MyToast.SHORT, MyToast.SUCCESS);
                    }
                    break;
            }
        }
    }
}
