package com.sapayth.bikeexpensemeter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sapayth.bikeexpensemeter.data.ExpenseDbHelper;
import com.sapayth.bikeexpensemeter.utils.CurrencyUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

public class ReportActivity extends AppCompatActivity {

    long[] expenses = new long[3];
    String[] expenseName = new String[3];
    String[] currencyArray = {"AFA","ALL","DZD","USD","EUR","AOA","XCD","NOK","XCD","ARA","AMD","AWG","AUD","EUR","AZM","BSD","BHD","BDT","BBD","BYR","EUR","BZD","XAF","BMD","BTN","BOB","BAM","BWP","NOK","BRL","GBP","BND","BGN","XAF","BIF","KHR","XAF","CAD","CVE","KYD","XAF","XAF","CLF","CNY","AUD","AUD","COP","KMF","CDZ","XAF","NZD","CRC","HRK","CUP","EUR","CZK","DKK","DJF","XCD","DOP","TPE","USD","EGP","USD","XAF","ERN","EEK","ETB","FKP","DKK","FJD","EUR","EUR","EUR","EUR","XPF","EUR","XAF","GMD","GEL","EUR","GHC","GIP","EUR","DKK","XCD","EUR","USD","GTQ","GNS","GWP","GYD","HTG","AUD","EUR","HNL","HKD","HUF","ISK","INR","IDR","IRR","IQD","EUR","ILS","EUR","XAF","JMD","JPY","JOD","KZT","KES","AUD","KPW","KRW","KWD","KGS","LAK","LVL","LBP","LSL","LRD","LYD","CHF","LTL","EUR","MOP","MKD","MGF","MWK","MYR","MVR","XAF","EUR","USD","EUR","MRO","MUR","EUR","MXN","USD","MDL","EUR","MNT","XCD","MAD","MZM","MMK","NAD","AUD","NPR","EUR","ANG","XPF","NZD","NIC","XOF","NGN","NZD","AUD","USD","NOK","OMR","PKR","USD","PAB","PGK","PYG","PEI","PHP","NZD","PLN","EUR","USD","QAR","EUR","ROL","RUB","RWF","XCD","XCD","XCD","WST","EUR","STD","SAR","XOF","EUR","SCR","SLL","SGD","EUR","EUR","SBD","SOS","ZAR","GBP","EUR","LKR","SHP","EUR","SDG","SRG","NOK","SZL","SEK","CHF","SYP","TWD","TJR","TZS","THB","XAF","NZD","TOP","TTD","TND","TRY","TMM","USD","AUD","UGS","UAH","SUR","AED","GBP","USD","USD","UYU","UZS","VUV","VEF","VND","USD","USD","XPF","XOF","MAD","ZMK","USD"};

    String currencyShortCode = "BDT";

    String currency = CurrencyUtil.getCurrencySymbol(currencyShortCode);;

    private final int NO_REPORT = -1;

    TextView mServiceTextView;
    TextView mRefuelTextView;
    TextView mOthersTextView;
    TextView mTotalTextView;

    LinearLayout mEmptyLayout;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        initializeAll();
        setupPieChart();
    }

    private void initializeAll() {
        mServiceTextView = findViewById(R.id.service_textview);
        mRefuelTextView = findViewById(R.id.refuel_textview);
        mOthersTextView = findViewById(R.id.others_textview);
        mTotalTextView = findViewById(R.id.total_textview);

        mEmptyLayout = findViewById(R.id.emptyreport_layout);

        expenses[0] = getServicesSum();
        expenses[1] = getRefuelSum();
        expenses[2] = getOthersSum();

        expenseName[0] = getResources().getString(R.string.tv_service);
        expenseName[1] = getResources().getString(R.string.tv_refuel);
        expenseName[2] = getResources().getString(R.string.tv_others);
    }

    private void setupPieChart() {
        if(expenses[0] < 1
                && expenses[1] < 1
                && expenses[2] < 1) {
            mEmptyLayout.setVisibility(View.VISIBLE);

        } else {
            List<PieEntry> pieEntries = new ArrayList<>();

            for (int i = 0; i < expenses.length; i++) {
                pieEntries.add(new PieEntry(expenses[i], expenseName[i]));
            }

            PieDataSet dataSet = new PieDataSet(pieEntries, null);
            dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            PieData pieData = new PieData(dataSet);

            Description description = new Description();
            description.setText("");

            PieChart chart = findViewById(R.id.pie_chart);
            chart.setData(pieData);
            chart.setDescription(description);
            chart.setNoDataText(getResources().getString(R.string.no_data));
            chart.setHoleRadius(20f);
            chart.setTransparentCircleRadius(40f);
            chart.animateY(1000);
            chart.invalidate();

            long total = expenses[0] + expenses[1] + expenses[2];

            mTotalTextView.setText( getResources().getString(R.string.tv_total) + " " + currency + total);
        }
    }

    public long getServicesSum() {
        // TodoDatabaseHandler is a SQLiteOpenHelper class connecting to SQLite
        ExpenseDbHelper helper = new ExpenseDbHelper(this);
        // Get access to the underlying writeable database
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT SUM(total_cost) FROM expenses where type = 0", null);

        if(cursor.moveToFirst()) {
            mServiceTextView.setText(getResources().getString(R.string.tv_service) + " " + currency
                    + cursor.getInt(0));
            return cursor.getInt(0);
        }

        return  NO_REPORT;
    }

    public long getRefuelSum() {
        // TodoDatabaseHandler is a SQLiteOpenHelper class connecting to SQLite
        ExpenseDbHelper helper = new ExpenseDbHelper(this);
        // Get access to the underlying writeable database
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT SUM(total_cost) FROM expenses where type = 1", null);

        if(cursor.moveToFirst()) {
            mRefuelTextView.setText(getResources().getString(R.string.tv_refuel) +  " " + currency
                    + cursor.getInt(0));
            return cursor.getInt(0);
        }

        return  NO_REPORT;
    }

    public long getOthersSum() {
        // TodoDatabaseHandler is a SQLiteOpenHelper class connecting to SQLite
        ExpenseDbHelper helper = new ExpenseDbHelper(this);
        // Get access to the underlying writeable database
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT SUM(total_cost) FROM expenses where type = 2", null);

        if(cursor.moveToFirst()) {
            mOthersTextView.setText(getResources().getString(R.string.tv_others) + " " + currency
                    + cursor.getInt(0));
            return cursor.getInt(0);
        }

        return  NO_REPORT;
    }
}
