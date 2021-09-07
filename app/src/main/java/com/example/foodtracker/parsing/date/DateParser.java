package com.example.foodtracker.parsing.date;

import android.graphics.Point;

import com.google.mlkit.vision.text.Text;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import androidx.core.util.Pair;

public class DateParser {
    private static final int DAY_INDEX = 1;
    private static final int MONTH_INDEX = 0;
    private static final int YEAR_INDEX = 2;

    //MAKE SURE WHEN ADDING NEW ALIASES TO HAVE THE LONGEST ALIASES FIRST
    private static final ArrayList<Pair<String, Integer>> LETTERED_MONTHS = new ArrayList<Pair<String, Integer>>() {{
        add(new Pair<>("jan", 1));
        add(new Pair<>("feb", 2));
        add(new Pair<>("mar", 3));
        add(new Pair<>("apr", 4));
        add(new Pair<>("may", 5));
        add(new Pair<>("jun", 6));
        add(new Pair<>("jul", 7));
        add(new Pair<>("aug", 8));
        add(new Pair<>("sep", 9));
        add(new Pair<>("oct", 10));
        add(new Pair<>("nov", 11));
        add(new Pair<>("dec", 12));
        add(new Pair<>("de" , 12));
    }};

    public static List<FoundDate> parse(Text.Line text) {
        List<FoundDate> out = new ArrayList<>();
        String textS = text.getText().toLowerCase();

        for(Pair<String, Integer> p : LETTERED_MONTHS) {
            textS = textS.replace(p.first, " "+TYPE_DATA[MONTH_INDEX]+""+p.second+" ");
        }

        List<String> validSequences = split(textS, NUMBERS, DELIMITERS, TYPE_DATA);
        Point[] cps = text.getCornerPoints();

        for(String seq : validSequences) {
            if(seq.length()<5) continue;
            List<String> numSeq = split(seq, NUMBERS, TYPE_DATA);
            if(numSeq.size() <= 1 || numSeq.size() > 3) continue;

            int ind = textS.indexOf(seq);//assert ind != -1
            double left = (double)ind/textS.length();
            double right = (double)(ind+seq.length())/textS.length();

            int[] parsedNumSeq = new int[numSeq.size()];

            //generate constraint system
            ConditionState[][] conditions = new ConditionState[3][3];//  [item][month/day/year]
            for(int i = 0; i<numSeq.size(); i++) {
                Pair<Integer, Optional<Integer>> val = parseInt(numSeq.get(i));
                parsedNumSeq[i] = val.first;
                if(val.second.isPresent()) {//this was previously known to be something (100% month at this point)
                    //for(int j = 0; j<3; j++) conditions[j][val.second.get()] = new ConditionState(false, true);
                    //for(int j = 0; j<3; j++) conditions[i][j] = new ConditionState(j==val.second.get(), true);
                    confirmInSystem(conditions, i, val.second.get());
                } else {
                    if(conditions[i][MONTH_INDEX]==null) conditions[i][MONTH_INDEX] = new ConditionState(validMonth(val.first));
                    if(conditions[i][DAY_INDEX]==null) conditions[i][DAY_INDEX] = new ConditionState(validDay(val.first));
                    if(conditions[i][YEAR_INDEX]==null) conditions[i][YEAR_INDEX] = new ConditionState(validYear(val.first));
                }
            }
            if(numSeq.size() == 2) for(int j = 0; j<3; j++) conditions[2][j] = new ConditionState(false, true);

            simplifySystem(conditions);


            int month = 1, day = 28, year = LocalDate.now().getYear();
            //solution selector
            for(int i = numSeq.size()-1; i>=0; i--) {
                for(int j = 2; j>=0; j--) {
                    if(conditions[i][j].v) {
                        confirmInSystem(conditions, i, j);
                        break;
                    }
                }
                if(conditions[i][DAY_INDEX].v) day = parsedNumSeq[i];
                if(conditions[i][MONTH_INDEX].v) month = parsedNumSeq[i];
                if(conditions[i][YEAR_INDEX].v) year = parsedNumSeq[i];
            }
            if(year<500) {
                year += 2000;
            } else if(year<1000) {
                year += 1000;
            }

            if(cps != null) {
                Point[] seqCornerPoints = new Point[] {
                        new Point((int)((cps[1].x-cps[0].x)*left) + cps[0].x, (int)((cps[1].y-cps[0].y)*left) + cps[0].y),
                        new Point((int)((cps[1].x-cps[0].x)*right) + cps[0].x, (int)((cps[1].y-cps[0].y)*right) + cps[0].y),
                        new Point((int)((cps[2].x-cps[3].x)*right) + cps[3].x, (int)((cps[2].y-cps[3].y)*right) + cps[3].y),
                        new Point((int)((cps[2].x-cps[3].x)*left) + cps[3].x, (int)((cps[2].y-cps[3].y)*left) + cps[3].y)
                };

                try {
                    out.add(new FoundDate(LocalDate.of(year, month, day), seqCornerPoints));
                } catch (Exception ignored) {}
            } else {
                out.add(new FoundDate(LocalDate.of(year, month, day), null));
            }
        }

        return out;
    }

    private static void confirmInSystem(ConditionState[][] sys, int row, int col) {
        for(int i = 0; i<3; i++) {
            sys[row][i] = new ConditionState(false, true);
            sys[i][col] = new ConditionState(false, true);
        }
        sys[row][col] = new ConditionState(true, true);
    }

    private static void simplifySystem(ConditionState[][] conditions) {
        ConditionState[][] prevState = null;
        while(!Arrays.deepEquals(conditions, prevState)) {
            prevState = copy(conditions);

            for(int i = 0; i<3; i++) {
                int ctRow = 0, posRow = 0, ctCol = 0, posCol = 0;
                for(int j = 0; j<3; j++) {
                    if(conditions[i][j].v) {
                        ctRow++;
                        posRow = j;
                    }
                    if(conditions[j][i].v) {
                        ctCol++;
                        posCol = j;
                    }
                }
                if(ctRow == 1) {
                    confirmInSystem(conditions, i, posRow);
                }
                if(ctCol == 1) {
                    int otherCount = 0;
                    for(int j = 0; j<3; j++) if(conditions[posCol][j].v && countSystemCol(conditions, j)==1) otherCount++;
                    if(countSystemRow(conditions, 2) > 0 || otherCount==1) confirmInSystem(conditions, posCol, i);
                }
            }
        }
    }

    private static ConditionState[][] copy(ConditionState[][] inp) {
        ConditionState[][] out = new ConditionState[inp.length][inp[0].length];
        for(int i = 0; i<inp.length; i++) {
            for(int j = 0; j<inp[0].length; j++) {
                out[i][j] = inp[i][j].clone();
            }
        }
        return out;
    }

    private static int countSystemRow(ConditionState[][] system, int row) {
        int a = 0;
        for(int i = 0; i<3; i++) if(system[row][i].v) a++;
        return a;
    }

    private static int countSystemCol(ConditionState[][] system, int col) {
        int a = 0;
        for(int i = 0; i<3; i++) if(system[i][col].v) a++;
        return a;
    }



    private static final char[] NUMBERS = new char[] {'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};
    private static final char[] DELIMITERS = new char[] {'-', '/', ' ', '.'};

    static ArrayList<String> split(String inp, char[]... validChars) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(char c : inp.toCharArray()) {
            boolean valid = false;
            for(char[] carr : validChars) {
                for(char ch : carr) {
                    if(c==ch) {
                        valid = true;
                        break;
                    }
                }
                if(valid) break;
            }

            if(valid) {
                sb.append(c);
            } else {
                String s = sb.toString();
                if(s.length() > 0) {
                    out.add(s);
                    sb = new StringBuilder();
                }
            }
        }
        if(sb.toString().length() > 0) {
            out.add(sb.toString());
        }
        return out;
    }

    //month   = '#'
    //day = '$'
    //year  = '%'
    //(mapped according to index variables above)
    //guaranteed that only one of these will exist, and that if it does, it will be the first char
    private static final char[] TYPE_DATA = new char[] {'#', '$', '%'};
    private static Pair<Integer, Optional<Integer>> parseInt(String inp) {
        if(inp.charAt(0) == '#' || inp.charAt(0) == '$' || inp.charAt(0) == '%') {
            return new Pair<>(Integer.parseInt(inp.substring(1)), Optional.of(inp.charAt(0) - 35));
        } else {
            return new Pair<>(Integer.parseInt(inp), Optional.empty());
        }
    }

    public static boolean validDay(int n) {
        return n>0 && n<32;
    }
    public static boolean validMonth(int n) {
        return n>0 && n<13;
    }
    public static boolean validYear(int n) {
        //TODO is this right? idk
        return n>0;
    }

    private static class ConditionState {
        boolean v, certain;
        ConditionState() {
            this(false);
        }
        ConditionState(boolean state) {
            this(state, false);
        }
        ConditionState(boolean state, boolean certain) {
            v = state;
            this.certain = certain;
        }
        public ConditionState clone() {
            return new ConditionState(v, certain);
        }
        public boolean equals(Object other) {
            if(!(other instanceof ConditionState)) return false;
            return (((ConditionState) other).v == v) && (((ConditionState) other).certain == certain);
        }
    }

    public static class FoundDate {
        private LocalDate date;
        private Point[] cornerPoints;

        FoundDate(LocalDate date, Point[] cps) {
            this.date = date;
            cornerPoints = cps;
        }

        public LocalDate getDate() {
            return date;
        }

        public Point[] getCornerPoints() {
            return cornerPoints;
        }

        @NotNull
        @Override
        public String toString() {
            return date.toString();
        }
    }

}


/*
*
* FORMATS:
* 15 Apr 2019
* 121518
* 10APR04
* 2015 DE 27
*
* KEYWORDS: bbf, by, before, exp,
 *
* ALGORITHM:
* crop to only date
* split into day, month, year
* identify each type
* parse each individually
*
*
* JA
* FE
* MA
* AP
* MA
*/