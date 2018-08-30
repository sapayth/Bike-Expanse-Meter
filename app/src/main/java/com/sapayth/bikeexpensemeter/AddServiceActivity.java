package com.sapayth.bikeexpensemeter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sapayth.bikeexpensemeter.data.ExpenseContract.ExpenseEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddServiceActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    static long dateLong;
    static long timeLong;

    /** Identifier for the expense data loader */
   private static final int EXISTING_EXPENSE_LOADER = 0;
    /** Content URI for the existing expense (null if it's a new expense) */
    private Uri mCurrentExpenseUri;

    private Button mSaveButton;
    static TextView serviceDateTextView;
    static TextView serviceTimeTextView;
    private EditText mOdometerEditText;

    private Spinner mExpenseTypeSpinner;
    private EditText mPriceEditText;
    private EditText mCostEditText;
    private EditText mLitresEditText;

    private EditText mTotalCostEditText;
    private EditText mServicePointEditText;
    private EditText mNoteEditText;

    private LinearLayout mRefuelLayout;
    private LinearLayout mTotalCostLayout;

    String mTime;
    String mDate;

    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(Calendar.HOUR);
    int minute = calendar.get(Calendar.MINUTE);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

    private boolean mExpenseHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        initializeAll();

        Intent intent = getIntent();
        mCurrentExpenseUri = intent.getData();

        if (mCurrentExpenseUri == null) {
            setTitle(R.string.add_service);
            getDateTime();

            serviceDateTextView.setText(mDate);
            serviceTimeTextView.setText(mTime);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a expense that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_service);
            // Initialize a loader to read the expense data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_EXPENSE_LOADER, null, this);
        }

        serviceTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddServiceActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                mTime = getAmPm(hourOfDay, minute);
                                serviceTimeTextView.setText(mTime);
                            }
                        },
                        hour, minute, false);
                timePickerDialog.show();
            }
        });

        serviceDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddServiceActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
                                mDate = dayOfMonth +"/" + (month+1) + "/" + year;
                                serviceDateTextView.setText(mDate);
                            }
                        },
                        year, month, dayOfMonth);
                datePickerDialog.show();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveExpense();
            }
        });

        mExpenseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getExpenseType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private String getAmPm(int hourOfDay, int minute) {
        String minutes = "00";
        if (minute != 0) {
            minutes = minute + "";
        }
        if (hourOfDay >= 12) {
            hourOfDay -= 12;
            return hourOfDay + ":" + minutes + "pm";
        } else {
            return hourOfDay + ":" + minutes + "am";
        }
    }

    private void initializeAll() {
        mSaveButton = findViewById(R.id.save_button);
        serviceDateTextView = findViewById(R.id.date_textview);
        serviceTimeTextView = findViewById(R.id.time_textview);

        mOdometerEditText = findViewById(R.id.odomoter_edit_text);
        mTotalCostEditText = findViewById(R.id.amount_edit_text);
        mExpenseTypeSpinner = findViewById(R.id.expense_type_spinner);
        mPriceEditText = findViewById(R.id.price_edittext);
        mCostEditText = findViewById(R.id.cost_edittext);
        mLitresEditText = findViewById(R.id.litres_edittext);
        mServicePointEditText = findViewById(R.id.servicepoint_edit_text);
        mNoteEditText = findViewById(R.id.note_edit_text);
        mSaveButton = findViewById(R.id.save_button);

        mRefuelLayout = findViewById(R.id.refuel_layout);
        mTotalCostLayout = findViewById(R.id.totalcost_layout);

        mOdometerEditText.setOnTouchListener(mTouchListener);
        mTotalCostEditText.setOnTouchListener(mTouchListener);
        mServicePointEditText.setOnTouchListener(mTouchListener);
        mNoteEditText.setOnTouchListener(mTouchListener);
        mCostEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                calculateRefuel();
                return false;
            }
        });
        mPriceEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                calculateRefuel();
                return false;
            }
        });
        mLitresEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                calculateRefuel();
                return false;
            }
        });

        Calendar calendar = Calendar.getInstance();
        dateLong = calendar.getTimeInMillis();
        timeLong = calendar.getTimeInMillis();
    }

    private int getExpenseType() {
        int position = mExpenseTypeSpinner.getSelectedItemPosition();
        switch (position) {
            case ExpenseEntry.TYPE_SERVICE:
                mRefuelLayout.setVisibility(View.GONE);
                mTotalCostLayout.setVisibility(View.VISIBLE);
                break;
            case ExpenseEntry.TYPE_REFUEL:
                mRefuelLayout.setVisibility(View.VISIBLE);
                mTotalCostLayout.setVisibility(View.GONE);
                break;
            case ExpenseEntry.TYPE_OTHERS:
                mRefuelLayout.setVisibility(View.GONE);
                mTotalCostLayout.setVisibility(View.VISIBLE);
                break;
        }

        return position;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    public void calculateRefuel() {
        boolean isPriceEmpty = true;
        boolean isCostEmpty = true;
        boolean isLitresEmpty = true;

        if (mPriceEditText.length() != 0) {
            isPriceEmpty = false;
        }

        if (mCostEditText.length() != 0) {
            isCostEmpty = false;
        }

        if (mLitresEditText.length() != 0) {
            isLitresEmpty = false;
        }
        
        if (!isPriceEmpty && !isCostEmpty) {
            mLitresEditText.setText("" + calculateLitres());
        } else if (!isLitresEmpty && !isCostEmpty) {
            mPriceEditText.setText("" + calculatePrice());
        } else  if (!isPriceEmpty && !isLitresEmpty) {
            mCostEditText.setText("" + calculateCost());
        }
    }
    
    public double calculateLitres() {
        double price = Double.parseDouble(mPriceEditText.getText().toString().trim());
        double cost = Double.parseDouble(mCostEditText.getText().toString().trim());
        
        return cost / price;
    }

    public double calculatePrice() {
        double litres = Double.parseDouble(mLitresEditText.getText().toString().trim());
        double cost = Double.parseDouble(mCostEditText.getText().toString().trim());

        return cost / litres;
    }

    public double calculateCost() {
        double litres = Double.parseDouble(mLitresEditText.getText().toString().trim());
        double cost = Double.parseDouble(mCostEditText.getText().toString().trim());

        return cost * litres;
    }

/**
 * * Perform the deletion of the expense in the database.
 */
    private void deleteExpense() {
        // Only perform the delete if this is an existing expense.
        if (mCurrentExpenseUri != null) {
            // Call the ContentResolver to delete the expense at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentexpenseUri
            // content URI already identifies the expense that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentExpenseUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "successfull",
                        Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    private void saveExpense() {
        int odo = 0;
        double pri = 0;

        if (getExpenseType() == ExpenseEntry.TYPE_SERVICE
                || getExpenseType() == ExpenseEntry.TYPE_OTHERS) {
            try {
                pri = Double.parseDouble(mTotalCostEditText.getText().toString().trim());
            } catch (NumberFormatException ex) {
                Toast.makeText(getApplicationContext(),
                        "Total cost should be a valid number",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }

        if (getExpenseType() == ExpenseEntry.TYPE_REFUEL) {
            validateRefuel();
        }

        if (serviceDateTextView.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Date cannot be blank", Toast.LENGTH_SHORT)
                    .show();
        } else if (serviceTimeTextView.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Time cannot be blank", Toast.LENGTH_SHORT)
                    .show();
        } else if (mOdometerEditText.getText().toString().trim().equals("")) {
            Toast.makeText(getApplicationContext(), "Odometer cannot be blank", Toast.LENGTH_SHORT)
                    .show();
        } else if (odo < 0) {
            Toast.makeText(getApplicationContext(), "Odometer should be minimum 0", Toast.LENGTH_SHORT)
                    .show();
        } else if (mTotalCostEditText.getText().toString().trim().equals("")
                && getExpenseType() != ExpenseEntry.TYPE_REFUEL) {
            Toast.makeText(getApplicationContext(), "Expense amount cannot be blank", Toast.LENGTH_SHORT)
                    .show();
        } else if (mPriceEditText.getText().toString().trim().equals("")
                && getExpenseType() == ExpenseEntry.TYPE_REFUEL) {
            Toast.makeText(getApplicationContext(), "Price/L cannot be blank", Toast.LENGTH_SHORT)
                    .show();
        } else if (mCostEditText.getText().toString().trim().equals("")
                && getExpenseType() == ExpenseEntry.TYPE_REFUEL) {
            Toast.makeText(getApplicationContext(), "Cost cannot be blank", Toast.LENGTH_SHORT)
                    .show();
        } else if (mLitresEditText.getText().toString().trim().equals("")
                && getExpenseType() == ExpenseEntry.TYPE_REFUEL) {
            Toast.makeText(getApplicationContext(), "Litres cannot be blank", Toast.LENGTH_SHORT)
                    .show();
        } else if (pri < 0) {
            Toast.makeText(getApplicationContext(), "Odometer should be minimum 0", Toast.LENGTH_SHORT)
                    .show();
        } else {
            int odometer = Integer.parseInt(mOdometerEditText.getText().toString().trim());
            int type = getExpenseType();
            double totalCost = 0;
            double price = 0;
            double litres = 0;

            switch (getExpenseType()) {
                case ExpenseEntry.TYPE_SERVICE:
                    totalCost = Double.parseDouble(mTotalCostEditText.getText().toString().trim());
                    price = 0;
                    litres = 0;
                    break;

                case ExpenseEntry.TYPE_REFUEL:
                    validateRefuel();
                    totalCost = Double.parseDouble(mCostEditText.getText().toString().trim());
                    price = Double.parseDouble(mPriceEditText.getText().toString().trim());
                    litres = Double.parseDouble(mLitresEditText.getText().toString().trim());
                    break;

                case ExpenseEntry.TYPE_OTHERS:
                    totalCost = Double.parseDouble(mTotalCostEditText.getText().toString().trim());
                    price = 0;
                    litres = 0;
                    break;
            }

            String servicePoint = mServicePointEditText.getText().toString().trim();
            String note = mNoteEditText.getText().toString().trim();

            ContentValues values = new ContentValues();

            values.put(ExpenseEntry.COLUMN_DATE, dateLong);
            values.put(ExpenseEntry.COLUMN_TIME, timeLong);
            values.put(ExpenseEntry.COLUMN_ODOMETER, odometer);
            values.put(ExpenseEntry.COLUMN_TYPE, type);
            values.put(ExpenseEntry.COLUMN_TOTAL_COST, totalCost);
            values.put(ExpenseEntry.COLUMN_PRICE_L, price);
            values.put(ExpenseEntry.COLUMN_LITRES, litres);
            values.put(ExpenseEntry.COLUMN_SERVICE_POINT, servicePoint);
            values.put(ExpenseEntry.COLUMN_NOTE, note);

            // Determine if this is a new or existing expense by checking if mCurrentexpenseUri is null or not
            if (mCurrentExpenseUri == null) {
                // This is a NEW expense, so insert a new expense into the provider,
                // returning the content URI for the new expense.
                Uri newUri = getContentResolver().insert(ExpenseEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this,"Expense insert failed",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, "Expense insert successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Otherwise this is an EXISTING expense, so update the expense with content URI: mCurrentexpenseUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentexpenseUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentExpenseUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, "Expense update failed",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, "Expense updated successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void validateRefuel() {
        double pricePerLitre;
        double fuelCosts;
        double fuelLitre;

        try{
            pricePerLitre = Double.parseDouble(mPriceEditText.getText().toString().trim());
            fuelCosts = Double.parseDouble(mCostEditText.getText().toString().trim());
            fuelLitre = Double.parseDouble(mLitresEditText.getText().toString().trim());
        } catch (NumberFormatException ex) {
            Toast.makeText(getApplicationContext(),
                    "Price, cost, Litres should be a valid number",
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (pricePerLitre < 0) {
            Toast.makeText(getApplicationContext(),
                    "Price/L should be a valid positive number",
                    Toast.LENGTH_SHORT)
                    .show();
        }

        if (fuelCosts < 0) {
            Toast.makeText(getApplicationContext(),
                    "Cost should be a valid positive number",
                    Toast.LENGTH_SHORT)
                    .show();
        }

        if (fuelLitre < 0) {
            Toast.makeText(getApplicationContext(),
                    "Litres should be a valid positive number",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new expense, hide the "Delete" menu item.
        if (mCurrentExpenseUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this expense?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the expense.
                deleteExpense();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the expense.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDate = DateFormat.getDateInstance().format(c.getTime());
        serviceDateTextView.setText(currentDate);
    }

    private void getDateTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String amPm = "am";

        if (hour >= 12) {
            amPm = "pm";
            hour = hour % 12;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
        mDate = sdf.format(calendar.getTime());
        mTime = hour + ":" + minute + amPm;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all expense attributes, define a projection that contains
        // all columns from the expense table
        String[] projection = {
                ExpenseEntry._ID,
                ExpenseEntry.COLUMN_DATE,
                ExpenseEntry.COLUMN_TIME,
                ExpenseEntry.COLUMN_ODOMETER,
                ExpenseEntry.COLUMN_TYPE,
                ExpenseEntry.COLUMN_TOTAL_COST,
                ExpenseEntry.COLUMN_PRICE_L,
                ExpenseEntry.COLUMN_LITRES,
                ExpenseEntry.COLUMN_SERVICE_POINT,
                ExpenseEntry.COLUMN_NOTE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // parent activity context
                mCurrentExpenseUri,       // provider content URI to query
                projection,                     // columns to include in the resulting function
                null,                  // no selection clause
                null,               // no selection argument
                "odometer DESC");         // default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            int expenseTypeColumnIndex = cursor.getColumnIndex(ExpenseEntry.COLUMN_TYPE);
            int priceColumnIndex = cursor.getColumnIndex(ExpenseEntry.COLUMN_TOTAL_COST);
            int expenseType = cursor.getInt(expenseTypeColumnIndex);

            if (expenseType == ExpenseEntry.TYPE_REFUEL) {
                mExpenseTypeSpinner.setSelection(ExpenseEntry.TYPE_REFUEL);
                int priceL = cursor.getColumnIndex(ExpenseEntry.COLUMN_PRICE_L);
                int litres = cursor.getColumnIndex(ExpenseEntry.COLUMN_LITRES);
                mPriceEditText.setText(cursor.getString(priceL));
                mLitresEditText.setText(cursor.getString(litres));
                mCostEditText.setText(cursor.getString(priceColumnIndex));

            } else {
                mTotalCostEditText.setText(cursor.getString(priceColumnIndex));
            }

            // Find the columns of expense attributes that we're interested in
            int dateColumnIndex = cursor.getColumnIndex(ExpenseEntry.COLUMN_DATE);
            int timeColumnIndex = cursor.getColumnIndex(ExpenseEntry.COLUMN_TIME);
            int odometerColumnIndex = cursor.getColumnIndex(ExpenseEntry.COLUMN_ODOMETER);
            int servicePointColumnIndex = cursor.getColumnIndex(ExpenseEntry.COLUMN_SERVICE_POINT);
            int noteColumnIndex = cursor.getColumnIndex(ExpenseEntry.COLUMN_NOTE);

            // Extract out the value from the Cursor for the given column index
            dateLong = cursor.getLong(dateColumnIndex);
            timeLong = cursor.getLong(timeColumnIndex);
            String odometer = cursor.getString(odometerColumnIndex);
            String servicePoint = cursor.getString(servicePointColumnIndex);
            String note = cursor.getString(noteColumnIndex);

            // Update the views on the screen with the values from the database
            serviceDateTextView.setText(mDate);
            serviceTimeTextView.setText(mTime);
            mOdometerEditText.setText(odometer);
            mServicePointEditText.setText(servicePoint);
            mNoteEditText.setText(note);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mOdometerEditText.setText("");
        mTotalCostEditText.setText("");
        mPriceEditText.setText("");
        mCostEditText.setText("");
        mLitresEditText.setText("");
        mServicePointEditText.setText("");
        mNoteEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the expense.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
// the view, and we change the mexpenseHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mExpenseHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        // If the expense hasn't changed, continue with handling back button press
        if (!mExpenseHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case android.R.id.home:
                // If the expense hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mExpenseHasChanged) {
                    NavUtils.navigateUpFromSameTask(AddServiceActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(AddServiceActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

            case R.id.action_save:
                saveExpense();
                return true;

            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}