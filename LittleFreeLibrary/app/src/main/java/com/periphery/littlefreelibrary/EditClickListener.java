package com.periphery.littlefreelibrary;

import android.content.Intent;
import android.util.Log;
import android.view.View;

public class EditClickListener implements View.OnClickListener {
    private Book book;

    public EditClickListener(Book book) {
        this.book = book;
        Log.d("updatebook - constructor", this.book.toString());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), UpdateBook.class);
//        intent.putExtra("book_id", this.book.getBook_id());
//        intent.putExtra("image", this.book.getImage());
//        intent.putExtra("title", this.book.getTitle());
//        intent.putExtra("author", this.book.getAuthor());
//        intent.putExtra("genre", this.book.getGenre());
//        intent.putExtra("description", this.book.getDescription());
        intent.putExtra("book", this.book);
        Log.d("updatebook - what the fuck", this.book.toString());
        v.getContext().startActivity(intent);
    }
}