package dev.milos.contentproviderexample;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    Button btnAdd, btnRetrieve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnRetrieve = (Button) findViewById(R.id.btnRetrieve);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAddTitle(v);
            }
        });

        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRetrieveTitles(v);
            }
        });
    }

    public void onClickAddTitle(View view) {

        ContentValues values = new ContentValues();
        values.put("naslov", ((EditText) findViewById(R.id.txtTitle)).getText().toString());
        values.put("isbn", ((EditText) findViewById(R.id.txtISBN)).getText().toString());

        Uri uri = getContentResolver().insert(Uri.parse("content://dev.milos.contentproviderexample.provider.Books/books"), values);

        Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
    }

    public void onClickRetrieveTitles(View view) {

        Uri allTitles = Uri.parse("content://dev.milos.contentproviderexample.provider.Books/books");
        Cursor c;
        CursorLoader cursorLoader = new CursorLoader(this,allTitles, null, null, null,"naslov desc");
        c = cursorLoader.loadInBackground();
        if (c.moveToFirst()) {
            do{
                Toast.makeText(this,
                        c.getString(c.getColumnIndex(BooksProvider.ID)) + ", " +
                                c.getString(c.getColumnIndex(
                                        BooksProvider.TITLE)) + ", " +
                                c.getString(c.getColumnIndex(
                                        BooksProvider.ISBN)),
                        Toast.LENGTH_SHORT).show();
            } while (c.moveToNext());
        }

    }

    public void updateTitle() {

        ContentValues editedValues = new ContentValues();
        editedValues.put(BooksProvider.TITLE, "Android Tipovi i trikovi.");
        getContentResolver().update(
                Uri.parse("content://dev.milos.contentproviderexample.provider.Books/books/2"), editedValues,null,null);
    }

    public void deleteTitle() {
//---brisanje naslova---
        getContentResolver().delete(Uri.parse("content://dev.milos.contentproviderexample.provider.Books/books/ 2"), null, null);
//---brisanje svih naslova---
        getContentResolver().delete(Uri.parse("content://dev.milos.contentproviderexample.provider.Books/books"), null, null);
    }




}
