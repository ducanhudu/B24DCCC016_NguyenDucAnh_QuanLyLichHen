package com.example.quanlylichhen;

public class Appointment {
    public int id;
    public String title, location, date, time;
    public boolean reminder;

    public Appointment(int id, String title, String location, String date, String time, boolean reminder) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.date = date;
        this.time = time;
        this.reminder = reminder;
    }

    public String displayDateTime() {
        String[] parts = date.split("-");
        return parts[2] + "/" + parts[1] + "/" + parts[0] + " · " + time;
    }
}
