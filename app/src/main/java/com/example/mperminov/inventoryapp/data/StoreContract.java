package com.example.mperminov.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class StoreContract {
    /**
     * Name for content provider - this app. Must be unique on the device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.mperminov.inventoryapp";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_PRODUCTS = "products";
    public static abstract class StoreEntry implements BaseColumns {
        //empty constructor - don't need to instance;
        private StoreEntry() {
        }

        //identify data in our app - table with products data
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
        public static final String COLUMN_IMAGE = "image_uri";
        //one more column for "upgrade"
        public static final String COLUMN_SIZES = "sizes";
    }
}
