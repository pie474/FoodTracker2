package com.example.foodtracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InputFood extends ActivityResultContract<Integer, Food> {
    public static final String ACTION_INPUT_FOOD = "com.example.foodtracker.INPUT_FOOD";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull Integer attributeType) {
        Intent intent = new Intent(ACTION_INPUT_FOOD);
        return intent;
    }

    @Override
    public Food parseResult(int resultCode, @Nullable Intent result) {
        if (((resultCode != Activity.RESULT_OK) && (resultCode != Activity.RESULT_CANCELED)) || result == null) {
            Log.d("debug", "error: " + resultCode + (result==null?" null":""));
            return null;
        }
        try {
            return new Food(
                    new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(result.getExtras().getString(Food.EXP_DATE_TAG)),
                    result.getExtras().getString(Food.NAME_TAG));
        } catch (ParseException e) {
            Log.d("debug", e.getMessage());
            return null;
        }
    }
}