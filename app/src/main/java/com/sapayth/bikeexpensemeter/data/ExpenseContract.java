package com.sapayth.bikeexpensemeter.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by S6H on 1/26/2018.
 */

public final class ExpenseContract {

    public static final String CONTENT_AUTHORITY = "com.sapayth.bikeexpensemeter";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_EXPENSES = "expenses";

    public static abstract class ExpenseEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EXPENSES);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of expense.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXPENSES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single expense.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_EXPENSES;

        public static final String TABLE_NAME = "expenses";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_ODOMETER = "odometer";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TOTAL_COST = "total_cost";
        public static final String COLUMN_PRICE_L = "price_litres";
        public static final String COLUMN_LITRES = "litres";
        public static final String COLUMN_SERVICE_POINT = "service_point";
        public static final String COLUMN_NOTE = "note";

        /**
         * Possible values for the type of the service
         */
        public static final int TYPE_SERVICE = 0;
        public static final int TYPE_REFUEL = 1;
        public static final int TYPE_OTHERS = 2;

        public static boolean isValidPrice(int price) {
            if (price >= 0) {
                return true;
            } else {
                return false;
            }
        }

    }
}
