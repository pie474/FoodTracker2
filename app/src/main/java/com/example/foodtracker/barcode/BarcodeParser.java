package com.example.foodtracker.barcode;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class BarcodeParser extends Thread {
    private static final String BASE_URL = "https://www.amazon.com/";
    private static final String SEARCH_EXTENSION = "s?k=";
    private static final String PRODUCT_EXTENSION = "dp/";
    private static final String USER_AGENT = "Safari/537.36";

    private OnBarcodeParsedListener parsedListener = null;
    private OnBarcodeParseFailedListener failureListener = null;
    private final String upc;

    public BarcodeParser(String upc) {
        this.upc = upc;
    }


    public BarcodeParser addParsedListener(OnBarcodeParsedListener listener) {
        parsedListener = listener;
        return this;
    }

    public BarcodeParser addFailureListener(OnBarcodeParseFailedListener listener) {
        failureListener = listener;
        return this;
    }

    /*public void parse(String upc) {
        this.upc = upc;
        start();
    }*/


    @Override
    public void run() {
        try {
            if(!validUPC(upc)) throw new IllegalArgumentException(String.format("\"%s\" is not a valid UPC code", upc));
            if(parsedListener != null) parsedListener.onParsed(getProductTitle(getASIN(upc)));
        } catch(Exception e) {
            if(failureListener != null) failureListener.onFailure(e);
        }
    }


    private String getASIN(String upcCode) throws IOException, ItemNotFoundException {
        Document d = Jsoup.connect(BASE_URL + SEARCH_EXTENSION + upcCode).userAgent(USER_AGENT).get();

        int ct = 0;
        for(Element e : d.body().select("div[data-asin]")) {
            if(!e.attr("data-asin").equals("")) ct++;
            if(ct==4) return e.attr("data-asin");
        }

        throw new ItemNotFoundException();
    }

    private String getProductTitle(String asin) throws IOException {
        Document d = Jsoup.connect(BASE_URL + PRODUCT_EXTENSION + asin).userAgent(USER_AGENT).get();

        String title = d.body().getElementById("productTitle").ownText();
        return title;
    }

    public static boolean validUPC(String upc) {
        if(upc.length() != 12) return false;
        try {
            Long.valueOf(upc);
        } catch(NumberFormatException ex) {
            return false;
        }
        return true;
    }

}
