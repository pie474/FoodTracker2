package com.example.foodtracker;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class Food implements Comparable<Food> {
    Date expDate;
    String type;
    public Food(Date expDate, String type){
        this.expDate = expDate;
        this.type = type;
    }

    public String toString(){
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return type + " (Expiration Date: " + formatter.format(expDate) +")";
    }

    public String getType() {
        return type;
    }

    public Date getExpDate() {
        return expDate;
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
}