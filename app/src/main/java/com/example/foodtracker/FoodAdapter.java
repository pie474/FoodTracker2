
package com.example.foodtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import androidx.recyclerview.widget.RecyclerView;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private String[] localDataSet;
    private Food[] dataSet2;
    Context context;


/**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView typeView;
        private TextView dateView;
        private int pos;

        public void setPosition(int p) {
            pos = p;
        }

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            typeView = (TextView) itemView.findViewById(R.id.itemName);
            dateView = (TextView) itemView.findViewById(R.id.itemDate);

            final ImageButton cameraButton = itemView.findViewById(R.id.imageButton);
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        JSONArray arr = MainActivity.getFoods();
                        arr.remove(pos);
                        MainActivity.writeFoods(arr);
                        //view.refreshDrawableState();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public TextView getTypeView() {
            return typeView;
        }

        public TextView getDateView() {
            return dateView;
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
        viewHolder.setPosition(position);
        viewHolder.getTypeView().setText(dataSet2[position].getType());
        viewHolder.getDateView().setText(dataSet2[position].getFormattedDate());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        // return localDataSet.length
        return dataSet2.length;
    }
}


