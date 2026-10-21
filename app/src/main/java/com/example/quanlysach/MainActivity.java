package com.example.quanlysach;

import android.app.Activity; import android.content.Intent; import android.os.Bundle; import android.view.View; import android.view.ViewGroup; import android.widget.*; import java.util.Locale;

public class MainActivity extends Activity {
    private TextView stats; private BookAdapter adapter;
    @Override public void onCreate(Bundle b) { super.onCreate(b); setContentView(R.layout.activity_main); stats=findViewById(R.id.tvStats); ListView list=findViewById(R.id.lvBooks); adapter=new BookAdapter(); list.setAdapter(adapter); list.setOnItemClickListener((parent, view, position, id) -> { Intent i=new Intent(this, BookDetailActivity.class); i.putExtra("index", position); startActivity(i); }); findViewById(R.id.btnAddBook).setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class))); refresh(); }
    @Override protected void onResume() { super.onResume(); if (adapter != null) refresh(); }
    private void refresh() { int read=0; for (Book x:BookRepository.BOOKS) if (x.status.equals("Đã đọc")) read++; int unfinished=BookRepository.BOOKS.size()-read; stats.setText(String.format(Locale.getDefault(), "TỔNG QUAN\n%d cuốn sách     •     %d đã đọc     •     %d chưa đọc xong", BookRepository.BOOKS.size(), read, unfinished)); adapter.notifyDataSetChanged(); }
    private class BookAdapter extends BaseAdapter { public int getCount(){return BookRepository.BOOKS.size();} public Object getItem(int p){return BookRepository.BOOKS.get(p);} public long getItemId(int p){return p;} public View getView(int p, View v, ViewGroup parent){ if(v==null)v=getLayoutInflater().inflate(R.layout.item_book,parent,false); Book x=BookRepository.BOOKS.get(p); ((TextView)v.findViewById(R.id.tvBookTitle)).setText(x.title); ((TextView)v.findViewById(R.id.tvBookMeta)).setText(x.author+"  •  "+x.category+"  •  "+stars(x.rating)); ((TextView)v.findViewById(R.id.tvBookStatus)).setText("●  "+x.status); return v; } private String stars(float r){StringBuilder s=new StringBuilder(); for(int i=0;i<5;i++)s.append(i<r?"★":"☆"); return s.toString();} }
}
