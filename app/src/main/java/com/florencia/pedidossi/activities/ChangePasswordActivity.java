package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.florencia.pedidossi.R;

public class ChangePasswordActivity extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    TextView lblTitle, lblSubtitle;
    ImageButton btnReturn;
    Button btnCambiar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        lblTitle = findViewById(R.id.toolbarTitle);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);
        btnReturn = findViewById(R.id.toolbarReturn);
        btnCambiar = findViewById(R.id.btnCambiar);

        lblTitle.setText(R.string.forgot_password2);
        btnReturn.setOnClickListener(this::onClick);
        btnCambiar.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.toolbarReturn:
            case R.id.btnCambiar:
                finish();
                break;
        }
    }
}
