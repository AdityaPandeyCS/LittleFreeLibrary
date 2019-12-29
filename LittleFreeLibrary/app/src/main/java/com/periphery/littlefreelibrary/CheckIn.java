package com.periphery.littlefreelibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class CheckIn extends AppCompatActivity implements View.OnClickListener {

    public int charterIndex;
    public boolean fromFavorites;
    public String cid;
    public String imageString;
    private Bitmap photo;
    private Button submit;
    private boolean image_added = false;
    private ImageView preview_image;

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //back navigation
        try {
            getSupportActionBar().setTitle("Add Book");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        } catch (NullPointerException e) {
            ;
        }
        setContentView(R.layout.activity_check_in);

        Intent mIntent = getIntent();

        charterIndex = mIntent.getIntExtra("charter index", 0);
        fromFavorites = mIntent.getBooleanExtra("favorites", false);
        cid = mIntent.getStringExtra("cid");
        preview_image = (ImageView) findViewById(R.id.preview_image);
        preview_image.setOnClickListener(this);

        submit = (Button) findViewById(R.id.submit_button);
        submit.setOnClickListener(this);

        Spinner spinner = (Spinner) findViewById(R.id.category);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Tab1Fragment.genres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    @Override
    public void onClick(View view) {
        TextInputLayout bookTitleLayout = (TextInputLayout) findViewById(R.id.bookTitleLayout);
        TextInputLayout authorTitleLayout = (TextInputLayout) findViewById(R.id.authorTitleLayout);

        if (view.getId() == R.id.preview_image) {
            //Start camera intent
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, 1);
            }
        }
        if (view.getId() == R.id.submit_button) {
            //Submit
            TextInputEditText bookTitleEditText = (TextInputEditText) findViewById(R.id.bookTitleView);
            String Title = bookTitleEditText.getText().toString();
            TextInputEditText AuthorEditText = (TextInputEditText) findViewById(R.id.bookAuthorView);
            String Author = AuthorEditText.getText().toString();
            TextInputEditText OptionalDescriptionEditText = (TextInputEditText) findViewById(R.id.bookdescriptionView);
            String Description = OptionalDescriptionEditText.getText().toString();
            String id = "" + System.currentTimeMillis();
            //check if there is text in the text boxes and if the user added a picture
            if (Title.length() > 0 && Author.length() > 0 && image_added) {
                //Add book
                Spinner spinner = (Spinner) findViewById(R.id.category);
                String category = spinner.getSelectedItem().toString();
                addBook(cid, id, imageString, Title, Author, category, Description);
                Toast.makeText(this, "Successfully added \"" + Title + "\"", Toast.LENGTH_SHORT).show();
                finish();
            } else if (Title.length() <= 0 && Author.length() <= 0) {
                Toast.makeText(this, "Please fill in the title and author.", Toast.LENGTH_SHORT).show();
                bookTitleLayout.setErrorEnabled(true);
                bookTitleLayout.setError("Required");

                authorTitleLayout.setErrorEnabled(true);
                authorTitleLayout.setError("Required");
            } else if (Title.length() <= 0) {
                Toast.makeText(this, "Please fill in the title.", Toast.LENGTH_SHORT).show();
                bookTitleLayout.setErrorEnabled(true);
                bookTitleLayout.setError("Required");
            } else if (Author.length() <= 0) {
                Toast.makeText(this, "Please fill in the author.", Toast.LENGTH_SHORT).show();
                authorTitleLayout.setErrorEnabled(true);
                authorTitleLayout.setError("Required");
            } else if (!image_added) {
                Toast.makeText(this, "Please add an image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addBook(String cid, String book_id, String img, String title, String author, String genre, String description) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final String username = "periphery";
        final String password = "PeripheryLFL";
        Connection conn = null;
        String SQL1 = String.format("INSERT INTO books VALUES ('%s', '%s', '%s', '%s', '%s', '%s');", book_id, img, title, author, genre, description);
        String SQL2 = String.format("INSERT INTO have VALUES ('%s', '%s', 1);", cid, book_id);

        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            stmt.executeQuery(SQL1);

        } catch (SQLException e) {
            Log.d("MyApp", "sql " + e.getMessage());
        }
        conn = null;

        try {
            conn = DriverManager.getConnection(Tab1Fragment.database_url, username, password);
            Statement stmt = conn.createStatement();
            stmt.executeQuery(SQL2);

        } catch (SQLException e) {
            Log.d("MyApp", "sql " + e.getMessage());
        }
        CatalogPage.update = true;
        CatalogPage.books.add(0, new Book(book_id, img, title, author, genre, description, 1));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //back button at the top
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data"); //add to the front of the list to match the catalog
            if (photo.getHeight() < photo.getWidth()) {
                //rotate image if it is horizontal because we only want vertical images.
                photo = RotateBitmap(photo, 90);
            }
//            Bitmap resized = Bitmap.createScaledBitmap(photo, photo.getWidth() * 4, photo.getHeight() * 4, true);
            Bitmap resized = Bitmap.createScaledBitmap(photo, photo.getWidth(), photo.getHeight(), true);
            preview_image.setImageBitmap(resized);
            image_added = true;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d("encode", "photo length = " + encoded.length());
            imageString = encoded;
        }
    }

}
