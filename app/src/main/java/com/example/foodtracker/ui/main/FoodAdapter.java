
package com.example.foodtracker.ui.main;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.foodtracker.Food;
import com.example.foodtracker.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {
    private ArrayList<Food> dataSet;
    private MainActivity mainActivity;
    Context context;

    private static final @ColorInt int COLOR_OK = Color.argb(255, 0, 222, 0);
    private static final @ColorInt int COLOR_SOON = Color.argb(255, 255, 175, 0);
    private static final @ColorInt int COLOR_EXPIRED = Color.argb(255, 255, 0, 0);


/**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView typeView;
        private TextView dateView;
        private View colorView;
        private int pos;

        public void setPosition(int p) {
            pos = p;
        }

        public ViewHolder(View view, MainActivity mainActivity) {
            super(view);
            // Define click listener for the ViewHolder's View

            typeView = (TextView) itemView.findViewById(R.id.itemName);
            dateView = (TextView) itemView.findViewById(R.id.itemDate);
            colorView = (View) itemView.findViewById(R.id.colorView);

            final ImageButton cameraButton = itemView.findViewById(R.id.imageButton);
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainActivity.removeFood(getAdapterPosition());
                }
            });
        }

        public TextView getTypeView() {
            return typeView;
        }

        public TextView getDateView() {
            return dateView;
        }

        public View getColorView() {
            return colorView;
        }
    }


/**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */


    public FoodAdapter(ArrayList<Food> dataSet, Context context, MainActivity mainActivity) {
        this.dataSet = dataSet;
        this.context = context;
        this.mainActivity = mainActivity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.food_list_item, viewGroup, false);

        return new ViewHolder(view, mainActivity);
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
        viewHolder.getTypeView().setText(dataSet.get(position).getType());
        viewHolder.getDateView().setText(dataSet.get(position).getFormattedDate());

        switch(dataSet.get(position).getExpirationState()) {
            case Food.EXP_STATE_OK:
                viewHolder.getColorView().setBackgroundColor(COLOR_OK);
                break;
            case Food.EXP_STATE_ALMOST:
                viewHolder.getColorView().setBackgroundColor(COLOR_SOON);
                break;
            case Food.EXP_STATE_EXPIRED:
                viewHolder.getColorView().setBackgroundColor(COLOR_EXPIRED);
                break;
        }

        /*viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });*/


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}


