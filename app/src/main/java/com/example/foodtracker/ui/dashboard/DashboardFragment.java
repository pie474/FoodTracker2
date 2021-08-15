package com.example.foodtracker.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodtracker.Food;
import com.example.foodtracker.R;
import com.example.foodtracker.databinding.FragmentDashboardBinding;
import com.example.foodtracker.ui.DetectFoodAttribute;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
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


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final ImageButton detectDateButton = binding.buttonLaunchCamera;
        detectDateButton.setOnClickListener(this::startCameraActivity);

        final Button submitButton = binding.buttonSubmit;
        submitButton.setOnClickListener(view -> {
            try {
               /* MainActivity.addFood(new Food(
                        new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(dateTextView.getText().toString()),
                        nameTextView.getText().toString()));*/

                //((MainActivity)getActivity()).switchToList();
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dateTextView = binding.editTextExpDate;
        nameTextView = binding.editTextFoodName;

        inflater.inflate(R.layout.fragment_dashboard, container, false);



        return root;
    }

    public void startCameraActivity(View view) {
        /*Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivity(intent);*/
        mGetContent.launch(0);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}