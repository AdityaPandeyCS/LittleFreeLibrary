package com.periphery.littlefreelibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class BookPage extends AppCompatActivity implements View.OnClickListener {
    public Charter charter;
    public int bookIndex;
    public Book book;
    private String cid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        setContentView(R.layout.activity_book_page);

        Intent mIntent = getIntent();
        bookIndex = mIntent.getIntExtra("book index", 0);
        cid = mIntent.getExtras().getString("cid", " ");
        book = CatalogPage.books.get(bookIndex);

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
        ((TextView) findViewById(R.id.book_desc)).setText(book.getDescription());
        if (book.getNumCopies() > 1) {
            ((TextView) findViewById(R.id.numCopies)).setText(book.getNumCopies() + " copies");
        }
        else {
            ((TextView) findViewById(R.id.numCopies)).setText("1 copy");
        }


        final MaterialButton button = (MaterialButton) findViewById(R.id.checkoutbutton);
        final Context context = this;
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (view.getId() == R.id.checkoutbutton) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    String message = "Are you sure you want to take \"" + book.getTitle() + "\"?";
                    builder1.setMessage(message);
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "YES",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    String title = book.getTitle();
                                    if(removeBook(cid,book.getBook_id())) {
                                        String text = "You have successfully taken \"" + title + "\"";
                                        int duration = Toast.LENGTH_SHORT;
                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                    else{
                                        String text = "An error occured while trying to take \"" + title + "\"";
                                        int duration = Toast.LENGTH_SHORT;
                                        Toast toast = Toast.makeText(context, text, duration);
                                        toast.show();
                                    }
                                    finish();
                                }
                            });

                    builder1.setNegativeButton(
                            "NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            }
        });
        final MaterialButton infoButton = (MaterialButton) findViewById(R.id.moreinfobutton);
        infoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (view.getId() == R.id.moreinfobutton) {
                    //TextView v = (TextView) view;
                    String address = book.title;
                    address.replace(" ", "%20");
                    String url = "https://www.google.com/search?q=" + address;
                    Uri uriUrl = Uri.parse(url);
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                    startActivity(launchBrowser);
                }
            }
        });

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menubar_bookpage, menu);

        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL1 = String.format("SELECT description FROM books WHERE book_id = '%s';", book.getBook_id()); // escape '?
        Log.d("sql", SQL1);
        String newdesc = book.getDescription();
        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL1);
            rs.next();
            newdesc = rs.getString("description");
            Log.d("sql", "newdesc = " + newdesc);
        } catch (SQLException e) {
            Log.d("MyApp", "sql " + e.getMessage());
        }
        book.setDescription(newdesc);
        ((TextView) findViewById(R.id.book_desc)).setText(book.getDescription());
    }

    private boolean removeBook(String cid, String book_id) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL = "DELETE FROM have WHERE cid = '" + cid + "' AND book_id = '" + book_id + "';";
        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            conn.close();
        } catch (SQLException e) {
            Log.d("MyApp", "sql Delete " + e.getMessage() + cid + book_id); //Apparently all is good despite it returning an error.
            CatalogPage.update = true;
            return true;
        }
        book.setNumCopies(book.getNumCopies() - 1);
        CatalogPage.update = true;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back button at the top
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, UpdateBook.class);
            intent.putExtra("book", this.book);
            this.startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
    }
}
