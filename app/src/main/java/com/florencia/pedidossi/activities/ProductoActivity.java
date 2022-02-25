package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.interfaces.IProducto;
import com.florencia.pedidossi.models.Carrito;
import com.florencia.pedidossi.models.DetalleCarrito;
import com.florencia.pedidossi.models.Producto;
import com.florencia.pedidossi.models.ProductoImage;
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

public class ProductoActivity extends AppCompatActivity implements View.OnClickListener {

    Producto producto;
    TextView tvNombre, tvDescripcion, tvCodigo, tvCategoria, tvPrecio,
            tvTotal, tvCantidad, lblTitle, tvUnidad, tvMarca, tvPVP, lblCantCarrito, lblSubtitle;
    Button btnMas, btnMenos, btnAnadir;
    Toolbar toolbar;
    ImageButton btnReturn;
    ImageView imageView;

    private OkHttpClient okHttpClient;
    Retrofit retrofit;
    Gson gson;
    LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init() {
        try {
            loading = new LoadingDialog(ProductoActivity.this);
            tvNombre = findViewById(R.id.tv_nombre);
            tvDescripcion = findViewById(R.id.tv_descripcion);
            tvCodigo = findViewById(R.id.tv_codigo);
            tvCategoria = findViewById(R.id.tv_categoria);
            tvCantidad = findViewById(R.id.tv_cantidad);
            tvPrecio = findViewById(R.id.tv_precio);
            tvTotal = findViewById(R.id.tv_total);
            btnMas = findViewById(R.id.btnMas);
            btnMenos = findViewById(R.id.btnMenos);
            btnAnadir = findViewById(R.id.btnAnadir);
            lblTitle = findViewById(R.id.toolbarTitle);
            btnReturn = findViewById(R.id.toolbarReturn);
            imageView = findViewById(R.id.image);
            tvMarca = findViewById(R.id.tv_marca);
            tvUnidad = findViewById(R.id.tv_unidad);
            tvPVP = findViewById(R.id.tv_pvp);
            lblCantCarrito = findViewById(R.id.toolbarIndicador);
            lblSubtitle = findViewById(R.id.toolbarSubTitle);

            btnMenos.setOnClickListener(this::onClick);
            btnMas.setOnClickListener(this::onClick);
            btnAnadir.setOnClickListener(this::onClick);
            btnReturn.setOnClickListener(this::onClick);
            btnReturn.setVisibility(View.VISIBLE);

            initRetrofit();
            if (getIntent().getExtras() != null) {
                String rucempresa = getIntent().getExtras().getString("rucempresa");
                Integer idproducto = getIntent().getExtras().getInt("idproducto");

                cargaDatos(rucempresa, idproducto);
            }
        } catch (Exception e) {
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

    private void cargaDatos(String rucempresa, Integer idproducto) {
        loading.start();
        Map<String, Object> data = new HashMap<>();
        data.put("rucempresa", rucempresa);
        data.put("idproducto", idproducto);
        if (Global.empresa.establecimiento != null)
            data.put("establecimientoid", Global.empresa.establecimiento.idestablecimiento);
        IProducto iProducto = retrofit.create(IProducto.class);
        Call<JsonObject> call = iProducto.getProductos(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    Utils.showMessage(ProductoActivity.this, "Error: " + response.code());
                    loading.dismiss();
                    return;
                }
                try {
                    if (response.body() != null) {
                        JsonObject obj = response.body();
                        if (obj.get("error").getAsBoolean())
                            MyToast.show(ProductoActivity.this, obj.get("message").getAsString(), MyToast.LONG, MyToast.ERROR);
                        else {
                            JsonObject oProducto = obj.get("data").getAsJsonObject().getAsJsonObject("productos");
                            if (oProducto != null) {
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
                                producto.stock = oProducto.get("stock").getAsDouble();

                                if (oProducto.get("images").isJsonArray()) {
                                    JsonArray aImage = oProducto.getAsJsonArray("images");
                                    if (aImage != null) {
                                        producto.images = new ArrayList<>();
                                        ProductoImage imagen;
                                        for (JsonElement el : aImage) {
                                            if (el.isJsonNull())
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

                                MostrarDatos();
                            } else
                                Utils.showMessage(ProductoActivity.this, "El producto es null");
                        }
                    } else
                        Utils.showMessage(ProductoActivity.this, "el body es null");
                } catch (Exception ex) {
                }
                loading.dismiss();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                loading.dismiss();
            }
        });
    }

    private void MostrarDatos() {
        tvCodigo.setText("COD: " + producto.codigo);
        Utils.FormatHtml(tvUnidad, producto.unidad);
        tvNombre.setText(producto.nombreproducto);
        Utils.FormatHtml(tvDescripcion, "<strong>Descripción: </strong>" + producto.descripcion_corta);
        Utils.FormatHtml(tvCategoria, "<strong>Categoría: </strong>" + producto.categoria);
        Utils.FormatHtml(tvMarca, "<strong>Marca: </strong>" + producto.marca);
        String text = "<strong>Precio: </strong>" + Utils.FormatoMoneda(producto.Precio(), 2) + " / " + producto.abreunidad;
        text += "<br><strong>Stock: </strong>" + (producto.stock > 0 ? Utils.RoundDecimal(producto.stock, 2) + " " + producto.abreunidad : "No disponible por el momento");
        Utils.FormatHtml(tvPVP, text);

        tvPrecio.setText(Utils.FormatoMoneda(producto.Precio(), 2));
        tvPrecio.setTag(producto.Precio());
        tvTotal.setText(Utils.FormatoMoneda(producto.Precio(), 2));
        tvCantidad.setText("1");
        lblTitle.setText(producto.nombreproducto);

        if (Global.carrito != null) {
            for (DetalleCarrito item : Global.carrito.detalle) {
                if (item.producto.idproducto == producto.idproducto) {
                    item.precio = producto.Precio();
                    tvCantidad.setText(item.cantidad.toString());
                    tvTotal.setText(Utils.FormatoMoneda(item.Total(), 2));
                    btnAnadir.setText(R.string.update);
                    break;
                }
            }
        }

        Thread th = new Thread() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    String image = producto.image_default;
                    if (producto.images != null && producto.images.size() > 0)
                        image = producto.images.get(0).url;

                    Glide.with(ProductoActivity.this)
                            .load(image)
                            .centerCrop()
                            .placeholder(R.drawable.cart_loading)
                            .into(imageView);
                });
            }
        };
        th.start();
    }

    private void CambiaCantidad(boolean incrementar) {
        double val = Double.parseDouble(tvCantidad.getText().toString().trim());
        if (incrementar)
            val++;
        else if (val > 1)
            val--;
        tvCantidad.setText(String.valueOf(val));
        CalculaTotal();
    }

    private void CalculaTotal() {
        try {
            Double precio = Double.parseDouble(tvPrecio.getTag().toString());
            Double cantidad = Double.parseDouble(tvCantidad.getText().toString());
            tvTotal.setText(Utils.FormatoMoneda(cantidad * precio, 2));
        } catch (Exception e) {

        }
    }

    private void AddItemCart() {
        try {
            if (Global.carrito == null)
                Global.carrito = new Carrito();
            boolean added = false;
            for (DetalleCarrito item : Global.carrito.detalle) {
                if (item.producto.idproducto == producto.idproducto) {
                    item.cantidad = Double.parseDouble(tvCantidad.getText().toString().trim());
                    item.precio = Double.parseDouble(tvPrecio.getTag().toString());
                    item.Total();
                    added = true;
                    MyToast.show(ProductoActivity.this, R.string.item_update, MyToast.SHORT, MyToast.INFO);
                    break;
                }
            }
            if (!added) {
                DetalleCarrito newItem = new DetalleCarrito();
                newItem.producto = producto;
                newItem.cantidad = Double.parseDouble(tvCantidad.getText().toString().trim());
                newItem.precio = Double.parseDouble(tvPrecio.getTag().toString());
                newItem.Total();
                Global.carrito.detalle.add(newItem);
                MyToast.show(ProductoActivity.this, R.string.item_add, MyToast.SHORT, MyToast.INFO);
            }
            onBackPressed();
        } catch (Exception e) {
            Log.d("TAGACT", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMas:
                CambiaCantidad(true);
                break;
            case R.id.btnMenos:
                CambiaCantidad(false);
                break;
            case R.id.btnAnadir:
                AddItemCart();
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
