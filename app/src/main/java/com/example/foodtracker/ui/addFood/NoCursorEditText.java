package com.example.foodtracker.ui.addFood;

import android.content.Context;
import android.util.AttributeSet;

public class NoCursorEditText extends androidx.appcompat.widget.AppCompatEditText {

    public NoCursorEditText(Context context) {
        super(context);
    }

    public NoCursorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoCursorEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onSelectionChanged(int start, int end) {
        CharSequence text = getText();
        if (text != null) {
            if (start != text.length() || end != text.length()) {
                setSelection(text.length(), text.length());
                return;
            }
        }

        super.onSelectionChanged(start, end);
    }

}
