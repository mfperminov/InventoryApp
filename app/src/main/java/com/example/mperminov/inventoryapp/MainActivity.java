package com.example.mperminov.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;
import com.example.mperminov.inventoryapp.data.StoreDbHelper;
import com.github.javafaker.Faker;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private StoreDbHelper mDbHelper;
    StoreCursorAdapter storeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getLoaderManager().initLoader(0, null, this);
        ListView itemsListView = findViewById(R.id.list_view);
        storeAdapter = new StoreCursorAdapter(this, null);
        itemsListView.setAdapter(storeAdapter);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(parent.getContext(), ItemActivity.class);
                Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(
                        view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
                ActivityCompat.startActivity(parent.getContext(), intent, options);
            }
        });
        insertProduct();
    }

    private void insertProduct() {
        //new instance of faker for more fake data!
        Faker faker = new Faker();
        String nameString = faker.commerce().productName();
        //Generate random int number within [0 1000] interval
        int price = (int) (Math.random() * ((1000) + 1));
        //Generate random int number within [0 100] interval
        int quantity = (int) (Math.random() * ((100) + 1));
        String supplierName = faker.name().fullName();
        String phoneNumber = faker.phoneNumber().phoneNumber();
        //this should generate random 0 or 1
        int hasDiscount = (Math.random() < 0.5) ? 0 : 1;
        //save values
        ContentValues values = new ContentValues();
        values.put(StoreEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(StoreEntry.COLUMN_PRICE, price);
        values.put(StoreEntry.COLUMN_QUANTITY, quantity);
        values.put(StoreEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER, phoneNumber);
        values.put(StoreEntry.COLUMN_DISCOUNTED, hasDiscount);
        //get database in write mode.
        mDbHelper = new StoreDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        //insert all values in database
        long rowId = db.insert(StoreEntry.TABLE_NAME, null, values);
        //check for error
        if (rowId == -1) {
            Toast.makeText(this, "Error with inserting row in database",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "New row has been added with index " + rowId,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                StoreEntry._ID,
                StoreEntry.COLUMN_PRODUCT_NAME,
                StoreEntry.COLUMN_QUANTITY,
                StoreEntry.COLUMN_PRICE
        };
        return new CursorLoader(this, StoreEntry.CONTENT_URI, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        storeAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        storeAdapter.swapCursor(null);
    }

   /* private void displayDatabaseInfo() {
        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] projection = {
                StoreEntry._ID,
                StoreEntry.COLUMN_PRODUCT_NAME,
                StoreEntry.COLUMN_PRICE,
                StoreEntry.COLUMN_QUANTITY,
                StoreEntry.COLUMN_SUPPLIER_NAME,
                StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER,
                StoreEntry.COLUMN_DISCOUNTED,
        };
        // Perform this SQL query "SELECT * FROM products"
        // to get a Cursor that contains all rows from the products table.
        // Actually we can pass null instead of projection to achieve the same thing.
        Cursor cursor = db.query(
                StoreEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);
        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // products table in the database).
            TextView displayView = findViewById(R.id.database_log_tv);
            displayView.setText(getString(R.string.table_header) + cursor.getCount());
            // Create table header.
            displayView.append("\n" + StoreEntry._ID + " - "
                    + StoreEntry.COLUMN_PRODUCT_NAME + " - " + StoreEntry.COLUMN_PRICE + " - "
                    + StoreEntry.COLUMN_QUANTITY + " - " + StoreEntry.COLUMN_SUPPLIER_NAME + " - "
                    + StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER + " - "
                    + StoreEntry.COLUMN_DISCOUNTED + " - " + "\n");
            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(StoreEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            int discountColumnIndex = cursor.getColumnIndex(StoreEntry.COLUMN_DISCOUNTED);
            // Iterate through all rows and append data to TextView.
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhone = cursor.getString(phoneColumnIndex);
                int currentDiscountState = cursor.getInt(discountColumnIndex);
                // Display the values from each column of the current row in the cursor in the TextView
                displayView.append(("\n" + currentID + " - "
                        + currentName + " - " + currentPrice + " - " + currentQuantity + " - "
                        + currentSupplierName + " - " + currentSupplierPhone + " - "
                        + currentDiscountState));
            }

        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }*/
}
