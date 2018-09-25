package dev.milos.contentproviderexample;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class BooksProvider extends ContentProvider {

    // == kreiranje provajdera ==
    static final String PROVIDER_NAME = "dev.milos.contentproviderexample.Books";
    static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/books");

    static final String ID = "_id";
    static final String TITLE = "naslov";
    static final String ISBN = "isbn";

    static final int BOOKS = 1;
    static final int BOOK_ID = 2;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "books", BOOKS);
        uriMatcher.addURI(PROVIDER_NAME, "books/#", BOOK_ID);
    }

    // == kreiranje baze podataka ==
    SQLiteDatabase booksDB;
    static final String DATABASE_NAME = "books.db";
    static final String DATABASE_TABLE = "knjiga";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + " (_id integer primary key autoincrement,  "
                    + "naslov text not null, isbn text not null);";


    // == inner klasa za komuniciranje sa bazom ==
    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("Baza podataka ProvCont",
                    "Ažuriranje verzije sa " +
                            oldVersion + " na verziju " + newVersion +
                            ", stari podaci biće uništeni");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        booksDB = dbHelper.getWritableDatabase();
        return (booksDB == null) ? false : true;

    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(DATABASE_TABLE);

        if (uriMatcher.match(uri) == BOOK_ID) {
            // == ako se preuzima konkretna knjiga ==
            sqLiteQueryBuilder.appendWhere(ID + " = " + uri.getPathSegments().get(1));
        }

        if (sortOrder == null || sortOrder == "") {
            sortOrder = TITLE;
        }

        Cursor cursor = sqLiteQueryBuilder.query(booksDB, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                return "vnd.android.cursor.dir/vnd.contentproviderexample.Books";
            case BOOK_ID:
                return "vnd.android.cursor.item/vnd.contentproviderexample.Books";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);

        }
    }

    @Nullable
    @Override
    public Uri insert(@Nullable Uri uri, @Nullable ContentValues values) {
        long rowID = booksDB.insert(DATABASE_TABLE, "", values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Dodavanje u bazu nije uspelo " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                count = booksDB.delete(DATABASE_TABLE, selection, selectionArgs);
                break;
            case BOOK_ID:
                String id = uri.getPathSegments().get(1);
                count = booksDB.delete(DATABASE_TABLE, ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);


        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case BOOKS:
                count = booksDB.update(DATABASE_TABLE, values, selection, selectionArgs);
                break;

            case BOOK_ID:
                String id = uri.getPathSegments().get(1);
                count = booksDB.delete(DATABASE_TABLE, ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);


        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
