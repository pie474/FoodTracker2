package com.example.foodtracker.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DetectFoodAttribute extends ActivityResultContract<Integer, Bundle> {
    public static final String ACTION_GET_DATE = "com.example.foodtracker.GET_DATE";

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, @NonNull Integer attributeType) {
        Intent intent = new Intent(ACTION_GET_DATE);
        return intent;
    }

    @Override
    public Bundle parseResult(int resultCode, @Nullable Intent result) {
        if (((resultCode != Activity.RESULT_OK) && (resultCode != Activity.RESULT_CANCELED)) || result == null) {
            Log.d("debug", "error: " + resultCode + (result==null?" null":""));
            return null;
        }
        return result.getExtras();
    }
}