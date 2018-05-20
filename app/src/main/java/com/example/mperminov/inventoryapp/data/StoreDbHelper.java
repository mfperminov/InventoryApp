package com.example.mperminov.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

public class StoreDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = StoreDbHelper.class.getSimpleName();
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "store.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + StoreEntry.TABLE_NAME + " (" +
                    StoreEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    StoreEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL," +
                    StoreEntry.COLUMN_PRICE + " INTEGER NOT NULL," +
                    StoreEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
                    StoreEntry.COLUMN_SUPPLIER_NAME + " TEXT," +
                    StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " TEXT," +
                    StoreEntry.COLUMN_DISCOUNTED + " INTEGER NOT NULL DEFAULT 0)";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + StoreEntry.TABLE_NAME;

    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.v(LOG_TAG, SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
