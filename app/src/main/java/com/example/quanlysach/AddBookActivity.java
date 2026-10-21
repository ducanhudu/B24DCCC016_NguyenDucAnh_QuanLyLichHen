package com.example.quanlysach;

import android.app.Activity; import android.os.Bundle; import android.widget.*; import android.view.View; import java.util.Arrays;

public class AddBookActivity extends Activity {
    private EditText title, author, note; private Spinner category; private RadioGroup status; private RatingBar rating;
    @Override public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_add_book); title=findViewById(R.id.etTitle);author=findViewById(R.id.etAuthor);note=findViewById(R.id.etNote);category=findViewById(R.id.spCategory);status=findViewById(R.id.rgStatus);rating=findViewById(R.id.ratingBook); category.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Arrays.asList("Văn học","Khoa học","Công nghệ","Kỹ năng sống","Khác"))); findViewById(R.id.btnSaveBook).setOnClickListener(v->save()); }
    private void save(){boolean ok=true; if(title.getText().toString().trim().isEmpty()){title.setError("Vui lòng nhập tên sách");ok=false;} if(author.getText().toString().trim().isEmpty()){author.setError("Vui lòng nhập tác giả");ok=false;} if(!ok)return; String s="Chưa đọc"; int id=status.getCheckedRadioButtonId(); if(id==R.id.rbReading)s="Đang đọc"; else if(id==R.id.rbRead)s="Đã đọc"; BookRepository.BOOKS.add(new Book(title.getText().toString().trim(),author.getText().toString().trim(),category.getSelectedItem().toString(),s,rating.getRating(),note.getText().toString().trim())); Toast.makeText(this,"Đã thêm sách",Toast.LENGTH_SHORT).show(); finish(); }
}
