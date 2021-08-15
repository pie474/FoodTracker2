package com.example.foodtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodtracker.ui.DetectFoodAttribute;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

public class AddActivity extends AppCompatActivity {

    private TextView dateTextView, nameTextView;

    ActivityResultLauncher<Integer> mGetContent = registerForActivityResult(new DetectFoodAttribute(),
            new ActivityResultCallback<Bundle>() {
                @Override
                public void onActivityResult(Bundle result) {
                    if(result == null) return;
                    nameTextView.setText(result.getString(Food.NAME_TAG));
                    dateTextView.setText(result.getString(Food.EXP_DATE_TAG));
                    Log.d("debug", "date: "+result);
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
                /*MainActivity.addFood(new Food(
                        new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(dateTextView.getText().toString()),
                        nameTextView.getText().toString()));*/

                Intent returnIntent = new Intent();
                Bundle b = new Bundle();
                b.putString(Food.EXP_DATE_TAG, dateTextView.getText().toString());
                b.putString(Food.NAME_TAG, nameTextView.getText().toString());
                returnIntent.putExtras(b);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dateTextView = findViewById(R.id.editTextExpDate);
        nameTextView = findViewById(R.id.editTextFoodName);
    }

    public void startCameraActivity(View view) {

        mGetContent.launch(0);
    }
}