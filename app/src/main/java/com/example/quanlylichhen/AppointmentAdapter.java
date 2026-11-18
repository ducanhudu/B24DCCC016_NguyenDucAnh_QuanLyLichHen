package com.example.quanlylichhen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class AppointmentAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private ArrayList<Appointment> appointments = new ArrayList<>();

    public AppointmentAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void setAppointments(ArrayList<Appointment> appointments) {
        this.appointments = appointments;
        notifyDataSetChanged();
    }

    @Override public int getCount() { return appointments.size(); }
    @Override public Appointment getItem(int position) { return appointments.get(position); }
    @Override public long getItemId(int position) { return getItem(position).id; }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) view = inflater.inflate(R.layout.item_appointment, parent, false);
        Appointment appointment = getItem(position);
        ((TextView) view.findViewById(R.id.tvAppointmentTitle)).setText(appointment.title);
        ((TextView) view.findViewById(R.id.tvAppointmentLocation)).setText(appointment.location);
        ((TextView) view.findViewById(R.id.tvAppointmentTime)).setText(appointment.displayDateTime());
        TextView reminder = view.findViewById(R.id.tvAppointmentReminder);
        reminder.setText(appointment.reminder ? "Có nhắc" : "Không nhắc");
        reminder.setTextColor(view.getContext().getColor(appointment.reminder ? R.color.primary : R.color.muted));
        return view;
    }
}
