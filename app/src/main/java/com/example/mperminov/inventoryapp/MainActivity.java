package com.example.mperminov.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;
import com.github.javafaker.Faker;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Use Floating button in right corner for adding new items
    @BindView(R.id.fab)
    FloatingActionButton addButton;
    // Custom CursorAdapter
    StoreCursorAdapter storeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //kick off loader
        getLoaderManager().initLoader(0, null, this);
        //finding list view and set empty view on it
        ListView itemsListView = findViewById(R.id.list_view);
        itemsListView.setEmptyView(findViewById(R.id.empty_view));
        //set out custom adapter on list view
        storeAdapter = new StoreCursorAdapter(this, null);
        itemsListView.setAdapter(storeAdapter);
        // From the rubric - "When a user clicks on a List Item from the Main Activity,
        // it opens up the detail screen for the correct product."
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override 
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(parent.getContext(), ItemActivity.class);
                // Nice parent-child transit animations as described in Material Design.
                // New activity fills screen starting from item position.
                Bundle options = ActivityOptionsCompat.makeScaleUpAnimation(
                        view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
                // Send Uri of tapped item to ItemActivity.
                Uri currentPetUri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, id);
                intent.setData(currentPetUri);
                ActivityCompat.startActivity(parent.getContext(), intent, options);
            }
        });
        // From the rubric - "The Main Activity contains an Add Product Button.."
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), EditActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert random product" menu option
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAll();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method starts dialog asking user about deleting all entries. And if confirmed do it.
     */
    private void deleteAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Ask question to user.
        builder.setMessage(R.string.delete_all_dialog_message);
        // Add the buttons
        builder.setPositiveButton(R.string.delete_entry_accept,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked delete button. Delete all!
                        int rowsDeleted = getContentResolver().delete(StoreEntry.CONTENT_URI, null,
                                null);
                        if (rowsDeleted > 0) {
                            // everything fine
                            Toast.makeText(getBaseContext(), R.string.delete_successful,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getBaseContext(), R.string.delete_all_not_ok,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.setNegativeButton(R.string.cancel_delete_dialog,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog. Dismiss the dialog.
                        dialog.dismiss();
                    }
                });
        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void insertProduct() {
        //new instance of faker for more fake data!
        Faker faker = new Faker();
        String productName = faker.commerce().productName();
        String supplierName = faker.name().fullName();
        String phoneNumber = faker.phoneNumber().phoneNumber();
        // Generate random int number within [0 1000] interval
        int price = (int) (Math.random() * ((1000) + 1));
        // Generate random int number within [0 100] interval
        int quantity = (int) (Math.random() * ((100) + 1));
        //Save values
        ContentValues values = new ContentValues();
        values.put(StoreEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(StoreEntry.COLUMN_PRICE, price);
        values.put(StoreEntry.COLUMN_QUANTITY, quantity);
        values.put(StoreEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER, phoneNumber);
        // Try to insert and look if uri is null. If yes something went wrong.
        Uri newRow = getContentResolver().insert(StoreEntry.CONTENT_URI, values);
        if (newRow == null) {
            Toast.makeText(this, R.string.toast_problem_insert_fake, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.toast_success_fake, Toast.LENGTH_SHORT).show();
        }
    }

    // Load data from database into separate thread by using loaders.
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Don't request for supplier info - we don't show it in MainActivity.
        String[] projection = {
                StoreEntry._ID,
                StoreEntry.COLUMN_PRODUCT_NAME,
                StoreEntry.COLUMN_QUANTITY,
                StoreEntry.COLUMN_PRICE,
                StoreEntry.COLUMN_IMAGE
        };
        return new CursorLoader(this, StoreEntry.CONTENT_URI, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // When end loading feed data to custom adapter
        storeAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Previous loader is being reset, so data unavailable and swap it with null for now.
        storeAdapter.swapCursor(null);
    }

}
