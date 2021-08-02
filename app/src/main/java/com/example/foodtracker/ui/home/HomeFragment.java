package com.example.foodtracker.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.foodtracker.Food;
import com.example.foodtracker.FoodAdapter;
import com.example.foodtracker.MainActivity;
import com.example.foodtracker.R;
import com.example.foodtracker.databinding.FragmentHomeBinding;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    Food[] items = new Food[0];
    RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = root.findViewById(R.id.recycler_view);

        try {
            items = MainActivity.getFoodsArray();
            Arrays.sort(items);
        } catch (Exception e) {
//            Snackbar.make(((MainActivity)getActivity()).getBinding().getRoot(), "_"+e.getMessage(), Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
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


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}