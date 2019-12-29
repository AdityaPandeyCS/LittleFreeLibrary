package com.periphery.littlefreelibrary;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class SpecificCharter extends Activity implements View.OnClickListener {
    public Button SeeCatalogButton;
    public TextView address;
    public TextView contact;
    public TextView description;
    public TextView hint;
    public Charter charter;
    public String cid;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_charter);


        //get the corresponding charter instance from the main activity
        Intent mIntent = getIntent();
        charter = (Charter) mIntent.getSerializableExtra("charter");
        cid = charter.getCid();

        /*Rename and add the back navigation button*/
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(charter.address);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));

        //set the actionbar color to white
        Spannable actionBarText = new SpannableString(actionBar.getTitle());
        actionBarText.setSpan(new ForegroundColorSpan(Color.WHITE), 0, actionBarText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        actionBar.setTitle(actionBarText);

        SeeCatalogButton = (Button) findViewById(R.id.see_books_button);
        SeeCatalogButton.setOnClickListener(this);

        address = (TextView) findViewById(R.id.address_text);
        address.setOnClickListener(this);

        contact = (TextView) findViewById(R.id.contact_email);
        contact.setOnClickListener(this);

        description = (TextView) findViewById(R.id.Charter_Description);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        SpannableString addr = new SpannableString(charter.longAddress);
        addr.setSpan(new UnderlineSpan(), 0, addr.length(), 0);

        SpannableString email = new SpannableString(charter.email);
        email.setSpan(new UnderlineSpan(), 0, email.length(), 0);

        ((TextView) findViewById(R.id.address_text)).setText(addr);
        ((TextView) findViewById(R.id.contact_name)).setText(charter.steward);
        ((TextView) findViewById(R.id.contact_email)).setText(email);
        Glide.with(this).load(charter.getImageURL()).into(((ImageView) findViewById(R.id.charter_image)));
        ((TextView) findViewById(R.id.Charter_Description)).setText(charter.description);
    }

    //Menu for the favorite button
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menubar_specific_charter, menu);
        //load the initial state of the icon

        if (!isFavoriteCharter(charter.cid)) {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.heartborder);
        } else {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.heartfilled);
        }

        return true;
    }

    public boolean isFavoriteCharter(String cid) {
        for (int i = 0; i < Tab2Fragment.favoriteCharters.size(); i++) {
            String fav_cid = Tab2Fragment.favoriteCharters.get(i).cid;
            if (fav_cid.equals(cid))
                return true;
        }
        return false;
    }

    public void removeFavoriteCharter(String cid) {
        for (int i = 0; i < Tab2Fragment.favoriteCharters.size(); i++) {
            String fav_cid = Tab2Fragment.favoriteCharters.get(i).cid;
            if (fav_cid.equals(cid)) {
                Tab2Fragment.favoriteCharters.get(i).setFavorite(false);
                Tab2Fragment.favoriteCharters.remove(i);
                return;
            }
        }
    }
    public void addFavorite(){
        charter.setFavorite(true);
        db.collection("people")
                .whereEqualTo("username", LoginActivity.email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    DocumentReference favRef = document.getReference();
                                    favRef.update("favorites", FieldValue.arrayUnion(cid));
                                }
                            }
                        }
                    }
                });
        if (!isFavoriteCharter(charter.cid)) {
            Tab2Fragment.favoriteCharters.add(charter);
            Tab2Fragment.resortFavorites();
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                // User chose the Favorite button on the UI

                if (!isFavoriteCharter(charter.cid)) {
                    item.setIcon(R.drawable.heartfilled);
                    //Query to Firebase with the current user add to favorites

                    addFavorite();
                    System.out.println("added charter, size is :" + Tab2Fragment.favoriteCharters.size());
                    Toast.makeText(this, "Added to favorites.", Toast.LENGTH_SHORT).show();
                    charter.setFavorite(true);


                } else {
                    item.setIcon(R.drawable.heartborder);
                    //Query to Firebase with the current user add to favorites
                    removeFavorite();
                    Toast.makeText(this, "Removed from favorites.", Toast.LENGTH_SHORT).show();
                    charter.setFavorite(false);
                }

                return true;

            case android.R.id.home:
                //back button at the top
                finish();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onResume() {  // After a pause OR at startup
        super.onResume();
    }


    @Override
    // handle clicks for the steward email and the address
    public void onClick(View view) {
        if (view.getId() == R.id.address_text) {
            TextView v = (TextView) view;
            String address = v.getText().toString();
            address.replace(" ", "+");
            String url = "https://www.google.com/maps/search/?api=1&query=" + address;
            Uri uriUrl = Uri.parse(url);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        } else if (view.getId() == R.id.contact_email) {
            TextView v = (TextView) view;
            String contact = v.getText().toString();
            String url = "mailto:" + contact;
            Uri uriUrl = Uri.parse(url);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
        } else if (view.getId() == R.id.see_books_button) {
            Intent intent = new Intent(this, CatalogPage.class);
            intent.putExtra("cid", cid);
            startActivity(intent);
        }
    }



    public void removeFavorite(){
        charter.setFavorite(false);
        removeFavoriteCharter(charter.cid);
        db.collection("people")
                .whereEqualTo("username", LoginActivity.email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    DocumentReference favRef = document.getReference();
                                    favRef.update("favorites", FieldValue.arrayRemove(charter.getCid()));

                                }
                            }
                        }
                    }
                });

    }
}