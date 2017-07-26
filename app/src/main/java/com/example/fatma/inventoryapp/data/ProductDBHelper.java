package com.example.fatma.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.fatma.inventoryapp.data.ProductContract.ProductEntry;

public class ProductDBHelper extends SQLiteOpenHelper {
    public final static String LOG_TAG = ProductDBHelper.class.getSimpleName();
    /**
     * Name of the database file
     */
    private static final String DATABADE_NAME = "shelter.db";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     **/
    private static final int DATABASE_VERSION = 1;

    public ProductDBHelper(Context context) {
        super(context, DATABADE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + "(" +
                ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ProductEntry.COLUMN_NAME + " TEXT NOT NULL," +
                ProductEntry.COLUMN_PRICE + " FLOAT NOT NULL DEFAULT 0.0," +
                ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                ProductEntry.COLUMN_SIZE + " INTEGER NOT NULL," +
                ProductEntry.COLUMN_IMAGE + " BLOB"
                + ");";
        Log.i(LOG_TAG, SQL_CREATE_PRODUCT_TABLE);
        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME);

        // create new table
        onCreate(db);

    }
}
