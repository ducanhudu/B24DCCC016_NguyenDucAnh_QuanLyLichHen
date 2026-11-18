package com.example.quanlylichhen;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends BaseActivity {
    private EditText username, password;
    private TextView error;
    private DatabaseHelper database;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_login);
        database = new DatabaseHelper(this);
        username = findViewById(R.id.etUsername);
        password = findViewById(R.id.etPassword);
        error = findViewById(R.id.tvLoginError);
        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
    }

    private void login() {
        String name = username.getText().toString().trim();
        String pass = password.getText().toString();
        error.setVisibility(View.GONE);
        if (name.isEmpty() || pass.trim().isEmpty()) {
            showError("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu");
            return;
        }
        try {
            if (database.checkLogin(name, pass)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                showError("Sai tên đăng nhập hoặc mật khẩu");
            }
        } catch (SQLiteException e) {
            showError("Không thể đọc dữ liệu. Vui lòng thử lại");
        }
    }

    private void showError(String message) {
        error.setText(message);
        error.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
