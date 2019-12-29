package com.periphery.littlefreelibrary;

import android.content.Intent;
import android.view.View;

public class BookClickListener implements View.OnClickListener {
    private String cid;
    private int bookIndex;

    public BookClickListener(int bookIndex, String cid) {
        this.bookIndex = bookIndex;
        this.cid = cid;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), BookPage.class);
        intent.putExtra("book index", this.bookIndex);
        intent.putExtra("cid", this.cid);
        v.getContext().startActivity(intent);
    }
}