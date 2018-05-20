package com.example.mperminov.inventoryapp.data;

import android.provider.BaseColumns;

public class StoreContract {
    public static abstract class StoreEntry implements BaseColumns {
        //empty constructor - don't need to instance;
        private StoreEntry() {
        }

        public static final String TABLE_NAME = "products";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
        public static final String COLUMN_DISCOUNTED = "has_discount";

        /**
         * If value 1 then discount from the supplier for this product
         * is exist, if 0 (default) there is no discount.
         */
        public static final int HAS_DISCOUNT = 1;
        public static final int NO_DISCOUNT = 0;
    }
}
