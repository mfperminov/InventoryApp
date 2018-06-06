package com.example.mperminov.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.mperminov.inventoryapp.data.StoreContract.StoreEntry;

public class StoreProvider extends ContentProvider {
    //Database helper object
    private StoreDbHelper mDbHelper;
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = StoreProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 10;

    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCT_ID = 11;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // Patterns for UriMatcher to recognize
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY,
                StoreContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }


    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        //return true because provider was successfully loaded and we don't call error prone
        //methods here like getReadableDatabase() or getWritableDatabase().
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(StoreEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as
                // "content://com.example.mperminov.inventoryapp/products/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = StoreEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(StoreEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            // We can insert new row data only if given uri matches the whole table,
            // not specific row
            case PRODUCTS:
                return insertProduct(uri, values);
            // Thrown an exception else
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }


    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        // Get database to write.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Try to insert new row with values. Get back the row id and (-1) if
        // an error had happen.
        long rowId = database.insert(StoreEntry.TABLE_NAME, null, values);
        if (rowId == -1) {
            return null;
        }
        // Notify about new row to load new data to cursor.
        getContext().getContentResolver().notifyChange(uri, null);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, rowId);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        // To hold number of rows deleted
        int rowsDeleted;
        // Get database to write
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                //just delete the whole table
                rowsDeleted = database.delete(StoreEntry.TABLE_NAME, null, null);
                break;
            case PRODUCT_ID:
                // selection by column "id"...
                selection = StoreEntry._ID + "=?";
                //where argument id ID given by uri.
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(StoreEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // We can't delete 0 or less rows.
        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;

    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = StoreEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection,
                              String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Get writable database so we can insert new row.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(StoreEntry.TABLE_NAME, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
