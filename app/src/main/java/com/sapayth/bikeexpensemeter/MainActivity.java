package com.sapayth.bikeexpensemeter;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sapayth.bikeexpensemeter.data.ExpenseContract.ExpenseEntry;
import com.sapayth.bikeexpensemeter.data.ExpenseDbHelper;
import com.sapayth.bikeexpensemeter.utils.Expense;
import com.sapayth.bikeexpensemeter.utils.ExpenseCursorAdapter;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXPENSE_LOADER = 0;
    ExpenseCursorAdapter mCursorAdapter;

    LinearLayout mEmptyViewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getApplicationContext().deleteDatabase("expenses.db");

        // TodoDatabaseHandler is a SQLiteOpenHelper class connecting to SQLite
        ExpenseDbHelper helper = new ExpenseDbHelper(this);
        // Get access to the underlying writeable database
        SQLiteDatabase database = helper.getWritableDatabase();

        mEmptyViewLayout = findViewById(R.id.emptyview_layout);

        // Find ListView to populate
        ListView listView = findViewById(R.id.list);
        // Setup cursor adapter using cursor from last step
        mCursorAdapter = new ExpenseCursorAdapter(this, null);
        // Attach cursor adapter to the ListView
        listView.setAdapter(mCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, AddServiceActivity.class);
                Uri currentExpenseUri = ContentUris.withAppendedId(ExpenseEntry.CONTENT_URI, id);
                intent.setData(currentExpenseUri);
                startActivity(intent);
            }
        });

        listView.setEmptyView(mEmptyViewLayout);

        getLoaderManager().initLoader(EXPENSE_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert:
                Intent serviceIntent = new Intent(this, AddServiceActivity.class);
                startActivity(serviceIntent);
                break;
            case R.id.action_report:
                Intent reportIntent = new Intent(this, ReportActivity.class);
                startActivity(reportIntent);
                break;
            case R.id.action_credit:
                Intent creditIntent = new Intent(this, CreditActivity.class);
                startActivity(creditIntent);
                break;
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
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

        return new CursorLoader(this,   // parent activity context
                ExpenseEntry.CONTENT_URI,       // provider content URI to query
                projection,                     // columns to include in the resulting function
                null,                  // no selection clause
                null,               // no selection argument
                "odometer DESC");      // default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
