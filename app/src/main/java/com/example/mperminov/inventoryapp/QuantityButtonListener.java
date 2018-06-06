package com.example.mperminov.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

/**
 * Custom button listener for changing quantity in ItemActivity ( by + and - buttons)
 * and MainActivity (by "Sell" button).
 */
public class QuantityButtonListener implements View.OnClickListener {
    private Uri mUri;
    private int mAction;
    private int mQuantityViewId;
    private boolean mNeedFormattedString;

    /**
     * Default constructor
     *
     * @param uri                 Uri of item which quantity is cnaged by button.
     * @param action              number to increase or decrease by button click
     * @param quantityViewId      in some cases automatic update of data (like from cursor adapter will
     *                            not be triggered, so set new quantity value in this view manually
     * @param needFormattedString true if need formatted string in TextView for quantity.
     */
    public QuantityButtonListener(Uri uri, int action, int quantityViewId, boolean needFormattedString) {
        mUri = uri;
        mAction = action;
        mQuantityViewId = quantityViewId;
        mNeedFormattedString = needFormattedString;
    }

    @Override
    public void onClick(View v) {
        int quantity;
        View rootView = v.getRootView();
        //get item info
        Cursor mCursor = v.getContext().getContentResolver().query
                (mUri, null, null, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            quantity = mCursor.getInt(mCursor.getColumnIndex(StoreEntry.COLUMN_QUANTITY));
            mCursor.close();
            //if quantity already zero 0 and user ask to decrease - stop doing it and warn user.
            if (quantity <= 0 && mAction == -1) {
                Toast.makeText(v.getContext(), R.string.quantity_less_zero_error_toast,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Put new value of quantity in values and update
                ContentValues values = new ContentValues();
                values.put(StoreEntry.COLUMN_QUANTITY, quantity + mAction);
                TextView quantityTextView = rootView.findViewById(mQuantityViewId);
                if (mNeedFormattedString) {
                    // when we need formatted string - for current state of app it's flag that
                    // button in MainActivity. So simply update values in database.
                    String selection = StoreEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mUri))};
                    v.getContext().getContentResolver().update(mUri, values, selection, selectionArgs);
                } else {
                    // Else we are in ItemActivity and more than updating we need to set value
                    // to TextView.
                    String selection = StoreEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mUri))};
                    v.getContext().getContentResolver().update(mUri, values, selection, selectionArgs);
                    quantityTextView.setText(String.valueOf(quantity + mAction));
                }
            }
        } else {
            Log.d("OnClick", "No data");
        }
    }
}
