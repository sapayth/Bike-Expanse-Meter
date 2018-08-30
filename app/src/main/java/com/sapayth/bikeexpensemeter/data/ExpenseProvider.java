package com.sapayth.bikeexpensemeter.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.sapayth.bikeexpensemeter.utils.Expense;

import static com.sapayth.bikeexpensemeter.data.ExpenseContract.ExpenseEntry;

/**
 * Created by S6H on 1/27/2018.
 */

public class ExpenseProvider extends ContentProvider {

    // checked

    /** Tag for the log messages */
    public static final String LOG_TAG = ExpenseProvider.class.getSimpleName();

    private ExpenseDbHelper mDbHelper;

    /** URI matcher code for the content URI for the expenses table */
    private static final int EXPENSES = 100;
    /** URI matcher code for the content URI for a single expense in the expenses table */
    private static final int EXPENSE_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ExpenseContract.CONTENT_AUTHORITY, ExpenseContract.PATH_EXPENSES, EXPENSES);
        sUriMatcher.addURI(ExpenseContract.CONTENT_AUTHORITY, ExpenseContract.PATH_EXPENSES + "/#", EXPENSE_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new ExpenseDbHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor = null;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSES:
                cursor = database.query(ExpenseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case EXPENSE_ID:
                selection = ExpenseEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ExpenseEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                Toast.makeText(getContext(), "Cannot query URI " + uri, Toast.LENGTH_SHORT).show();
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
            final int match = sUriMatcher.match(uri);
            switch (match) {
                case EXPENSES:
                    return insertExpense(uri, contentValues);
                default:
                    Toast.makeText(getContext(), "Insertion is not supported for  " + uri, Toast.LENGTH_SHORT).show();
            }
            return uri;
        }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSES:
                return updateExpense(uri, contentValues, selection, selectionArgs);
            case EXPENSE_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ExpenseEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateExpense(uri, contentValues, selection, selectionArgs);
            default:
                Toast.makeText(getContext(), "Update is not supported for " + uri, Toast.LENGTH_SHORT).show();
        }
        return -1;
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateExpense(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ExpenseEntry#COLUMN_TYPE} key is present,
        // check that the type value is not null.
        if (values.containsKey(ExpenseEntry.COLUMN_TYPE)) {
            String purpose = values.getAsString(ExpenseEntry.COLUMN_TYPE);
            if (purpose == null) {
                Toast.makeText(getContext(), "Expense requires a type", Toast.LENGTH_SHORT).show();
            }
        }

        // If the {@link ExpenseEntry#COLUMN_TOTAL_COST} key is present,
        // check that the total cost value is valid.
        if (values.containsKey(ExpenseEntry.COLUMN_TOTAL_COST)) {
            Integer price = values.getAsInteger(ExpenseEntry.COLUMN_TOTAL_COST);
            if (price == null || !ExpenseEntry.isValidPrice(price)) {
                Toast.makeText(getContext(), "Price requires valid number", Toast.LENGTH_SHORT).show();
            }
        }

        // If the {@link ExpenseEntry#COLUMN_ODOMETER} key is present,
        // check that the odometer value is valid.
        if (values.containsKey(ExpenseEntry.COLUMN_ODOMETER)) {
            // Check that the odometer is greater than or equal to 0 kg
            Integer odometer = values.getAsInteger(ExpenseEntry.COLUMN_ODOMETER);
            if (odometer != null && odometer < 0) {
                Toast.makeText(getContext(), "Odometer cannot be less then 0", Toast.LENGTH_SHORT).show();
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ExpenseEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted = 0;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ExpenseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case EXPENSE_ID:
                // Delete a single row given by the ID in the URI
                selection = ExpenseEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ExpenseEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                Toast.makeText(getContext(), "Deletion is not supported for " + uri, Toast.LENGTH_SHORT).show();
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case EXPENSES:
                return ExpenseEntry.CONTENT_LIST_TYPE;
            case EXPENSE_ID:
                return ExpenseEntry.CONTENT_ITEM_TYPE;
            default:
                Toast.makeText(getContext(), "Unknown URI " + uri + " with match " + match,
                        Toast.LENGTH_SHORT).show();
                return "stub";
        }
    }

    private Uri insertExpense(Uri uri, ContentValues values) {

        // Check that the date is not null
        String date = values.getAsString(ExpenseEntry.COLUMN_DATE);
        if (date == null) {
            Toast.makeText(getContext(), "Requires a date", Toast.LENGTH_SHORT).show();
        }

        // Check that the time is not null
        String time = values.getAsString(ExpenseEntry.COLUMN_TIME);
        if (time == null) {
            Toast.makeText(getContext(), "Requires a time", Toast.LENGTH_SHORT).show();
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        Integer odometer = values.getAsInteger(ExpenseEntry.COLUMN_ODOMETER);
        if (odometer != null && odometer < 0) {
            Toast.makeText(getContext(), "Requires a valid odometer", Toast.LENGTH_SHORT).show();
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new pet with the given values
        long id = database.insert(ExpenseEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Toast.makeText(getContext(), "Failed to insert row for " + uri, Toast.LENGTH_SHORT).show();
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }
}
