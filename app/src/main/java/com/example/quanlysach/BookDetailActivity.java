package com.example.quanlysach;

import android.app.Activity; import android.app.AlertDialog; import android.os.Bundle; import android.view.View; import android.widget.*;

public class BookDetailActivity extends Activity {
    private Book book; private int index; private TextView status; private RatingBar rating; private Button mark;
    @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_book_detail); index=getIntent().getIntExtra("index",-1); if(index<0||index>=BookRepository.BOOKS.size()){finish();return;} book=BookRepository.BOOKS.get(index); TextView title=findViewById(R.id.tvDetailTitle), meta=findViewById(R.id.tvDetailAuthorCategory), note=findViewById(R.id.tvDetailNote); status=findViewById(R.id.tvDetailStatus); rating=findViewById(R.id.ratingDetail); mark=findViewById(R.id.btnMarkRead); title.setText(book.title);meta.setText(book.author+"  •  "+book.category);note.setText(book.note.isEmpty()?"Chưa có ghi chú":book.note); rating.setRating(book.rating); rating.setOnRatingBarChangeListener((bar,r,from)->book.rating=r); mark.setOnClickListener(v->{book.status="Đã đọc"; render(); Toast.makeText(this,"Đã cập nhật tình trạng",Toast.LENGTH_SHORT).show();}); findViewById(R.id.btnBack).setOnClickListener(v->finish()); findViewById(R.id.btnDelete).setOnClickListener(v->confirmDelete()); render(); }
    private void render(){status.setText("Tình trạng: "+book.status); boolean done=book.status.equals("Đã đọc");mark.setVisibility(done?View.GONE:View.VISIBLE);}
    private void confirmDelete(){new AlertDialog.Builder(this).setTitle("Xóa sách?").setMessage("Bạn có chắc muốn xóa cuốn sách này không?").setNegativeButton("Hủy",null).setPositiveButton("Xóa",(d,w)->{BookRepository.BOOKS.remove(index);finish();}).show();}
}
