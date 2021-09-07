package com.example.foodtracker.ui.addFood;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.foodtracker.Food;
import com.example.foodtracker.R;
import com.example.foodtracker.ui.camera.DetectFoodAttribute;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends AppCompatActivity {

    private TextView dayTextView, monthTextView, yearTextView, nameTextView;

    ActivityResultLauncher<Integer> mGetContent = registerForActivityResult(new DetectFoodAttribute(),
            new ActivityResultCallback<Bundle>() {
                @Override
                public void onActivityResult(Bundle result) {
                    if(result == null) return;
                    if(result.getString(Food.NAME_TAG) != null) nameTextView.setText(result.getString(Food.NAME_TAG));
                    if(result.getString(Food.EXP_DAY_TAG) != null) dayTextView.setText(result.getString(Food.EXP_DAY_TAG));
                    if(result.getString(Food.EXP_MONTH_TAG) != null) monthTextView.setText(result.getString(Food.EXP_MONTH_TAG));
                    if(result.getString(Food.EXP_YEAR_TAG) != null) yearTextView.setText(result.getString(Food.EXP_YEAR_TAG));
                    //Log.d("debug", "date: "+result);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        final ImageButton detectDateButton = findViewById(R.id.buttonLaunchCamera);
        detectDateButton.setOnClickListener(this::startCameraActivity);

        final Button submitButton = findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(view -> {
            try {
                if(nameTextView.getText().toString().length()==0) {
                    throw new IllegalArgumentException("Enter a name!");
                }
                Intent returnIntent = new Intent();
                Bundle b = new Bundle();
                b.putString(Food.EXP_DATE_TAG, getDate().toString());
                b.putString(Food.NAME_TAG, nameTextView.getText().toString());
                returnIntent.putExtras(b);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.addLayout), ""+e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });

        nameTextView = findViewById(R.id.editTextFoodName);

        monthTextView = findViewById(R.id.editTextExpMonth);
        dayTextView = findViewById(R.id.editTextExpDay);
        yearTextView = findViewById(R.id.editTextExpYear);
        /*monthTextView.setCursorVisible(false);
        dayTextView.setCursorVisible(false);
        yearTextView.setCursorVisible(false);*/

        monthTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int curr = Integer.parseInt(s.toString());
                    if (curr * 10 > 12) {
                        if (curr > 12) {
                            s.delete(s.length() - 1, s.length());
                        } else {
                            dayTextView.requestFocus();
                        }
                    } else if(s.length()==2) {
                        dayTextView.requestFocus();
                    }
                } catch (NumberFormatException ignored) {}
            }
        });
        monthTextView.setOnEditorActionListener((v, actionId, event) -> {
            dayTextView.requestFocus();
            return false;
        });

        dayTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int curr = Integer.parseInt(s.toString());
                    if (curr * 10 > 31) {
                        if (curr > 31) {
                            s.delete(s.length() - 1, s.length());
                        } else {
                            yearTextView.requestFocus();
                        }
                    } else if(s.length()==2) {
                        dayTextView.requestFocus();
                    }
                } catch (NumberFormatException ignored) {}

            }
        });
        dayTextView.setOnEditorActionListener((v, actionId, event) -> {
            yearTextView.requestFocus();
            return false;
        });

        yearTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 4) {
                    s.delete(4, s.length());
                }
            }
        });
    }

    private void setDate(LocalDate d) {
        setDate(d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    private void setDate(int day, int month, int year) {
        dayTextView.setText(day);
        monthTextView.setText(month);
        yearTextView.setText(year);
    }

    private LocalDate getDate() {
        int year = Integer.parseInt(yearTextView.getText().toString());
        if(year<500) {
            year += 2000;
        } else if(year<1000) {
            year += 1000;
        }

        return LocalDate.of(year,
                Integer.parseInt(monthTextView.getText().toString()),
                Integer.parseInt(dayTextView.getText().toString()));
    }

    public void startCameraActivity(View view) {

        mGetContent.launch(0);
    }
}