package com.example.foodtracker.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.foodtracker.FoodAdapter;
import com.example.foodtracker.R;
import com.example.foodtracker.databinding.FragmentDashboardBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.foodtracker.Food;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.GregorianCalendar;


public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;
    Food[] items;
    RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        View view= inflater.inflate(R.layout.fragment_dashboard, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        items = new Food[]{
                new Food(new Date(), "Bread"),
                new Food(new Date(), "Milk"),
                new Food(new Date(), "Cheese")
        };
        FoodAdapter foodAdapter = new FoodAdapter(items, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(foodAdapter);

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        final FloatingActionButton cameraButton = binding.floatingCameraButton;
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                openCamera();
            }
        });


        return view;
    }

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}