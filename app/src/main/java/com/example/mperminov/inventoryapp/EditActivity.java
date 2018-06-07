package com.example.mperminov.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mperminov.inventoryapp.data.StoreContract;
import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

import butterknife.BindView;
import butterknife.ButterKnife;


public class EditActivity extends AppCompatActivity {

    private Uri mUri;
    // Uri for optional image
    private Uri mImageUri;
    private boolean mDataHasChanged = false;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final String LOG_TAG = EditActivity.class.getSimpleName();
    // TextInputEditText is a child of TextInputLayout
    // First - for grabbing data, second - to show an error in input if exist
    @BindView(R.id.edit_product)
    TextInputEditText productEditText;
    @BindView(R.id.edit_price)
    TextInputEditText priceEditText;
    @BindView(R.id.edit_quantity)
    TextInputEditText quantityEditText;
    @BindView(R.id.edit_supplier_name)
    TextInputEditText supplierNameEditText;
    @BindView(R.id.edit_supplier_phone)
    TextInputEditText supplierPhoneEditText;
    @BindView(R.id.layout_edit_product)
    TextInputLayout productTextLayout;
    @BindView(R.id.layout_edit_price)
    TextInputLayout priceTextLayout;
    @BindView(R.id.layout_edit_quantity)
    TextInputLayout quantityTextLayout;
    @BindView(R.id.layout_edit_supplier_name)
    TextInputLayout supplierTextLayout;
    @BindView(R.id.layout_edit_supplier_phone)
    TextInputLayout supplierPhoneLayout;
    // Button for image picking
    @BindView(R.id.image_chooser_button)
    Button imageChooseButton;
    @BindView(R.id.image_uri_text_view)
    TextView imageUriText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);
        if (getIntent().getData() != null) {
            mUri = getIntent().getData();
            setTitle(getString(R.string.edit_label_case_edit));
            inflateData(mUri);
        } else {
            setTitle(getString(R.string.edit_label_case_add));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        productEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        supplierNameEditText.setOnTouchListener(mTouchListener);
        supplierPhoneEditText.setOnTouchListener(mTouchListener);
        imageChooseButton.setOnTouchListener(mTouchListener);
        imageChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });
    }

    private void inflateData(Uri mUri) {
        Cursor mCursor = getContentResolver().query(mUri, null,
                null, null, null);
        if (mCursor.moveToFirst()) {
            String name = mCursor.getString(mCursor.getColumnIndexOrThrow
                    (StoreEntry.COLUMN_PRODUCT_NAME));
            int quantity = mCursor.getInt(mCursor.getColumnIndexOrThrow(StoreEntry.COLUMN_QUANTITY));
            int price = mCursor.getInt(mCursor.getColumnIndexOrThrow(StoreEntry.COLUMN_PRICE));
            String supplier = mCursor.getString(mCursor.getColumnIndexOrThrow
                    (StoreEntry.COLUMN_SUPPLIER_NAME));
            String supplierPhone = mCursor.getString(mCursor.getColumnIndexOrThrow
                    (StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER));
            productEditText.setText(name);
            quantityEditText.setText(String.valueOf(quantity));
            priceEditText.setText(String.valueOf(price));
            supplierNameEditText.setText(supplier);
            supplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_edit.xml file.
        // This adds icon for Save in toolbar
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (!mDataHasChanged && validateData()) {
                    Toast.makeText(this, R.string.save_nothing_changes_toast,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                if (validateData()) {
                    ContentValues values = collectData();
                    if (mUri != null) {
                        int rowsUpdated = getContentResolver().update(mUri, values, null,
                                null);
                        if (rowsUpdated > 0) {
                            Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, R.string.update_failure, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Uri newRow =
                                getContentResolver().insert(StoreContract.StoreEntry.CONTENT_URI, values);
                        if (newRow != null) {
                            Toast.makeText(this, R.string.insert_success, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, R.string.insert_failure, Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                } else {
                    Toast.makeText(this, R.string.user_correct_toast, Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                Log.e("button click", "id home clcked");
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mDataHasChanged) {
                    upButtonNavigate();
                    //NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardUpButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                //NavUtils.navigateUpFromSameTask(EditActivity.this);
                                upButtonNavigate();
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardUpButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void upButtonNavigate() {
        if (mUri != null) {
            Intent intent = new Intent(this, ItemActivity.class);
            intent.setData(mUri);
            ActivityCompat.startActivity(this, intent, null);
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    /**
     * Method collects all inputs from EditTextFields after validation
     *
     * @return Content values to create/update row in database.
     */

    private ContentValues collectData() {
        ContentValues values = new ContentValues();
        values.put(StoreEntry.COLUMN_PRODUCT_NAME, productEditText.getText().toString());
        values.put(StoreEntry.COLUMN_PRICE, Float.valueOf(priceEditText.getText().toString()));
        values.put(StoreEntry.COLUMN_QUANTITY, Integer.valueOf(
                quantityEditText.getText().toString()));
        values.put(StoreEntry.COLUMN_SUPPLIER_NAME, supplierNameEditText.getText().toString());
        values.put(StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER,
                supplierPhoneEditText.getText().toString());
        // If user choose an image add its address to values
        if (mImageUri != null) {
            values.put(StoreEntry.COLUMN_IMAGE, mImageUri.toString());
        }
        return values;
    }

    /**
     * Method collect data from TextInputEditText fields and check it according to this rules:
     * *Product name - not empty;
     * *Price - not empty. Also it should be > 0 but we can guarantee it with typeInput specified
     * in xml layout
     * *Quantity - same as for price. Positive and integer value guaranteed by xml layout parameter
     * *Supplier name - can be empty BUT I think often happens that some people start
     * to write phone instead of name(like me). So if we found that input doesn't contain
     * letters at all, we should tell user to correct name.
     * Supplier phone number - can be empty and inputType guarantee to us phone number
     * so don't check
     *
     * @return result of validation
     */
    private boolean validateData() {
        boolean dataCorrect = true;
        //*Product name - not empty;
        if (TextUtils.isEmpty(productEditText.getText().toString())) {
            productTextLayout.setError(getString(R.string.product_name_empty_error));
            dataCorrect = false;
        } else {
            productTextLayout.setError("");
        }
        //*Price - not empty
        if (TextUtils.isEmpty(priceEditText.getText().toString())) {
            priceTextLayout.setError(getString(R.string.enter_price_error));
            dataCorrect = false;
        } else {
            priceTextLayout.setError("");
        }
        //*Quantity - not empty
        if (TextUtils.isEmpty(quantityEditText.getText().toString())) {
            quantityTextLayout.setError(getString(R.string.enter_quantity_error));
            dataCorrect = false;
        } else {
            quantityTextLayout.setError("");
        }
        // Supplier name - not empty and aat least one letter. Correct after first review.
        if (TextUtils.isEmpty(supplierNameEditText.getText().toString())) {
            supplierTextLayout.setError(getString(R.string.enter_supplier_error));
            dataCorrect = false;
        } else if (supplierNameEditText.getText().toString().matches("[0-9]+") &&
                !TextUtils.isEmpty(supplierNameEditText.getText().toString())) {
            supplierTextLayout.setError(getString(R.string.name_error_edit_text));
            dataCorrect = false;
        } else {
            supplierTextLayout.setError("");
        }
        // Supplier phone - not empty. Correct after first review.
        if (TextUtils.isEmpty(supplierPhoneEditText.getText().toString())) {
            supplierPhoneLayout.setError(getString(R.string.enter_supplier_phone_error));
            dataCorrect = false;
        } else {
            quantityTextLayout.setError("");
        }
        return dataCorrect;
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDataHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mDataHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            if (data != null) {
                mImageUri = data.getData();
                Log.i(LOG_TAG, "Uri: " + mImageUri.toString());
                imageUriText.setText(mImageUri.toString());
            }
        }
    }
}
