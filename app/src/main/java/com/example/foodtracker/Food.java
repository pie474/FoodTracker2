package com.example.foodtracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import androidx.annotation.IntDef;

public class Food implements Comparable<Food> {
    public static final String NAME_TAG = "name";
    public static final String EXP_DATE_TAG = "exp_date";
    public static final String EXP_DAY_TAG = "exp_day";
    public static final String EXP_MONTH_TAG = "exp_month";
    public static final String EXP_YEAR_TAG = "exp_year";

    public static final String[] ALL_ATTRS = new String[] {NAME_TAG, EXP_MONTH_TAG};

    @IntDef({EXP_STATE_OK, EXP_STATE_ALMOST, EXP_STATE_EXPIRED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExpState {}

    public static final int EXP_STATE_OK = 0;
    public static final int EXP_STATE_ALMOST = 1;
    public static final int EXP_STATE_EXPIRED = 2;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM dd yyyy");

    private LocalDate expDate;
    private String type;
    public Food(LocalDate expDate, String type) {
        this.expDate = expDate;
        this.type = type;
    }

    public String toString() {
        return type + " (Expiration Date: " + getFormattedDate() +")";
    }

    public String getType() {
        return type;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public String getFormattedDate() {
        return expDate.format(DATE_FORMATTER);
    }

    @ExpState
    public int getExpirationState() {
        LocalDate today = LocalDate.now();
        if(today.isAfter(expDate)) {
            return EXP_STATE_EXPIRED;
        } else if(today.isAfter(expDate.minusDays(3))) {
            return EXP_STATE_ALMOST;
        } else {
            return EXP_STATE_OK;
        }
    }

    @Override
    public int compareTo(Food other) {
        return this.expDate.compareTo(other.expDate);
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject out = new JSONObject();
        out.put("type", type);
        out.put("date", getFormattedDate());
        return out;
    }

    public static Food fromJson(JSONObject json) throws JSONException, ParseException {
        return new Food(LocalDate.parse((String)json.get("date"), DATE_FORMATTER), (String)json.get("type"));
    }
}
