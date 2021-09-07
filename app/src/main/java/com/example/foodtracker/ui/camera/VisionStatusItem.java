package com.example.foodtracker.ui.camera;

import com.example.foodtracker.R;

import androidx.annotation.DrawableRes;

public class VisionStatusItem {
    private final @DrawableRes int[] assets = new int[] {
            R.drawable.ic_empty_circle_outline_24,
            R.drawable.ic_static_loading_24dp,
            R.drawable.ic_check_circle_outline_24
    };

    private final String name;
    private String result;
    private int state = 0;

    public VisionStatusItem(String name) {
        this.name = name;
    }

    public String getName() {
        return (state == 2 ? result : name);
    }

    public @DrawableRes int getIcon() {
        return assets[state];
    }

    public void setLoading() {
        state = 1;
    }

    public void setComplete(String result) {
        state = 2;
        this.result = result;
    }

    public void reset() {
        state = 0;
    }
}
