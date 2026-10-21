package com.example.quanlysach;

import java.util.ArrayList;

public final class BookRepository {
    public static final ArrayList<Book> BOOKS = new ArrayList<>();
    static { resetSamples(); }
    private BookRepository() {}
    public static void resetSamples() { if (!BOOKS.isEmpty()) return; BOOKS.add(new Book("Lập trình Android cơ bản", "Nguyễn Văn A", "Công nghệ", "Đang đọc", 4, "")); BOOKS.add(new Book("Đắc nhân tâm", "Dale Carnegie", "Kỹ năng sống", "Đã đọc", 5, "")); BOOKS.add(new Book("Dế Mèn phiêu lưu ký", "Tô Hoài", "Văn học", "Chưa đọc", 0, "")); }
}
