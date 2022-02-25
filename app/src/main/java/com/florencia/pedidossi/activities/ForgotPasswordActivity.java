package com.florencia.pedidossi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.florencia.pedidossi.R;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener{
    Toolbar toolbar;
    TextView lblTitle, tvLogin, lblSubtitle;
    ImageButton btnReturn;
    Button btnRecuperar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        lblTitle = findViewById(R.id.toolbarTitle);
        btnReturn = findViewById(R.id.toolbarReturn);
        tvLogin = findViewById(R.id.lblLogin);
        btnRecuperar = findViewById(R.id.btnRecuperar);
        lblSubtitle = findViewById(R.id.toolbarSubTitle);

        lblTitle.setText(R.string.forgot_password2);
        btnReturn.setOnClickListener(this::onClick);
        tvLogin.setOnClickListener(this::onClick);
        btnRecuperar.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnRecuperar:
            case R.id.toolbarReturn:
            case R.id.lblLogin:
                finish();
                break;
        }
    }
}
