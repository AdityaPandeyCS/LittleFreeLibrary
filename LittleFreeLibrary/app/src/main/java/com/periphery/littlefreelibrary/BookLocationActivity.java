package com.periphery.littlefreelibrary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class BookLocationActivity extends AppCompatActivity {

    public static String book_id = "";
    public static ArrayList<Charter> charters = new ArrayList<>();
    public LinearLayout charterLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_location);
        charterLayout = findViewById(R.id.search_charter_items);
        getSupportActionBar().setTitle("Charters with your book");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));

        book_id = getIntent().getStringExtra("book_id");
        getData();


        //Build the list of charters with the layout inflater
        for (int i = 0; i < charters.size(); i++) {
            View charterView = LayoutInflater.from(this).inflate(R.layout.charter_item, null);

            charterView.setOnClickListener(new CharterClickListener(charters.get(i)));

            TextView bookTitleView = charterView.findViewById(R.id.charterNameView);
            TextView authorView = charterView.findViewById(R.id.charterNumberView);
            ImageView iconView = charterView.findViewById(R.id.iconView);
            TextView distanceView = charterView.findViewById(R.id.distance);
            Charter charter = charters.get(i);
            bookTitleView.setText(charter.getAddress());
            authorView.setText(charter.getNumber());
            RequestOptions myOptions = new RequestOptions().override(50, 50);
            Glide.with(this).asBitmap().apply(myOptions).load(charter.getImageURL()).diskCacheStrategy(DiskCacheStrategy.ALL).into(iconView);
            distanceView.setText(charter.getDistance());

            charterLayout.addView(charterView);
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back button at the top
            finish();
            return true;
        }
        return true;
    }

    private void getData() {
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL = "SELECT *\n" +
                "FROM charters c JOIN have h ON(c.cid = h.cid) JOIN books b ON(h.book_id = b.book_id)\n" +
                "WHERE b.book_id = '" + book_id + "'\n" +
                "ORDER BY 2 * 3961 * ASIN(SQRT((SIN(RADIANS((c.lat - 40.113799) / 2))) ^ 2 + COS(RADIANS(40.113799)) * COS(RADIANS(lat)) * " +
                "(SIN(RADIANS((c.lon - -88.225202) / 2))) ^ 2))\n" +
                "LIMIT 10";
        Log.d("sql", SQL);
        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            charters.clear();
            while (rs.next()) {
                String street = rs.getString("street");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String zipcode = rs.getString("zipcode");
                String num = rs.getString("num");
                String stewname = rs.getString("stewname");
                String email = rs.getString("email");
                String image = rs.getString("image");
                String description = rs.getString("story");
                Double lat = rs.getDouble("lat");
                Double lon = rs.getDouble("lon");
                String cid = rs.getString("cid");
                Charter temp = new Charter(cid, street, city + ", " + state + " " + zipcode, num, stewname, email, lat, lon, Tab1Fragment.current_lat,Tab1Fragment.current_long, image, description);
                temp.setFavorite(true);
                charters.add(temp);
            }
            conn.close();
//            conn.close();
        } catch (SQLException e) {
            Log.d("sql", e.getMessage());
        }
    }
}
