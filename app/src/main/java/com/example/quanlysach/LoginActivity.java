package com.example.quanlysach;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
    private EditText username, password; private TextView error;
    @Override public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); setContentView(R.layout.activity_login); username=findViewById(R.id.etUsername); password=findViewById(R.id.etPassword); error=findViewById(R.id.tvLoginError); Button login=findViewById(R.id.btnLogin); login.setOnClickListener(v -> doLogin()); }
    private void doLogin() { String u=username.getText().toString().trim(), p=password.getText().toString(); if (u.isEmpty() || p.isEmpty()) { error.setText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu"); error.setVisibility(View.VISIBLE); return; } if (u.equals("admin") && p.equals("123456")) { startActivity(new Intent(this, MainActivity.class)); finish(); } else { error.setText("Sai tên đăng nhập hoặc mật khẩu"); error.setVisibility(View.VISIBLE); } }
}
