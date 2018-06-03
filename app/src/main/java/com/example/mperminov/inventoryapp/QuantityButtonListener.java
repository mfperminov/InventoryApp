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

public class QuantityButtonListener implements View.OnClickListener {
    private Uri mUri;
    private int mAction;
    private int mQuantityViewId;
    private boolean mNeedFormattedString;

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
        Cursor mCursor = v.getContext().getContentResolver().query
                (mUri, null, null, null, null);
        if (mCursor != null && mCursor.moveToFirst()) {
            quantity = mCursor.getInt(mCursor.getColumnIndex(StoreEntry.COLUMN_QUANTITY));
            mCursor.close();
            if (quantity <= 0) {
                Toast.makeText(v.getContext(), R.string.quantity_less_zero_error_toast,
                        Toast.LENGTH_SHORT).show();
            } else {
                ContentValues values = new ContentValues();
                values.put(StoreEntry.COLUMN_QUANTITY, quantity + mAction);
                TextView quantityTextView = rootView.findViewById(mQuantityViewId);
                if (mNeedFormattedString) {
                    //String formattedQuantityString = v.getContext().getString(R.string.quantity_available, Integer.parseInt
                    //(quantityTextView.getText().toString()) + mAction);
                    quantityTextView.setText("Nihuya");
                } else {
                    String selection = StoreEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mUri))};
                    v.getContext().getContentResolver().update(mUri, values, selection, selectionArgs);
                    quantityTextView.setText(String.valueOf(quantity + mAction));
                }
            }
        } else {
            Log.d("OnClick", "No data");
        }


        /*String selection = StoreEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(ContentUris.parseId(mUri))};

        v.getContext().getContentResolver().update(mUri,)*/
    }
}
