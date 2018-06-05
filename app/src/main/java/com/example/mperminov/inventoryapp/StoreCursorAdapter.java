package com.example.mperminov.inventoryapp;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

/**
 * {@link StoreCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of products data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class StoreCursorAdapter extends CursorAdapter {
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
        sellButton.setOnClickListener(new QuantityButtonListener(currentUri, -1, R.id.quantity_text_view, true));
    }
}
