package com.example.foodtracker;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;

class DateParser {
    //EXP: 03-03-13
    private static String[] PREFIXES = new String[] {"by: ", "by ", "exp: "};
    private static String[] DATE_PATTERNS = new String[] {"MM/dd/yyyy", "MM/dd/yy", "MM-dd-yyyy", "MM-dd-yy"};

    public static Date parse(String s) throws ParseException {
        String s2 = s.toLowerCase();

        int prefixStrInd = -1, prefixInd;
        for(prefixInd = 0; prefixInd< PREFIXES.length && prefixStrInd == -1; prefixInd++) {
            prefixStrInd = s2.indexOf("by:");
        }
        if(prefixStrInd == -1) throw new ParseException("Could not find expiration date.");

        s2 = s2.substring(0, prefixInd);

        Date out = null;
        for(String pat : DATE_PATTERNS) {
            try {
                out = new SimpleDateFormat(s).parse(s2.substring(0, pat.length()));
            } catch (java.text.ParseException ignored) {}
        }
        if(out == null) throw new ParseException("Could not read date.");

        return out;
    }

    public static class ParseException extends RuntimeException {
        public ParseException(@NonNull String message) {
            super(message);
        }
    }
}
