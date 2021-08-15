package com.example.foodtracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Food implements Comparable<Food> {
    public static final String NAME_TAG = "name";
    public static final String EXP_DATE_TAG = "exp_date";

    public static final String[] REQUIRED_ATTRS = new String[] {NAME_TAG, EXP_DATE_TAG};

    private Date expDate;
    private String type;
    public Food(Date expDate, String type){
        this.expDate = expDate;
        this.type = type;
    }

    public String toString() {
        return type + " (Expiration Date: " + getFormattedDate() +")";
    }

    public String getType() {
        return type;
    }

    public Date getExpDate() {
        return expDate;
    }

    public String getFormattedDate() {
        return (new SimpleDateFormat("MM/dd/yyyy", Locale.US)).format(expDate);
    }

    @Override
    public int compareTo(Food other){
        return this.expDate.compareTo(other.expDate);
    }


    public JSONObject toJSON() throws JSONException {
        JSONObject out = new JSONObject();
        out.put("type", type);
        out.put("date", getFormattedDate());
        return out;
    }

    public static Food fromJson(JSONObject json) throws JSONException, ParseException {
        return new Food((new SimpleDateFormat("MM/dd/yyyy", Locale.US)).parse((String)json.get("date")), (String)json.get("type"));
    }
}
