package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.florencia.pedidossi.BuildConfig;
import com.florencia.pedidossi.R;
import com.florencia.pedidossi.models.PedidoHistory;
import com.florencia.pedidossi.models.ProductoHistory;
import com.florencia.pedidossi.utils.Constants;
import com.florencia.pedidossi.utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;

public class InfoHistoryActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar toolbar;
    TextView lblInfoEmpresa, lblHeaderPedido, lblCantidad, lblNombreProducto, lblPrecio, lblSubtotal;
    PedidoHistory pedido;

    TextView toolbarTitle;
    ImageButton toolbarReturn, btnWhatsapp, btnShare;
    LinearLayout cvContent;

    String ExternalDirectory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_history);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        init();
    }

    private void init() {
        lblInfoEmpresa = findViewById(R.id.lblInfoEmpresa);
        lblHeaderPedido = findViewById(R.id.lblHeaderPedido);
        lblCantidad = findViewById(R.id.lblCantidad);
        lblNombreProducto = findViewById(R.id.lblNombreProducto);
        lblPrecio = findViewById(R.id.lblPrecio);
        lblSubtotal = findViewById(R.id.lblSubtotal);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarReturn = findViewById(R.id.toolbarReturn);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);
        btnShare = findViewById(R.id.btnShare);
        cvContent = findViewById(R.id.cvContent);

        toolbarTitle.setText("Información de pedido");
        toolbarReturn.setVisibility(View.VISIBLE);

        toolbarReturn.setOnClickListener(this::onClick);
        btnWhatsapp.setOnClickListener(this::onClick);
        btnShare.setOnClickListener(this::onClick);

        File miFile = new File(getExternalMediaDirs()[0], Constants.FOLDER_FILES);
        if (!miFile.exists())
            miFile.mkdirs();

        ExternalDirectory = getExternalMediaDirs()[0] + File.separator + Constants.FOLDER_FILES;

        if (getIntent().getExtras() != null) {
            String json = getIntent().getExtras().getString("pedido", "");
            pedido = new Gson().fromJson(json, PedidoHistory.class);
            if (pedido != null)
                MostrarDatos();
        }
    }

    private void MostrarDatos() {
        String htmlEmpresa = "<strong>RUC: </strong>" + pedido.rucempresa + "<br>";
        htmlEmpresa += "<strong>Nombre: </strong>" + (pedido.aliasempresa.equals("") ? pedido.nombreempresa : pedido.aliasempresa) + "<br>";
        if (!pedido.fonoempresa.equals(""))
            htmlEmpresa += "<strong>Contacto: </strong>" + pedido.fonoempresa + "<br>";
        if (!pedido.emailempresa.equals(""))
            htmlEmpresa += "<strong>Email: </strong>" + pedido.emailempresa + "<br>";
        if (!pedido.direccionempresa.equals(""))
            htmlEmpresa += "<strong>Dirección: </strong>" + pedido.direccionempresa + "<br>";

        Utils.FormatHtml(lblInfoEmpresa, htmlEmpresa);

        String htmlHeader = "<strong>Total: </strong>" + Utils.FormatoMoneda(pedido.total, 2) + "<br>";
        htmlHeader += "<strong>Núm. Ítems: </strong>" + pedido.detalle.size() + "<br>";
        htmlHeader += "<strong>Fecha pedido: </strong>" + pedido.fechapedido + "<br>";
        htmlHeader += "<strong>Dirección: </strong>" + pedido.direccion.toString()
                + (!pedido.direccion.referencia.equals("") ? " <strong>Ref.: </strong> " + pedido.direccion.referencia : "");

        Utils.FormatHtml(lblHeaderPedido, htmlHeader);

        String cant = "", name_pro = "", precio = "", subt = "";
        for (ProductoHistory item : pedido.detalle) {
            cant += item.cantidad.toString() + "\n";
            name_pro += item.nombreproducto + "\n";
            precio += Utils.FormatoMoneda(item.precio, 2) + "\n";
            subt += Utils.FormatoMoneda(item.total, 2) + "\n";
        }
        lblCantidad.setText(cant);
        lblNombreProducto.setText(name_pro);
        lblPrecio.setText(precio);
        lblSubtotal.setText(subt);
    }

    private void GenerarImagen(boolean other) {
        try {
            cvContent.setDrawingCacheEnabled(true);
            cvContent.buildDrawingCache(true);
            Bitmap bitmap = Bitmap.createBitmap(cvContent.getDrawingCache());
            cvContent.setDrawingCacheEnabled(false);

            String nameimg = "pedido_" + Utils.getDateFormat("yyyyMMdd_HHmmss") + ".png";
            String numberphone = "";

            File imageFile = new File(ExternalDirectory + File.separator + nameimg);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            Utils.showMessage(InfoHistoryActivity.this, "Generando imagen, espere un momento...");

            String ni = nameimg;
            final String number = numberphone;
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (other)
                    sendImage(imageFile);
                else
                    sendImageWhatsApp(number, ni);
            }, 3000);

        } catch (Throwable e) {
            Utils.showMessage(InfoHistoryActivity.this, "ERROR al generar imagen .png");
            Log.d("TAGIMAGEN", e.getMessage());
        }
    }

    //PERMITE COMPARTIR IMAGEN VIA WHATSAPP
    private void sendImageWhatsApp(String phoneNumber, String nombreImagen) {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(ExternalDirectory + File.separator + nombreImagen));
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.leyenda_image));
            intent.putExtra("jid", phoneNumber + "@s.whatsapp.net"); //numero telefonico sin prefijo "+"!
            intent.setPackage("com.whatsapp");
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            Utils.showMessage(InfoHistoryActivity.this, "Whatsapp no esta instalado.");
            Log.d("TAGIMAGE", ex.getMessage());
        }
    }

    //PERMITE COMPARTIR IMAGEN MEDIANTE APLICACIONES MULTIMEDIA
    private void sendImage(File fileImage) {
        try {
            String PACKAGE_NAME = BuildConfig.APPLICATION_ID + ".services.GenericFileProvider";

            Uri contentUri = FileProvider.getUriForFile(InfoHistoryActivity.this, PACKAGE_NAME, fileImage);

            if (contentUri != null) {

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.leyenda_image));
                startActivity(Intent.createChooser(shareIntent, "Elige una aplicación:"));

            }
        } catch (Exception e) {
            Log.d("TAGIMAGE", e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbarReturn:
                onBackPressed();
                return;
            case R.id.btnWhatsapp:
            case R.id.btnShare:
                GenerarImagen(v.getId() == R.id.btnShare);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
