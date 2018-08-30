package com.sapayth.bikeexpensemeter.utils;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sapayth.bikeexpensemeter.DatePickerPopUp;
import com.sapayth.bikeexpensemeter.R;
import com.sapayth.bikeexpensemeter.data.ExpenseContract.ExpenseEntry;

/**
 * Created by S6H on 1/28/2018.
 */

public class ExpenseCursorAdapter extends CursorAdapter {
    String currency = CurrencyUtil.getCurrencySymbol("BDT");;

    public ExpenseCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(
                R.layout.activity_single_expense, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        ImageView ivExpense = view.findViewById(R.id.expense_imageview);
        TextView tvExpenseType = view.findViewById(R.id.expensetype_textview);
        TextView tvNote = view.findViewById(R.id.note_textview);
        TextView tvCost = view.findViewById(R.id.cost_textview);
        TextView tvOdometer = view.findViewById(R.id.odometer_textview);
        TextView tvDate = view.findViewById(R.id.date_textview);

        // Extract properties from cursor
        String expenseType = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_TYPE));
        String note = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_NOTE));
        String cost = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_TOTAL_COST));
        String odometer = cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_ODOMETER));
        long dateUnix = cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_DATE));

        int expenseTypeInt = Integer.parseInt(expenseType.trim());

        // Populate fields with extracted properties
        switch (expenseTypeInt) {
            case ExpenseEntry.TYPE_SERVICE:
                tvExpenseType.setText("Service");
                tvCost.setText(currency + cost);
                ivExpense.setImageResource(R.drawable.type_service);
                break;

            case ExpenseEntry.TYPE_REFUEL:
                tvExpenseType.setText("Refuel");
                tvCost.setText(currency + cost);
                ivExpense.setImageResource(R.drawable.type_refuel);
                break;

            case ExpenseEntry.TYPE_OTHERS:
                tvExpenseType.setText("Others");
                tvCost.setText(currency + cost);
                ivExpense.setImageResource(R.drawable.type_others);
                break;
        }

        if (!note.equals("")) {
            tvNote.setVisibility(View.VISIBLE);
            tvNote.setText(note);
        }


        tvOdometer.setText(odometer + "km");
        tvDate.setText(DatePickerPopUp.formatDate(dateUnix));
    }
}