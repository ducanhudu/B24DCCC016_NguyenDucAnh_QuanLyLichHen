package com.example.quanlylichhen;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class AppointmentTestRunner extends Instrumentation {
    private Bundle arguments;
    private final StringBuilder results = new StringBuilder();

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        this.arguments = arguments;
        start();
    }

    @Override
    public void onStart() {
        Bundle result = new Bundle();
        try {
            if (arguments != null && "verify".equals(arguments.getString("phase"))) {
                verifyRestart();
            } else {
                testDatabase();
                testScreens();
            }
            result.putString("stream", results + "\nALL CHECKS PASSED\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable error) {
            result.putString("stream", results + "\nFAILED: " + android.util.Log.getStackTraceString(error));
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private void testDatabase() {
        getTargetContext().deleteDatabase("appointments.db");
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) {
            check(db.countAll() == 3 && db.countReminders() == 2, "Dữ liệu mẫu và thống kê 3/2");
            check(db.checkLogin("admin", "123456") && !db.checkLogin("admin", "wrong"), "Đăng nhập truy vấn SQLite");
            check(!db.checkLogin("' OR 1=1 --", "x"), "Tham số truy vấn không cho đăng nhập bằng SQL injection");
            check(db.insertUser("tester", "pass") > 0 && db.checkLogin("tester", "pass"), "Tài khoản thêm trong SQLite đăng nhập được");
            long later = db.insertAppointment(new Appointment(0, "Later", "Room", "2099-01-02", "10:30", false));
            long earlier = db.insertAppointment(new Appointment(0, "Earlier", "Room", "2099-01-02", "09:05", true));
            long nextDay = db.insertAppointment(new Appointment(0, "Next", "Room", "2099-01-03", "08:00", false));
            ArrayList<Appointment> rows = db.getAllAppointments();
            check(rows.get(3).id == earlier && rows.get(4).id == later && rows.get(5).id == nextDay,
                    "Sắp xếp theo ngày và giờ, không phụ thuộc thứ tự thêm");
            check("25/10/2026 · 09:00".equals(rows.get(0).displayDateTime()), "Định dạng ngày giờ hiển thị");
            check(db.updateReminder((int) later, true) && db.getAppointmentById((int) later).reminder, "Cập nhật nhắc nhở trong SQLite");
            check(db.deleteAppointment((int) earlier) && db.getAppointmentById((int) earlier) == null, "Xóa và truy vấn id đã xóa");
            check(db.getAppointmentById(-1) == null && !db.updateReminder(-1, true), "Xử lý id không tồn tại");
        }
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) {
            check(db.countAll() == 5 && db.countReminders() == 3, "Đóng và mở lại database giữ dữ liệu");
        }
        getTargetContext().deleteDatabase("appointments.db");
    }

    private void testScreens() throws Exception {
        Activity login = launch(LoginActivity.class, -1);
        screenshot("01-login");
        click(login, R.id.btnLogin);
        check(visible(login, R.id.tvLoginError), "Đăng nhập bỏ trống báo lỗi");
        edit(login, R.id.etUsername, "admin");
        edit(login, R.id.etPassword, "wrong");
        click(login, R.id.btnLogin);
        check("Sai tên đăng nhập hoặc mật khẩu".equals(text(login, R.id.tvLoginError)), "Đăng nhập sai báo đúng thông báo");
        screenshot("06-login-error");
        edit(login, R.id.etPassword, "123456");
        Activity main = transition(login, R.id.btnLogin, MainActivity.class);
        check(login.isFinishing() || login.isDestroyed(), "Đăng nhập thành công đóng LoginActivity");
        check("3".equals(text(main, R.id.tvTotal)) && "2".equals(text(main, R.id.tvReminders)), "Thống kê ban đầu trên giao diện");
        screenshot("02-list");
        Activity add = transition(main, R.id.btnAddAppointment, AddAppointmentActivity.class);
        screenshot("03-add");
        edit(add, R.id.etTitle, "   ");
        edit(add, R.id.etLocation, "   ");
        click(add, R.id.btnSaveAppointment);
        final boolean[] fieldErrors = new boolean[1];
        ui(() -> fieldErrors[0] = ((EditText) add.findViewById(R.id.etTitle)).getError() != null
                && ((EditText) add.findViewById(R.id.etLocation)).getError() != null);
        check(fieldErrors[0] && !add.isFinishing(), "Không lưu tên hoặc địa điểm chỉ có khoảng trắng");
        screenshot("07-add-empty");
        edit(add, R.id.etTitle, "Lịch kiểm thử");
        edit(add, R.id.etLocation, "Phòng kiểm thử");
        Calendar selected = Calendar.getInstance();
        selected.add(Calendar.DAY_OF_MONTH, -1);
        setDateTime(add, selected);
        click(add, R.id.btnSaveAppointment);
        check(visible(add, R.id.tvDateError) && count() == 3, "Không lưu thời điểm quá khứ");
        ui(() -> ((android.widget.ScrollView) ((android.view.ViewGroup) add.findViewById(android.R.id.content)).getChildAt(0)).fullScroll(View.FOCUS_DOWN));
        screenshot("08-add-past");
        selected = Calendar.getInstance();
        setDateTime(add, selected);
        click(add, R.id.btnSaveAppointment);
        check(visible(add, R.id.tvDateError) && count() == 3, "Không lưu phút hiện tại");
        selected.add(Calendar.DAY_OF_MONTH, 2);
        setDateTime(add, selected);
        click(add, R.id.btnSaveAppointment);
        check(add.isFinishing() || add.isDestroyed(), "Lưu lịch tương lai và đóng màn hình thêm");
        waitForIdleSync();
        check(awaitText(main, R.id.tvTotal, "4") && awaitText(main, R.id.tvReminders, "3"), "Danh sách và thống kê tự cập nhật sau thêm");
        int id;
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) {
            ArrayList<Appointment> appointments = db.getAllAppointments();
            id = appointments.get(appointments.size() - 1).id;
            check("Lịch kiểm thử".equals(db.getAppointmentById(id).title), "Lịch mới được lưu và nằm đúng vị trí");
        }
        ui(() -> ((ListView) main.findViewById(R.id.lvAppointments)).setSelection(3));
        screenshot("09-after-add");
        ActivityMonitor detailMonitor = addMonitor(AppointmentDetailActivity.class.getName(), null, false);
        ui(() -> {
            ListView list = main.findViewById(R.id.lvAppointments);
            list.performItemClick(null, list.getCount() - 1, id);
        });
        Activity detail = waitForMonitorWithTimeout(detailMonitor, 5000);
        removeMonitor(detailMonitor);
        check(detail != null && "Lịch kiểm thử".equals(text(detail, R.id.tvDetailTitle)), "Nhấn dòng mở chi tiết đúng id");
        screenshot("04-detail");
        ui(() -> ((Switch) detail.findViewById(R.id.swDetailReminder)).setChecked(false));
        check("Đang tắt".equals(text(detail, R.id.tvReminderStatus)), "Switch đổi nhãn trạng thái");
        screenshot("10-reminder-off");
        click(detail, R.id.btnBack);
        check(awaitText(main, R.id.tvReminders, "2"), "Thống kê cập nhật khi quay lại sau đổi nhắc nhở");
        Activity deleteDetail = launch(AppointmentDetailActivity.class, id);
        click(deleteDetail, R.id.btnDeleteAppointment);
        screenshot("11-confirm-delete");
        long deleteDeadline = android.os.SystemClock.uptimeMillis() + 5000;
        boolean deleted;
        do {
            getUiAutomation().waitForIdle(300, 4000);
            deleted = clickText(getUiAutomation().getRootInActiveWindow(), "Xóa");
            if (!deleted) android.os.SystemClock.sleep(100);
        } while (!deleted && android.os.SystemClock.uptimeMillis() < deleteDeadline);
        check(deleted, "Hộp xác nhận xóa xuất hiện và thao tác được");
        waitForIdleSync();
        check(count() == 3, "Xóa lịch khỏi SQLite");
        check(awaitText(main, R.id.tvTotal, "3"), "Thống kê cập nhật sau xóa lịch");
        screenshot("12-after-delete");
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) {
            for (Appointment appointment : db.getAllAppointments()) db.deleteAppointment(appointment.id);
        }
        ui(main::finish);
        Activity emptyMain = launch(MainActivity.class, -1);
        check(visible(emptyMain, R.id.tvEmpty) && "0".equals(text(emptyMain, R.id.tvTotal))
                && "0".equals(text(emptyMain, R.id.tvReminders)), "Danh sách rỗng và thống kê 0/0");
        screenshot("05-empty");
        ui(emptyMain::finish);
        Activity invalid = launch(AppointmentDetailActivity.class, -1);
        check(invalid.isFinishing() || invalid.isDestroyed(), "Chi tiết thiếu id đóng an toàn");
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) {
            db.insertAppointment(new Appointment(0, "Persistence check", "SQLite", "2099-12-31", "10:00", true));
            db.insertAppointment(new Appointment(0, "Deleted check", "SQLite", "2099-12-31", "11:00", true));
            ArrayList<Appointment> appointments = db.getAllAppointments();
            db.updateReminder(appointments.get(0).id, false);
            db.deleteAppointment(appointments.get(1).id);
        }
        pass("Đã chuẩn bị dữ liệu để kiểm tra qua hai tiến trình riêng biệt");
    }

    private void verifyRestart() {
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) {
            check(db.countAll() == 1 && db.countReminders() == 0, "Khởi động tiến trình mới giữ thao tác thêm, sửa và xóa");
            check("Persistence check".equals(db.getAllAppointments().get(0).title), "Không tự chèn lại dữ liệu mẫu");
        }
        getTargetContext().deleteDatabase("appointments.db");
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) {
            check(db.countAll() == 3 && db.countReminders() == 2, "Khôi phục dữ liệu mẫu cho bản chạy thử");
        }
        Activity login = launch(LoginActivity.class, -1);
        check(login != null, "Mở lại ứng dụng tại màn hình đăng nhập");
    }

    private Activity launch(Class<? extends Activity> type, int id) {
        Intent intent = new Intent(getTargetContext(), type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (id >= 0) intent.putExtra("id", id);
        Activity activity = startActivitySync(intent);
        waitForIdleSync();
        return activity;
    }

    private Activity transition(Activity from, int button, Class<? extends Activity> type) {
        ActivityMonitor monitor = addMonitor(type.getName(), null, false);
        click(from, button);
        Activity activity = waitForMonitorWithTimeout(monitor, 5000);
        removeMonitor(monitor);
        check(activity != null, "Chuyển sang " + type.getSimpleName());
        waitForIdleSync();
        return activity;
    }

    private void setDateTime(Activity activity, Calendar calendar) {
        ui(() -> {
            DatePicker date = activity.findViewById(R.id.datePicker);
            date.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            TimePicker time = activity.findViewById(R.id.timePicker);
            time.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            time.setMinute(calendar.get(Calendar.MINUTE));
        });
    }

    private int count() {
        try (DatabaseHelper db = new DatabaseHelper(getTargetContext())) { return db.countAll(); }
    }

    private void click(Activity activity, int id) { ui(() -> activity.findViewById(id).performClick()); }
    private void edit(Activity activity, int id, String value) { ui(() -> ((EditText) activity.findViewById(id)).setText(value)); }

    private String text(Activity activity, int id) {
        String[] value = new String[1];
        ui(() -> value[0] = ((TextView) activity.findViewById(id)).getText().toString());
        return value[0];
    }

    private boolean awaitText(Activity activity, int id, String expected) {
        long deadline = android.os.SystemClock.uptimeMillis() + 5000;
        do {
            if (expected.equals(text(activity, id))) return true;
            android.os.SystemClock.sleep(50);
        } while (android.os.SystemClock.uptimeMillis() < deadline);
        return false;
    }

    private boolean visible(Activity activity, int id) {
        boolean[] value = new boolean[1];
        ui(() -> value[0] = activity.findViewById(id).getVisibility() == View.VISIBLE);
        return value[0];
    }

    private void ui(Runnable action) {
        runOnMainSync(action);
        waitForIdleSync();
    }

    private boolean clickText(AccessibilityNodeInfo node, String text) {
        if (node == null) return false;
        if (text.equalsIgnoreCase(node.getText() == null ? "" : node.getText().toString()) && node.isClickable())
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        for (int i = 0; i < node.getChildCount(); i++) if (clickText(node.getChild(i), text)) return true;
        return false;
    }

    private void screenshot(String name) throws Exception {
        getUiAutomation().waitForIdle(300, 4000);
        android.os.SystemClock.sleep(700);
        Bitmap bitmap = getUiAutomation().takeScreenshot();
        if (bitmap == null) throw new AssertionError("Không chụp được giao diện");
        File directory = new File(getTargetContext().getExternalFilesDir(null), "test-screenshots");
        directory.mkdirs();
        try (FileOutputStream stream = new FileOutputStream(new File(directory, name + ".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        }
        bitmap.recycle();
    }

    private void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        pass(name);
    }

    private void pass(String name) {
        results.append("PASS: ").append(name).append('\n');
        Bundle status = new Bundle();
        status.putString("stream", "PASS: " + name + "\n");
        sendStatus(0, status);
    }
}
