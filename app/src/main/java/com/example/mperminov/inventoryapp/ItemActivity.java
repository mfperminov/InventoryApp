package com.example.mperminov.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Uri mUri;
    @BindView(R.id.product_name_tv)
    TextView productNameTextView;
    @BindView(R.id.price_tv)
    TextView priceTextView;
    @BindView(R.id.quantity_tv)
    TextView quantityTextView;
    @BindView(R.id.button_minus)
    ImageButton buttonMinus;
    @BindView(R.id.button_plus)
    ImageButton buttonPlus;
    @BindView(R.id.supplier_name_tv)
    TextView supplierNameTextView;
    @BindView(R.id.supplier_phone_tv)
    TextView supplierPhoneTextView;
    @BindView(R.id.button_order)
    Button callButton;
    @BindView(R.id.button_delete)
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.bind(this);
        if (getIntent().getData() != null) {
            mUri = getIntent().getData();
            getLoaderManager().initLoader(0, null, this);
            setButtonsClickListeners();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_item.xml file.
        // This adds icon for Edit in toolbar
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.setData(mUri);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setButtonsClickListeners() {
        // Button to call - when clicked intent is started.
        // As a result user will get a dial pad with number of supplier already entered.
        // And user can cancel or resume calling.
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(supplierPhoneTextView.getText())) {
                    Uri call = Uri.parse("tel:" + String.valueOf(supplierPhoneTextView.getText()));
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, call);
                    startActivity(callIntent);
                } else {
                    Toast.makeText(v.getContext(), R.string.toast_provide_phone,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Delete button when clicked start coonfirmation dialog and on confirm
        // delete entry.
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // In most parts code provided by Google Developer Guide:
                // https://developer.android.com/guide/topics/ui/dialogs
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                // Ask question to user.
                builder.setMessage(R.string.delete_dialog_message);
                // Add the buttons
                builder.setPositiveButton(R.string.delete_entry_accept,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked delete button. Delete current entry.
                                int rowsDeleted = getContentResolver().delete(mUri, null,
                                        null);
                                if (rowsDeleted > 0) {
                                    Toast.makeText(v.getContext(), R.string.delete_successful,
                                            Toast.LENGTH_SHORT).show();
                                    //return to Main list and call OnDestroy() for this activity by
                                    //calling finish()
                                    Intent intentMainActivity = new Intent(v.getContext(),
                                            MainActivity.class);
                                    startActivity(intentMainActivity);
                                    finish();
                                } else {
                                    Toast.makeText(v.getContext(), R.string.delete_not_ok,
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
        });
        buttonMinus.setOnClickListener(new QuantityButtonListener(mUri, -1, quantityTextView.getId(), false));
        buttonPlus.setOnClickListener(new QuantityButtonListener(mUri, 1, quantityTextView.getId(), false));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                StoreEntry._ID,
                StoreEntry.COLUMN_PRODUCT_NAME,
                StoreEntry.COLUMN_PRICE,
                StoreEntry.COLUMN_QUANTITY,
                StoreEntry.COLUMN_SUPPLIER_NAME,
                StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this, mUri, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            finish();
        }
        if (data.moveToFirst()) {
            productNameTextView.setText(data.getString
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_PRODUCT_NAME)));
            String formattedPrice = getString(R.string.price_with_dollar,
                    data.getInt(data.getColumnIndexOrThrow(StoreEntry.COLUMN_PRICE)));
            priceTextView.setText(formattedPrice);
            quantityTextView.setText(String.valueOf(data.getInt
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_QUANTITY))));
            supplierNameTextView.setText(data.getString
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_SUPPLIER_NAME)));
            supplierPhoneTextView.setText(data.getString
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameTextView.setText("");
        priceTextView.setText("");
        quantityTextView.setText("");
        supplierNameTextView.setText("");
        supplierPhoneTextView.setText("");
    }
}
