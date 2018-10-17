package com.example.brylee.zunbe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by brylee on 5/1/17.
 */

public class OptimalLocationAdapter extends ArrayAdapter<Location> {
    public OptimalLocationAdapter(Context context, ArrayList<Location> locations) {
        super(context, 0, locations);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Location location = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_location, parent, false);
        }
        // Lookup view for data population
        TextView tvIndex = (TextView) convertView.findViewById(R.id.item_index);
        TextView tvName = (TextView) convertView.findViewById(R.id.item_name);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.item_address);

        // Populate the data into the template view using the data object
        if (position == 0)
            tvIndex.setText("start");
        else
            tvIndex.setText(String.valueOf(position + 1));

        tvName.setText(location.getName());
        tvAddress.setText(location.getAddress());

        // Return the completed view to render on screen
        return convertView;
    }
}
