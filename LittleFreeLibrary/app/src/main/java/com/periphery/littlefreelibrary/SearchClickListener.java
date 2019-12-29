package com.periphery.littlefreelibrary;

import android.content.Intent;
import android.view.View;

public class SearchClickListener implements View.OnClickListener {
    private String bookIndex;

    public SearchClickListener(String bookIndex) {
        this.bookIndex = bookIndex;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), BookLocationActivity.class);
        intent.putExtra("book_id", this.bookIndex);
        v.getContext().startActivity(intent);
    }
}