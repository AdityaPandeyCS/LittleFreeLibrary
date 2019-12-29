package com.periphery.littlefreelibrary;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.seismic.ShakeDetector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.SENSOR_SERVICE;



public class Tab1Fragment extends Fragment implements ShakeDetector.Listener {

    public static ArrayList<Charter> charters = new ArrayList<>();
    public static double current_long;
    public static double current_lat;
    public Context mContext;
    public LinearLayout charterLayout;
    public static ArrayList<String> genres = new ArrayList<>();
    private SensorManager sensorManager;
    private ShakeDetector sd;
    View view;
    public static final String database_url = "jdbc:postgresql://periphery.ctqxvyj4v2ke.us-east-1.rds.amazonaws.com:5432/postgres";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);


        view = inflater.inflate(R.layout.tab1_fragment, container, false);
        mContext = getContext();
        sd = new ShakeDetector(this);
        setGenres();

        LocationManager mgr =
                (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                updatePage(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };


        try {
            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
            Location location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            current_lat = location.getLatitude();
            current_long = location.getLongitude();
            Log.d("location", "success");
        } catch (SecurityException e) {
            Log.d("location", "error");
            current_lat = 40.1138069; //siebel
            current_long = -88.2270939;
        } catch (NullPointerException n) {
            Log.d("location no fix", n.getMessage());
            current_lat = 40.1138069; //siebel
            current_long = -88.2270939;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String strlat = "" + current_lat;
        String strlong = "" + current_long;
        String SQL = String.format("SELECT *, 2 * 3961 * ASIN(SQRT((SIN(RADIANS((lat - %1$s) / 2))) ^ 2 + COS(RADIANS(%1$s)) *" +
                " COS(RADIANS(lat)) * (SIN(RADIANS((lon - %2$s) / 2))) ^ 2)) as distance\n" +
                "FROM Charters\n" +
                "ORDER BY distance ASC\n" +
                "LIMIT 50;", strlat, strlong);
        try {
            conn = DriverManager.getConnection(database_url, username, password);
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
                Charter temp = new Charter(cid, street, city + ", " + state + " " + zipcode, num, stewname, email, lat, lon, current_lat,
                        current_long, image, description);
                charters.add(temp);
            }
            conn.close();
        } catch (SQLException e) {
            Log.d("MyApp", "sql " + e.getMessage());
        }

        charterLayout = view.findViewById(R.id.charter_items);
        //Build the list of charters with the layout inflater
        for (int i = 0; i < charters.size(); i++) {
            View charterView = LayoutInflater.from(getActivity()).inflate(R.layout.charter_item, null);

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
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menubar_mainactivity, menu);
        //load the initial state of the icon
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //back button at the top
                return true;
            case R.id.action_search:
                //search books
                Intent intent = new Intent(Tab1Fragment.this.getActivity(), SearchActivity.class);
                startActivity(intent);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
        sd.setSensitivity(11);
        sd.start(sensorManager);
    }

    @Override
    public void onPause() {
        super.onPause();
        sd.stop();
    }

    public void hearShake() {
        Toast.makeText(mContext, "Nearby locations updated", Toast.LENGTH_SHORT).show();
        LocationManager mgr =
                (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            updatePage(location);
        } catch (SecurityException e) {
            Log.d("refresh", "error");
        }
    }


    public void updatePage(Location location) {


        if (location != null) {
            current_lat = location.getLatitude();
            current_long = location.getLongitude();
            for (int i = 0; i < charters.size(); i++) {
                charters.get(i).setDistanceDoubleandString(current_lat, current_long);
            }
        }

        //resort
        Collections.sort(charters, new Comparator<Charter>() {
            @Override
            public int compare(final Charter lhs, Charter rhs) {
                if (rhs.distanceDouble < lhs.distanceDouble)
                    return 1;
                if (rhs.distanceDouble > lhs.distanceDouble)
                    return -1;
                return 0;
            }
        });
    }

    /* Get an array list of all of the genres */
    private void setGenres() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL = String.format("SELECT DISTINCT genre FROM Books;");
        try {
            conn = DriverManager.getConnection(database_url, username, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL);
            genres.clear();
            while (rs.next()) {
                String genre = rs.getString("genre");
                genres.add(genre);
            }
            conn.close();
        } catch (SQLException e) {
            Log.d("MyApp", "sql " + e.getMessage());
        }
    }

}