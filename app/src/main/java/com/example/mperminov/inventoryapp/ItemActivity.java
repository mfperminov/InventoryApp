package com.example.mperminov.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ItemActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Uri of current item
    private Uri mUri;
    private static final String LOG_TAG = ItemActivity.class.getSimpleName();
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
    @BindView(R.id.item_image)
    ImageView itemImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);
        ButterKnife.bind(this);
        if (getIntent().getData() != null) {
            // Get info from database about item we got to Item Activity with
            mUri = getIntent().getData();
            getLoaderManager().initLoader(0, null, this);
            setButtonsClickListeners();
        } else {
            // Basically we never shall be in ItemActivity without Uri in intent. If so - just
            // forward to MainActivity
            // This is helpful then we press Back from EditActivity in Add mode without
            // enter product info. Then we left over ItemActivity and transit to Main.
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
        // Launch EditActivity in Edit mode.
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
        // Delete button when clicked start confirmation dialog and on confirm
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
        // Set listeners for "Plus" and "Minus" buttons.
        buttonMinus.setOnClickListener(new QuantityButtonListener(mUri, -1,
                quantityTextView.getId(), false));
        buttonPlus.setOnClickListener(new QuantityButtonListener(mUri, 1,
                quantityTextView.getId(), false));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Here need full information
        String[] projection = {
                StoreEntry._ID,
                StoreEntry.COLUMN_PRODUCT_NAME,
                StoreEntry.COLUMN_PRICE,
                StoreEntry.COLUMN_QUANTITY,
                StoreEntry.COLUMN_SUPPLIER_NAME,
                StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER,
                StoreEntry.COLUMN_IMAGE
        };
        return new CursorLoader(this, mUri, projection, null,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // If no data - return to MainActivity and ask user to add new entries.
        if (data == null || data.getCount() < 1) {
            finish();
        }
        // If there is data - bind it to Views
        if (data.moveToFirst()) {
            productNameTextView.setText(data.getString
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_PRODUCT_NAME)));
            // Need price in TextView like "$15" not simple number "15"
            String formattedPrice = getString(R.string.price_with_dollar,
                    data.getInt(data.getColumnIndexOrThrow(StoreEntry.COLUMN_PRICE)));
            priceTextView.setText(formattedPrice);
            quantityTextView.setText(String.valueOf(data.getInt
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_QUANTITY))));
            supplierNameTextView.setText(data.getString
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_SUPPLIER_NAME)));
            supplierPhoneTextView.setText(data.getString
                    (data.getColumnIndexOrThrow(StoreEntry.COLUMN_SUPPLIER_PHONE_NUMBER)));
            String imageUriString = data.getString(data.getColumnIndex(StoreEntry.COLUMN_IMAGE));
            //if an Uri of an image exists - show it!
            if (!TextUtils.isEmpty(imageUriString)) {
                itemImageView.setImageBitmap(getBitmapFromUri(Uri.parse(imageUriString)));
            }
        }
    }

    /**
     * Memory saving method for showing an image
     * It expands the JPEG into a memory array that’s
     * already scaled to match the size of the destination view.
     * Taken from
     * https://github.com/crlsndrsjmnz/MyShareImageExample
     *
     * @param imageUri - URI of an image saved on phone.
     * @return Bitmap of an image
     */
    private Bitmap getBitmapFromUri(Uri imageUri) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        // Get the dimensions of the Screen
        // This part rewritten because original approach can throw Division by zero exception
        // then method try to calculate scaleFactor with zero height.
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int targetW = displayMetrics.widthPixels;
        int targetH = Math.round((displayMetrics.heightPixels) * 2 / 3);
        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(imageUri);
            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;
            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;
            input = this.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, getString(R.string.failed_image_loading), fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, getString(R.string.failed_image_loading), e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, getString(R.string.input_stream_error), ioe);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // When loader is being reset clear all TextViews.
        productNameTextView.setText("");
        priceTextView.setText("");
        quantityTextView.setText("");
        supplierNameTextView.setText("");
        supplierPhoneTextView.setText("");
    }
}
