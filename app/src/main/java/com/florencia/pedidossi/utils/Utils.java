package com.florencia.pedidossi.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.florencia.pedidossi.R;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static Double RoundDecimal(Double numero, Integer numeroDecimales) {
        BigDecimal result = BigDecimal.valueOf(numero);
        //return (double) Math.round(numero * Math.pow(10, numeroDecimales)) / Math.pow(10, numeroDecimales);
        return result.setScale(numeroDecimales, RoundingMode.HALF_UP).doubleValue();
    }

    public static String FormatoMoneda(Double valor, int numeroDecimales) {
        String retorno = "$0,00";
        try {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
            valor = RoundDecimal(valor, numeroDecimales);
            retorno = format.format(valor);
        } catch (Exception e) {
            Log.d("TAGERROR", "FormatoMoneda(): " + e.getMessage());
        }
        return retorno;
    }

    public static void EfectoLayout(View lyEfecto, TextView lblEfecto) {
        if (lyEfecto.getVisibility() == View.GONE) {
            lyEfecto.setVisibility(View.VISIBLE);
            lblEfecto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
        } else if (lyEfecto.getVisibility() == View.VISIBLE) {
            lyEfecto.setVisibility(View.GONE);
            lblEfecto.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        }
    }

    public static void EfectoLayout(View lyEfecto, ImageButton btnEfecto) {
        if (lyEfecto.getVisibility() == View.GONE) {
            lyEfecto.setVisibility(View.VISIBLE);
            btnEfecto.setImageResource(R.drawable.ic_arrow_up);
        } else if (lyEfecto.getVisibility() == View.VISIBLE) {
            lyEfecto.setVisibility(View.GONE);
            btnEfecto.setImageResource(R.drawable.ic_arrow_down);
        }
    }

    public static void showMessage(Context ctx, String message){
        try{
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
        }catch (Exception e){
            Log.d("TAGUTILS", "showMessage(): " + e.getMessage());
        }
    }
    public static void showMessageShort(Context ctx, String message){
        try{
            Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.d("TAGUTILS", "showMessage(): " + e.getMessage());
        }
    }

    public static void FormatHtml(TextView label, String text){
        try{
            label.setText(Html.fromHtml(text));
        }catch (Exception e){}
    }



    public static String getDateFormat(String formato) {
        String fecha = "";
        try {
            Locale idioma = new Locale("es", "ES");
            SimpleDateFormat sdf = new SimpleDateFormat(formato, idioma);
            fecha = sdf.format(new Date());
        } catch (Exception e) {
            fecha = "";
        }
        return fecha;
    }


    public static void verificarPermisos(Activity context) {
        ArrayList<String> pe = new ArrayList<>();
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                pe.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                pe.add(Manifest.permission.ACCESS_FINE_LOCATION);
                pe.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                pe.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                pe.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                pe.add(Manifest.permission.CAMERA);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED)
                pe.add(Manifest.permission.INSTALL_PACKAGES);*/

            if (pe.size() > 0) {
                String[] permisos = new String[pe.size()];
                for (int i = 0; i < pe.size(); i++)
                    permisos[i] = pe.get(i);
                ActivityCompat.requestPermissions(context, permisos, 1000);
            }

        } catch (Exception e) {
            Log.d("TAGP", e.getMessage());
        }
    }
}
