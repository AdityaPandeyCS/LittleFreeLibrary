package com.periphery.littlefreelibrary;

import android.content.Context;
import android.content.Intent;
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
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.seismic.ShakeDetector;

import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.SENSOR_SERVICE;
import static com.periphery.littlefreelibrary.Tab1Fragment.charters;

public class Tab2Fragment extends Fragment implements ShakeDetector.Listener {

    public static ArrayList<Charter> favoriteCharters = new ArrayList<>();
    public static TextView hint;
    public Context mContext;
    public LinearLayout favoritesLayout;
    View view;
    static FirebaseFirestore db;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.tab2_fragment, container, false);
        mContext = getContext();

        hint = (TextView) view.findViewById(R.id.hint);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        favoriteCharters.clear();
        getCharters();

        return view;
    }

    public void onResume() {
        if (favoriteCharters.size() == 0){
            hint.setVisibility(View.VISIBLE);
        }
        else {
            hint.setVisibility(View.INVISIBLE);
        }
        super.onResume();
        favoritesLayout = view.findViewById(R.id.favorite_items);
        favoritesLayout.removeAllViews();
        for (int i = 0; i < favoriteCharters.size(); i++) {
            Charter temp_charter = favoriteCharters.get(i);
            if (!temp_charter.favorite)
                continue;
            String cid = favoriteCharters.get(i).cid;
            View charterView = LayoutInflater.from(getActivity()).inflate(R.layout.charter_item, null);
            charterView.setOnClickListener(new CharterClickListener(favoriteCharters.get(i)));

            TextView bookTitleView = charterView.findViewById(R.id.charterNameView);
            TextView authorView = charterView.findViewById(R.id.charterNumberView);
            ImageView iconView = charterView.findViewById(R.id.iconView);
            TextView distanceView = charterView.findViewById(R.id.distance);
            Charter charter = favoriteCharters.get(i);
            bookTitleView.setText(charter.getAddress());
            authorView.setText(charter.getNumber());
            RequestOptions myOptions = new RequestOptions().override(50, 50);
            Glide.with(this).asBitmap().apply(myOptions).load(charter.getImageURL()).into(iconView);
            distanceView.setText(charter.getDistance());

            favoritesLayout.addView(charterView);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void hearShake() {
        Toast.makeText(mContext, "Nearby locations updated", Toast.LENGTH_SHORT).show();
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
                Intent intent = new Intent(Tab2Fragment.this.getActivity(), SearchActivity.class);
                startActivity(intent);
                return true;
        }

        return true;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        View view = getView();

        if (isVisibleToUser) {
            favoritesLayout = view.findViewById(R.id.favorite_items);
            favoritesLayout.removeAllViews();
            for (int i = 0; i < favoriteCharters.size(); i++) {
                Charter temp_charter = favoriteCharters.get(i);
                if (!temp_charter.favorite)
                    continue;
                String cid = favoriteCharters.get(i).cid;
                View charterView = LayoutInflater.from(getActivity()).inflate(R.layout.charter_item, null);
                charterView.setOnClickListener(new CharterClickListener(favoriteCharters.get(i)));

                TextView bookTitleView = charterView.findViewById(R.id.charterNameView);
                TextView authorView = charterView.findViewById(R.id.charterNumberView);
                ImageView iconView = charterView.findViewById(R.id.iconView);
                TextView distanceView = charterView.findViewById(R.id.distance);
                Charter charter = favoriteCharters.get(i);
                bookTitleView.setText(charter.getAddress());
                authorView.setText(charter.getNumber());
                RequestOptions myOptions = new RequestOptions().override(50, 50);
                Glide.with(this).asBitmap().apply(myOptions).load(charter.getImageURL()).into(iconView);
                distanceView.setText(charter.getDistance());

                favoritesLayout.addView(charterView);
            }
        }

    }


    public static void resortFavorites() {
        Collections.sort(favoriteCharters, new Comparator<Charter>() {
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

    //Query the Firebase database to populate the favorite charter array.
    public static void getCharters(){
        favoriteCharters.clear();
        db.getInstance().collection("people")
                .whereEqualTo("username", LoginActivity.email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                if (document.exists()) {
                                    DocumentReference favRef = document.getReference();
                                    ArrayList<String> hashFavs = (ArrayList<String>) document.get("favorites");

                                    for (int i = 0; hashFavs != null && i < hashFavs.size(); i++) {
                                        String cidtemp = hashFavs.get(i);

                                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                                        StrictMode.setThreadPolicy(policy);
                                        final String username = "periphery";
                                        final String password = "PeripheryLFL";
                                        Connection conn = null;
                                        String SQL = "SELECT * FROM Charters WHERE cid = '" + cidtemp + "';";
                                        try {
                                            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
                                            Statement stmt = conn.createStatement();
                                            ResultSet rs = stmt.executeQuery(SQL);
                                            //charters.clear();
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
                                                Charter temp = new Charter(cid, street, city + ", " + state
                                                        + " " + zipcode, num, stewname, email, lat, lon, Tab1Fragment.current_lat,
                                                        Tab1Fragment.current_long, image, description);
                                                temp.setFavorite(true);
                                                favoriteCharters.add(temp);
                                                System.out.println("HEllo");
                                            }
                                            conn.close();
                                        } catch (SQLException e) {
                                            Log.d("MyApp", "sql " + e.getMessage());
                                        }



                                    }
                                    hint.setVisibility(View.VISIBLE);
                                    if (favoriteCharters.size() == 0){
                                        hint.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        hint.setVisibility(View.INVISIBLE);
                                    }
                                    System.out.println("getCharters within document, and size is : " + favoriteCharters.size());
                                    resortFavorites();
                                }
                            }
                        }
                    }
                });
        System.out.println("getCharters method and size is : " + favoriteCharters.size());

    }

}