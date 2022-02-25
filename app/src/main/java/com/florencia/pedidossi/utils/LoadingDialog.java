package com.florencia.pedidossi.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.florencia.pedidossi.R;

public class LoadingDialog {
    Activity activity;
    AlertDialog dialog;

    public LoadingDialog(Activity activity){
        this.activity = activity;
    }

    public void start(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_loading, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }
    public void dismiss(){
        dialog.dismiss();
    }
}
