package com.periphery.littlefreelibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.seismic.ShakeDetector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CatalogPage extends AppCompatActivity implements View.OnClickListener{
    public static ArrayList<Book> books = new ArrayList<>();
    public int charterIndex;
    public boolean fromFavorites;
    public static String cid;
    LinearLayout catalogLayout;
    Button submit;
    Button clear;
    public String filterGenre;
    private PopupWindow mPopupWindow;
    private SensorManager sensorManager;
    private ShakeDetector sd;
    private Spinner spinner;
    public static boolean update = true;
    public static ArrayList<String> genres = new ArrayList<>();
    public static ArrayList<String> genresAndCounts = new ArrayList<>();
    public static ArrayList<Integer> counts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_page);
        update = true;

        Intent mIntent = getIntent();
        charterIndex = mIntent.getIntExtra("charter index", 0);
        fromFavorites = mIntent.getBooleanExtra("favorites", false);
        cid = mIntent.getStringExtra("cid");
        catalogLayout = findViewById(R.id.catalog_items);

        /*Rename and add the back navigation button*/
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));

        getSupportActionBar().setTitle("Catalog");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FloatingActionButton fabView = findViewById(R.id.fab);
        fabView.setOnClickListener(this);
        filterGenre = "All";
        onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //menu for the filters
        inflater.inflate(R.menu.menubar_catalog_page, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            //back button at the top
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_filter) {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

            // Inflate the custom layout/view
            View customView = inflater.inflate(R.layout.filter_window, null);

            // Initialize a new instance of popup window
            mPopupWindow = new PopupWindow(
                    customView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            // Finally, show the popup window at the center location of root relative layout
            mPopupWindow.showAtLocation(findViewById(android.R.id.content).getRootView(), Gravity.CENTER, 0, 0);
            View curr = mPopupWindow.getContentView();

            spinner = (Spinner) curr.findViewById(R.id.genre);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genresAndCounts);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            submit = (Button) curr.findViewById(R.id.submit);
            clear = (Button) curr.findViewById(R.id.clear);


            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    catalogLayout.removeAllViews();
                    mPopupWindow.dismiss();
                    updatePage();

                }

            });

            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String selectedGenre = spinner.getSelectedItem().toString();

                        for(String genre : genres) {
                            if (selectedGenre.contains(genre)) {
                                selectedGenre = genre;
                                break;
                            }
                        }
                    Toast.makeText(getApplicationContext(), selectedGenre, Toast.LENGTH_SHORT).show();
                    filterGenre = selectedGenre;
                    filterData();
                    mPopupWindow.dismiss();
                    updatePage();

                }
            });

        }
        return false;
    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab) {
            Intent intent = new Intent(this, CheckIn.class);
            intent.putExtra("charter index", charterIndex);
            intent.putExtra("favorites", fromFavorites);
            intent.putExtra("cid", cid);
            startActivity(intent);
        }

    }



    private void filterData() {
        if(filterGenre == "All"){
            getData();
            return;
        }
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL = "SELECT * FROM books JOIN have ON books.book_id=have.book_id WHERE genre = '" + filterGenre + "' AND cid = '" + cid + "' order by title;";
        Log.d("sql", SQL);
        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            books.clear();
            while (rs.next()) {
                String book_id = rs.getString("book_id");
                String image = rs.getString("image");
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
    }

    private void getData() {
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL = "SELECT * FROM books JOIN have ON books.book_id=have.book_id WHERE cid = '" + cid + "' order by title;";
        Log.d("sql", SQL);
        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            books.clear();
            while (rs.next()) {
                String book_id = rs.getString("book_id");
                String image = rs.getString("image");
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
    }
    public static void getGenreCounts(){
        CatalogPage.counts.clear();
        CatalogPage.genres.clear();
        CatalogPage.genresAndCounts.clear();
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL = "SELECT books.genre, COUNT(*) as cnt FROM books NATURAL JOIN have WHERE cid = '" + CatalogPage.cid + "' GROUP BY genre ORDER BY cnt DESC, books.genre ASC;";
        Log.d("sql", SQL);
        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            while (rs.next()) {
                Integer count = rs.getInt("cnt");
                String genre = rs.getString("genre");
                CatalogPage.counts.add(count);
                CatalogPage.genres.add(genre);
                CatalogPage.genresAndCounts.add(genre + " (" + Integer.toString(count)+")");
            }
        } catch (SQLException e) {
            Log.d("sql", e.getMessage());
        }
        int sum = 0;
        for(Integer d : CatalogPage.counts)
            sum += d;
        CatalogPage.genres.add("All");
        CatalogPage.genresAndCounts.add(0,"All (" + sum + ")");
    }

    public void updatePage(){
        catalogLayout.removeAllViews();
        //Refresh your stuff here
        filterData();
        for (int i = 0; i < books.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.catalog_item, null);

            view.setOnClickListener(new BookClickListener(i, cid));

            TextView bookTitleView = view.findViewById(R.id.bookTitleView);
            TextView authorView = view.findViewById(R.id.authorView);
            ImageView iconView = view.findViewById(R.id.iconView2);

            Book book = books.get(i);
            bookTitleView.setText(book.getTitle());
            authorView.setText(book.getAuthor());
            String imageString = book.getImage();
            if (imageString.substring(0, 7).equals("http://")) {
                RequestOptions myOptions = new RequestOptions().override(50, 50);
                Glide.with(this).asBitmap().apply(myOptions).load(book.getImage()).into(iconView);
            } else {
                byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                iconView.setImageBitmap(decodedByte);
            }

            catalogLayout.addView(view);
        }
    }
    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
        if(update){
            update = false;
            getGenreCounts();
            updatePage();
        }

    }

    public void onPause() {
        super.onPause();
    }
}