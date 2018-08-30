package com.sapayth.bikeexpensemeter;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.DatePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class DatePickerPopUp extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    static long dateInMilis;

   @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);

        return new DatePickerDialog(getActivity(),
                this,
                year, month, date);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        AddServiceActivity.dateLong = c.getTimeInMillis();

        String currentDate = DateFormat.getDateInstance().format(c.getTime());
        AddServiceActivity.serviceDateTextView.setText(currentDate);
    }

    public static String formatDate(long unixDate) {
        DateFormat dateFormat = new SimpleDateFormat("d MMM, yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(unixDate);
        return dateFormat.format(calendar.getTime());
    }
}
