package com.periphery.littlefreelibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class UpdateBook extends AppCompatActivity implements View.OnClickListener {
    public Book book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_book);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        Intent mIntent = getIntent();


        book = (Book) mIntent.getSerializableExtra("book");
        Log.d("updatebook - oncreate", book.toString());
        ImageView book_image = (ImageView) findViewById(R.id.book_image);
        View view = LayoutInflater.from(this).inflate(R.layout.activity_book_page, null);
        String imageString = book.getImage();
        if (imageString.substring(0, 7).equals("http://")) {
            Glide.with(this).load(book.getImage()).into(book_image);
        }
        else {
            byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            book_image.setImageBitmap(decodedByte);
        }
        LinearLayout booklayout = findViewById(R.id.book_page);
        getSupportActionBar().setTitle(book.getTitle());

        ((TextView) findViewById(R.id.book_genre)).setText("#" + book.getGenre());
        ((TextView) findViewById(R.id.book_title)).setText(book.getTitle());
        ((TextView) findViewById(R.id.book_author)).setText("by " + book.getAuthor());
        TextInputEditText desc = (TextInputEditText) findViewById(R.id.descinput);
        desc.setText(book.getDescription());
        MaterialButton submit = (MaterialButton) findViewById(R.id.submit);
        submit.setOnClickListener(this);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back button at the top
            finish();
            return true;
        }
        return false;
    }
    public void onClick(View view) {
        if (view.getId() == R.id.submit) {
            TextInputEditText desc = (TextInputEditText) findViewById(R.id.descinput);
            String description = desc.getText().toString();

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            final String username = "periphery";
            final String password = "PeripheryLFL";
            Connection conn = null;
            String SQL1 = String.format("UPDATE books SET description='%s' WHERE book_id = '%s';", description, book.getBook_id()); // escape '?
            Log.d("sql", SQL1);
            try {
                conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
                Statement stmt = conn.createStatement();
                stmt.executeQuery(SQL1);
            } catch (SQLException e) {
                Log.d("MyApp", "sql " + e.getMessage());
            }
            book.setDescription(description);
            finish();
        }
    }
}
