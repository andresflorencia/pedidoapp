package com.florencia.pedidossi.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.florencia.pedidossi.R;
import com.florencia.pedidossi.adapters.DetalleCarritoAdapter;
import com.florencia.pedidossi.models.Carrito;
import com.florencia.pedidossi.models.DetalleCarrito;
import com.florencia.pedidossi.models.Producto;
import com.florencia.pedidossi.utils.Global;
import com.florencia.pedidossi.utils.MyToast;
import com.florencia.pedidossi.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class CarritoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int ACTIVITY_LOGIN = 100;
    private static final int ACTIVITY_CONFIRM = 200;
    Toolbar toolbar;
    RecyclerView rvCarrito;
    public TextView lblTitle, lblCantCarrito, lblSubtitle;
    Button btnNext;
    TextView tvSubtotal, tvEnvio, tvTotal;
    ImageButton btnReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init() {

        rvCarrito = findViewById(R.id.rvCarrito);
        lblTitle = findViewById(R.id.toolbarTitle);
        btnNext = findViewById(R.id.btnNext);
        btnReturn = findViewById(R.id.toolbarReturn);
        lblCantCarrito = findViewById(R.id.toolbarIndicador);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvEnvio = findViewById(R.id.tv_envio);
        tvTotal = findViewById(R.id.tv_total);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);

        lblTitle.setText(R.string.cart_shop);
        btnReturn.setVisibility(View.VISIBLE);
        btnNext.setOnClickListener(this::onClick);
        btnReturn.setOnClickListener(this::onClick);

        CargarDatos();
    }

    private void CargarDatos() {
        if(Global.empresa.establecimiento != null)
            Global.carrito.establecimientoid = Global.empresa.establecimiento.idestablecimiento;
        DetalleCarritoAdapter adapter = new DetalleCarritoAdapter(CarritoActivity.this, Global.carrito.detalle);
        rvCarrito.setAdapter(adapter);
    }

    public void Totales(){
        try{
            if(Global.carrito != null){
                lblCantCarrito.setText("" + Global.carrito.detalle.size());
                tvSubtotal.setText(Utils.FormatoMoneda(Global.carrito.SubTotal(), 2));
                tvEnvio.setText(Utils.FormatoMoneda(Global.carrito.costo_envio, 2));
                tvTotal.setText(Utils.FormatoMoneda(Global.carrito.Total(), 2));
            }
        }catch (Exception e){}
    }

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btnNext:
                if(Global.usuario == null) {
                    i = new Intent(v.getContext(), LoginActivity.class);
                    startActivityForResult(i, ACTIVITY_LOGIN);
                }else if(Global.carrito == null || Global.carrito.detalle.size() == 0)
                    MyToast.show(CarritoActivity.this, "No hay productos en el carrito.", MyToast.SHORT, MyToast.ERROR);
                else
                    ConfirmPedido();
                break;
            case R.id.toolbarReturn:
                onBackPressed();
                break;
        }
    }

    private void ConfirmPedido() {
        Intent i = new Intent(CarritoActivity.this, EnvioPedidoActivity.class);
        startActivityForResult(i, ACTIVITY_CONFIRM);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Totales();
        if(Global.empresa != null) {
            lblSubtitle.setText(Global.empresa.nombre);
            lblSubtitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK){
            switch (requestCode){
                case ACTIVITY_LOGIN:
                    ConfirmPedido();
                    break;
                case ACTIVITY_CONFIRM:
                    setResult(RESULT_OK);
                    onBackPressed();
                    break;
            }
        }
    }
}
