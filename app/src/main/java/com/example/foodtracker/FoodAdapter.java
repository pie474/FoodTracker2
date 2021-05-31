
package com.example.foodtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private String[] localDataSet;
    private Food[] dataSet2;
    Context context;


/**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (CheckBox) itemView.findViewById(R.id.checkBox);
        }

        public TextView getTextView() {
            return textView;
        }
    }


/**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */

    public FoodAdapter(String[] dataSet, Context context) {
        localDataSet = dataSet;
        this.context = context;
    }

    public FoodAdapter(Food[] dataSet, Context context) {
        dataSet2 = dataSet;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);

        return new ViewHolder(view);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, int position) {
        /*
        ViewHolder viewHolder1 = (ViewHolder)viewHolder;
        viewHolder1.textView.setText(localDataSet.toString());
        viewHolder1.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Item Selected", Toast.LENGTH_LONG).show();
            }
        });
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        */

        // viewHolder.getTextView().setText(localDataSet[position]);
        viewHolder.getTextView().setText(dataSet2[position].getType());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        // return localDataSet.length
        return dataSet2.length;
    }
}


