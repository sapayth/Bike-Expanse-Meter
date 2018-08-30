package com.sapayth.bikeexpensemeter.utils;

/**
 * Created by S6H on 1/26/2018.
 */

public class Expense {
    private int id;
    private String date;
    private String time;
    private String odometer;
    private String amount;
    private String purpose;
    private String servicePoint;
    private String note;

    public Expense(int id, String date, String time, String odometer, String amount, String purpose, String servicePoint, String note) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.odometer = odometer;
        this.amount = amount;
        this.purpose = purpose;
        this.servicePoint = servicePoint;
        this.note = note;
    }

    public Expense(String date, String odometer, String amount, String purpose) {
        this.date = date;
        this.odometer = odometer;
        this.amount = amount;
        this.purpose = purpose;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getOdometer() {
        return odometer;
    }

    public String getAmount() {
        return amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getServicePoint() {
        return servicePoint;
    }

    public String getNote() {
        return note;
    }
}
