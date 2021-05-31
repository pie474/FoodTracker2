package com.example.foodtracker.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.foodtracker.Food;
import com.example.foodtracker.databinding.FragmentNotificationsBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    Food[] items;
    RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNotifications;
        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        /*try {
            JSONArray arr = ((MainActivity)getActivity()).getFoods();
            Snackbar.make(((MainActivity)getActivity()).getBinding().getRoot(), "" + Food.fromJson((JSONObject)arr.get(arr.length()-1)).getType(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            Snackbar.make(((MainActivity)getActivity()).getBinding().getRoot(), "_"+e.getMessage(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }*/

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}