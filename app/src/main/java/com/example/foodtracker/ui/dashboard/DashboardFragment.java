package com.example.foodtracker.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.foodtracker.MainActivity;
import com.example.foodtracker.R;
import com.example.foodtracker.databinding.FragmentDashboardBinding;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final ImageButton cameraButton = binding.floatingCameraButton;
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).openCamera();
            }
        });

        final ImageButton textButton = binding.textInput;
        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "klik", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        inflater.inflate(R.layout.fragment_dashboard, container, false);


        /*final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/




        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}