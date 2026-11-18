package com.example.quanlylichhen;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    private DatabaseHelper database;
    private AppointmentAdapter adapter;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        database = new DatabaseHelper(this);
        adapter = new AppointmentAdapter(this);
        ListView list = findViewById(R.id.lvAppointments);
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.tvEmpty));
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, AppointmentDetailActivity.class);
            intent.putExtra("id", adapter.getItem(position).id);
            startActivity(intent);
        });
        findViewById(R.id.btnAddAppointment).setOnClickListener(v -> startActivity(new Intent(this, AddAppointmentActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            adapter.setAppointments(database.getAllAppointments());
            ((TextView) findViewById(R.id.tvTotal)).setText(String.valueOf(database.countAll()));
            ((TextView) findViewById(R.id.tvReminders)).setText(String.valueOf(database.countReminders()));
        } catch (SQLiteException e) {
            Toast.makeText(this, "Không thể tải danh sách lịch hẹn", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
