package com.example.quanlylichhen;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AppointmentDetailActivity extends BaseActivity {
    private DatabaseHelper database;
    private Appointment appointment;
    private Switch reminder;
    private TextView reminderStatus;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_appointment_detail);
        database = new DatabaseHelper(this);
        int id = getIntent().getIntExtra("id", -1);
        try {
            appointment = database.getAppointmentById(id);
        } catch (SQLiteException e) {
            Toast.makeText(this, "Không thể tải lịch hẹn", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (appointment == null) {
            Toast.makeText(this, "Lịch hẹn không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ((TextView) findViewById(R.id.tvDetailTitle)).setText(appointment.title);
        ((TextView) findViewById(R.id.tvDetailLocation)).setText(appointment.location);
        ((TextView) findViewById(R.id.tvDetailTime)).setText(appointment.displayDateTime());
        reminder = findViewById(R.id.swDetailReminder);
        reminderStatus = findViewById(R.id.tvReminderStatus);
        reminder.setChecked(appointment.reminder);
        renderReminder();
        reminder.setOnCheckedChangeListener((button, checked) -> updateReminder(checked));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDeleteAppointment).setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Xóa lịch hẹn?")
                .setMessage("Bạn muốn xóa lịch hẹn này?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> delete())
                .show());
    }

    private void updateReminder(boolean checked) {
        if (checked == appointment.reminder) return;
        boolean updated = false;
        try {
            updated = database.updateReminder(appointment.id, checked);
        } catch (SQLiteException e) {
            Toast.makeText(this, "Không thể cập nhật nhắc nhở", Toast.LENGTH_SHORT).show();
        }
        if (updated) {
            appointment.reminder = checked;
        } else {
            reminder.setChecked(appointment.reminder);
        }
        renderReminder();
    }

    private void renderReminder() {
        reminderStatus.setText(appointment.reminder ? "Đang bật" : "Đang tắt");
    }

    private void delete() {
        try {
            if (database.deleteAppointment(appointment.id)) {
                Toast.makeText(this, "Đã xóa lịch hẹn", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lịch hẹn không còn tồn tại", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (SQLiteException e) {
            Toast.makeText(this, "Không thể xóa lịch hẹn", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
