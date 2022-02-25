package com.florencia.pedidossi.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.florencia.pedidossi.R;

public class MyToast {
    //****DURATION****///
    public static final int LONG = 1;
    public static final int SHORT = 0;

    public static final int INFO = 1;
    public static final int WARNING = 2;
    public static final int SUCCESS = 3;
    public static final int ERROR = 4;

    public static void show(Activity a, String m, int duration, int type) {
        LayoutInflater inflater = a.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_tooltip, (ViewGroup) a.findViewById(R.id.lyTooltip));
        ((TextView) layout.findViewById(R.id.message)).setText(m);

        switch (type){
            case INFO:
                layout.setBackground(a.getDrawable(R.drawable.bg_button_blue));
                break;
            case WARNING:
                layout.setBackground(a.getDrawable(R.drawable.tooltip_warning));
                ((TextView) layout.findViewById(R.id.message)).setTextColor(a.getResources().getColor(R.color.black));
                ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.ic_warning);
                break;
            case SUCCESS:
                layout.setBackground(a.getDrawable(R.drawable.tooltip_success));
                ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.ic_happy);
                break;
            case ERROR:
                layout.setBackground(a.getDrawable(R.drawable.tooltip_error));
                ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.ic_sad);
                break;
        }

        Toast toast = new Toast(a.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }
    public static void show(Activity a, int resource, int duration, int type) {
        LayoutInflater inflater = a.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_tooltip, (ViewGroup) a.findViewById(R.id.lyTooltip));
        ((TextView) layout.findViewById(R.id.message)).setText(resource);

        switch (type){
            case INFO:
                layout.setBackground(a.getDrawable(R.drawable.bg_button_blue));
                break;
            case WARNING:
                layout.setBackground(a.getDrawable(R.drawable.tooltip_warning));
                ((TextView) layout.findViewById(R.id.message)).setTextColor(a.getResources().getColor(R.color.black));
                ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.ic_warning);
                break;
            case SUCCESS:
                layout.setBackground(a.getDrawable(R.drawable.tooltip_success));
                ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.ic_happy);
                break;
            case ERROR:
                layout.setBackground(a.getDrawable(R.drawable.tooltip_error));
                ((ImageView) layout.findViewById(R.id.image)).setImageResource(R.drawable.ic_sad);
                break;
        }

        Toast toast = new Toast(a.getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }
}
