package com.periphery.littlefreelibrary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    public static ArrayList<Book> books = new ArrayList<>();
    public LinearLayout searchCatalog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);
        /*Rename and add the back navigation button*/
        searchCatalog = findViewById(R.id.search_book_items);
        getSupportActionBar().setTitle("Search");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));

        EditText myTextBox = (EditText) findViewById(R.id.searchInput);
        myTextBox.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {

                getData("" + s);
            }
        });

    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back button at the top
            finish();
            return true;
        }
        return true;
    }
    private void getData(String input) {
        if (input.length() <= 2) {
            //Show a button?
            searchCatalog.removeAllViews();
            return;
        }
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL = "SELECT b.book_id, b.image as bookimage, b.title, b.author,b.genre, b.description, h.numcopies\n" +
                "FROM charters c JOIN have h ON(c.cid = h.cid) JOIN books b ON(h.book_id = b.book_id)\n" +
                "WHERE LOWER(b.title) LIKE '%" + input.toLowerCase() + "%' OR LOWER(b.author) LIKE '%" + input.toLowerCase() + "%'\n" +
                "ORDER BY 2 * 3961 * ASIN(SQRT((SIN(RADIANS((c.lat - 40.113799) / 2))) ^ 2 + COS(RADIANS(40.113799)) *" +
                " COS(RADIANS(lat)) * (SIN(RADIANS((c.lon - -88.225202) / 2))) ^ 2))\n" +
                "LIMIT 20";
        Log.d("sql", SQL);
        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            books.clear();
            while (rs.next()) {
                String book_id = rs.getString("book_id");
                String image = rs.getString("bookimage");
                Log.d("rahbert2", image);
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                String description = rs.getString("description");
                int numCopies = rs.getInt("numcopies");
                Log.d("description", "desc = " + description);
                Book temp = new Book(book_id, image, title, author, genre, description, numCopies);
                books.add(temp);
            }
//            conn.close();
        } catch (SQLException e) {
            Log.d("sql", e.getMessage());
        }
        searchCatalog.removeAllViews();
        for (int i = 0; i < books.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.catalog_item, null);

            view.setOnClickListener(new SearchClickListener(books.get(i).book_id));

            TextView bookTitleView = view.findViewById(R.id.bookTitleView);
            TextView authorView = view.findViewById(R.id.authorView);
            ImageView iconView = view.findViewById(R.id.iconView2);

            Book book = books.get(i);
            bookTitleView.setText(book.getTitle());
            authorView.setText(book.getAuthor());
            String imageString = book.getImage();
//            Log.d("rahbert", imageString.substring(0,10));
            if (imageString.substring(0, 7).equals("http://")) {
                RequestOptions myOptions = new RequestOptions().override(50, 50);
                Glide.with(this).asBitmap().apply(myOptions).load(book.getImage()).into(iconView);
            } else {
                byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                iconView.setImageBitmap(decodedByte);
            }

            searchCatalog.addView(view);
        }
    }
}
