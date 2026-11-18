package com.example.quanlylichhen;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAppointmentActivity extends BaseActivity {
    private EditText title, location;
    private DatePicker date;
    private TimePicker time;
    private Switch reminder;
    private TextView error;
    private DatabaseHelper database;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_add_appointment);
        database = new DatabaseHelper(this);
        title = findViewById(R.id.etTitle);
        location = findViewById(R.id.etLocation);
        date = findViewById(R.id.datePicker);
        time = findViewById(R.id.timePicker);
        reminder = findViewById(R.id.swReminder);
        error = findViewById(R.id.tvDateError);
        time.setIs24HourView(true);
        if (state == null) {
            Calendar initial = Calendar.getInstance();
            initial.add(Calendar.HOUR_OF_DAY, 1);
            date.updateDate(initial.get(Calendar.YEAR), initial.get(Calendar.MONTH), initial.get(Calendar.DAY_OF_MONTH));
            time.setHour(initial.get(Calendar.HOUR_OF_DAY));
            time.setMinute(initial.get(Calendar.MINUTE));
        }
        findViewById(R.id.btnSaveAppointment).setOnClickListener(v -> save());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void save() {
        date.clearFocus();
        time.clearFocus();
        String name = title.getText().toString().trim();
        String place = location.getText().toString().trim();
        boolean valid = true;
        error.setVisibility(View.GONE);
        if (name.isEmpty()) {
            title.setError("Vui lòng nhập tên lịch hẹn");
            valid = false;
        }
        if (place.isEmpty()) {
            location.setError("Vui lòng nhập địa điểm");
            valid = false;
        }
        if (!valid) return;
        Calendar selected = Calendar.getInstance();
        selected.clear();
        selected.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), time.getHour(), time.getMinute(), 0);
        if (selected.getTimeInMillis() <= System.currentTimeMillis()) {
            error.setText("Thời điểm hẹn phải sau thời điểm hiện tại");
            error.setVisibility(View.VISIBLE);
            return;
        }
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selected.getTime());
        String hour = new SimpleDateFormat("HH:mm", Locale.US).format(selected.getTime());
        try {
            long id = database.insertAppointment(new Appointment(0, name, place, day, hour, reminder.isChecked()));
            if (id == -1) {
                Toast.makeText(this, "Không thể lưu lịch hẹn", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đã thêm lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
        } catch (SQLiteException e) {
            Toast.makeText(this, "Không thể lưu lịch hẹn. Vui lòng thử lại", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
