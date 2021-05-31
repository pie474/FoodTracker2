package com.example.foodtracker;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;

public class Food implements Comparable<Food> {
    Date expDate;
    String type;
    public Food(Date expDate, String type){
        this.expDate = expDate;
        this.type = type;
    }

    public String toString(){

        return type + " (Expiration Date: " + getFormattedDate() +")";
    }

    public String getType() {
        return type;
    }

    public Date getExpDate() {
        return expDate;
    }


    public String getFormattedDate() {
        return (new SimpleDateFormat("MM/dd/yyyy")).format(expDate);
    }

    public String getRecipe() {
        return "https://www.google.com/search?q=" + this.getType() + "+recipes";
    }

    public String getOnlineShop() {
        return "https://www.google.com/search?tbm=shop&q=" + this.getType();
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
        return new Food((new SimpleDateFormat("MM/dd/yyyy")).parse((String)json.get("date")), (String)json.get("type"));
    }
}
