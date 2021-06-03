package com.example.foodtracker.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodtracker.Food;
import com.example.foodtracker.FoodAdapter;
import com.example.foodtracker.MainActivity;
import com.example.foodtracker.R;
import com.example.foodtracker.databinding.FragmentDashboardBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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

        final FloatingActionButton cameraButton = binding.floatingCameraButton;
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).openCamera();
            }
        });

        inflater.inflate(R.layout.fragment_dashboard, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);

        try {
            items = MainActivity.getFoodsArray();
            Arrays.sort(items);
        } catch (Exception e) {
            Snackbar.make(((MainActivity)getActivity()).getBinding().getRoot(), "_"+e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        /*items = new Food[]{
                new Food(new Date(), "Bread"),
                new Food(new Date(), "Milk"),
                new Food(new Date(), "Cheese"),
                new Food(new Date(), "Rice"),
                new Food(new Date(), "Lettuce"),
                new Food(new Date(), "Tomatoes"),
                new Food(new Date(), "Spinach"),
                new Food(new Date(), "Olives"),
                new Food(new Date(), "Pasta"),
                new Food(new Date(), "Noodles"),
                new Food(new Date(), "Ketchup"),
                new Food(new Date(), "Soy Sauce"),
                new Food(new Date(), "Spoons"),
                new Food(new Date(), "Forks"),
                new Food(new Date(), "Mango"),
                new Food(new Date(), "Apple")

        };*/

        FoodAdapter foodAdapter = new FoodAdapter(items, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(foodAdapter);

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