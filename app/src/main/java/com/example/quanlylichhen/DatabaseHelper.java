package com.example.quanlylichhen;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), "appointments.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL)");
        db.execSQL("CREATE TABLE appointments (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, location TEXT NOT NULL, date TEXT NOT NULL, time TEXT NOT NULL, reminder INTEGER NOT NULL CHECK(reminder IN (0, 1)))");
        ContentValues user = new ContentValues();
        user.put("username", "admin");
        user.put("password", "123456");
        db.insertOrThrow("users", null, user);
        db.insertOrThrow("appointments", null, values(new Appointment(0, "Họp nhóm đồ án", "Phòng B203", "2026-10-25", "09:00", true)));
        db.insertOrThrow("appointments", null, values(new Appointment(0, "Khám răng", "Nha khoa Minh Tâm", "2026-10-26", "14:30", true)));
        db.insertOrThrow("appointments", null, values(new Appointment(0, "Đi cà phê với bạn", "Highland Coffee", "2026-10-27", "19:00", false)));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public boolean checkLogin(String username, String password) {
        try (Cursor cursor = getReadableDatabase().query("users", new String[]{"id"},
                "username = ? AND password = ?", new String[]{username, password}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    public long insertUser(String username, String password) {
        ContentValues user = new ContentValues();
        user.put("username", username);
        user.put("password", password);
        return getWritableDatabase().insert("users", null, user);
    }

    public long insertAppointment(Appointment appointment) {
        return getWritableDatabase().insert("appointments", null, values(appointment));
    }

    public ArrayList<Appointment> getAllAppointments() {
        ArrayList<Appointment> appointments = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("appointments", null, null, null, null, null, "date ASC, time ASC")) {
            while (cursor.moveToNext()) appointments.add(read(cursor));
        }
        return appointments;
    }

    public Appointment getAppointmentById(int id) {
        try (Cursor cursor = getReadableDatabase().query("appointments", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? read(cursor) : null;
        }
    }

    public boolean updateReminder(int id, boolean on) {
        ContentValues values = new ContentValues();
        values.put("reminder", on ? 1 : 0);
        return getWritableDatabase().update("appointments", values, "id = ?", new String[]{String.valueOf(id)}) == 1;
    }

    public boolean deleteAppointment(int id) {
        return getWritableDatabase().delete("appointments", "id = ?", new String[]{String.valueOf(id)}) == 1;
    }

    public int countAll() {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM appointments", null)) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    public int countReminders() {
        try (Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM appointments WHERE reminder = ?", new String[]{"1"})) {
            cursor.moveToFirst();
            return cursor.getInt(0);
        }
    }

    private ContentValues values(Appointment appointment) {
        ContentValues values = new ContentValues();
        values.put("title", appointment.title);
        values.put("location", appointment.location);
        values.put("date", appointment.date);
        values.put("time", appointment.time);
        values.put("reminder", appointment.reminder ? 1 : 0);
        return values;
    }

    private Appointment read(Cursor cursor) {
        return new Appointment(cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("title")),
                cursor.getString(cursor.getColumnIndexOrThrow("location")),
                cursor.getString(cursor.getColumnIndexOrThrow("date")),
                cursor.getString(cursor.getColumnIndexOrThrow("time")),
                cursor.getInt(cursor.getColumnIndexOrThrow("reminder")) == 1);
    }
}
