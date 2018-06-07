package com.example.mperminov.inventoryapp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link StoreCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of products data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class StoreCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = StoreCursorAdapter.class.getSimpleName();
    /**
     * Constructs a new {@link StoreCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public StoreCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name
     * TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //Find views.
        TextView nameTextView = view.findViewById(R.id.item_name_text_view);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        TextView priceTextView = view.findViewById(R.id.price_text_view);
        //Get data from cursor.
        String name = cursor.getString(cursor.getColumnIndexOrThrow
                (StoreEntry.COLUMN_PRODUCT_NAME));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(StoreEntry.COLUMN_QUANTITY));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(StoreEntry.COLUMN_PRICE));
        //Bind data to views.
        nameTextView.setText(name);
        String formattedQuantityString = context.getString(R.string.quantity_available, quantity);
        quantityTextView.setText(formattedQuantityString);
        String formattedPriceString = context.getString(R.string.price_with_dollar, price);
        priceTextView.setText(formattedPriceString);
        Button sellButton = view.findViewById(R.id.sell_button);
        // Get current row id;
        long currentId = cursor.getLong(cursor.getColumnIndex(StoreEntry._ID));
        Uri currentUri = ContentUris.withAppendedId(StoreEntry.CONTENT_URI, currentId);
        // Uri newUri = StoreEntry.CONTENT_URI.with id appended
        sellButton.setOnClickListener(new QuantityButtonListener(currentUri, -1,
                R.id.quantity_text_view, true));
        ImageView smallImageViewInList = view.findViewById(R.id.image_in_list_view);
        // if an uri for image exists - show image in List View.
        String imageUriString = cursor.getString(cursor.getColumnIndex(StoreEntry.COLUMN_IMAGE));
        if (!TextUtils.isEmpty(imageUriString)) {
            smallImageViewInList.setImageBitmap(getBitmapFromUri(Uri.parse(imageUriString), context));
        } else // make view transparent or views will be mixing up with images while scrolling
        {
            smallImageViewInList.setImageResource(0);
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
    private Bitmap getBitmapFromUri(Uri imageUri, Context context) {
        // Get the dimensions of the View
        int targetW = (int) context.getResources().getDimension(R.dimen.image_small_list_item);
        int targetH = (int) context.getResources().getDimension(R.dimen.image_small_list_item);
        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(imageUri);
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
            input = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, context.getResources().getString(R.string.failed_image_loading), fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, context.getResources().getString(R.string.failed_image_loading), e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {
                Log.e(LOG_TAG, context.getResources().getString(R.string.input_stream_error), ioe);
            }
        }
    }
}
