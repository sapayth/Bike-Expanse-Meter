package com.sapayth.bikeexpensemeter.data;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sapayth.bikeexpensemeter.data.ExpenseContract.ExpenseEntry;

/**
 * Created by S6H on 1/26/2018.
 */

public class ExpenseDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 1;

    public ExpenseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_EXPENSE_TABLE =  "CREATE TABLE " + ExpenseEntry.TABLE_NAME + "("
                + ExpenseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ExpenseEntry.COLUMN_DATE + " REAL NOT NULL, "
                + ExpenseEntry.COLUMN_TIME + " REAL NOT NULL, "
                + ExpenseEntry.COLUMN_ODOMETER + " REAL NOT NULL, "
                + ExpenseEntry.COLUMN_TYPE + " INTEGER NOT NULL, "
                + ExpenseEntry.COLUMN_TOTAL_COST + " REAL NOT NULL DEFAULT 0, "
                + ExpenseEntry.COLUMN_PRICE_L + " REAL NOT NULL DEFAULT 0, "
                + ExpenseEntry.COLUMN_LITRES + " REAL NOT NULL DEFAULT 0, "
                + ExpenseEntry.COLUMN_SERVICE_POINT + " TEXT, "
                + ExpenseEntry.COLUMN_NOTE + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_EXPENSE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}
}
