package com.example.brylee.zunbe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.brylee.zunbe.aco.Route;

import java.util.ArrayList;

/**
 * Created by brylee on 5/1/17.
 */

public class RouteAdapter extends ArrayAdapter<Route> {
    public RouteAdapter(Context context, ArrayList<Route> routes) {
        super(context, 0, routes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Route route = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_route, parent, false);
        }
        // Lookup view for data population
        TextView tvIndex = (TextView) convertView.findViewById(R.id.route_item_index);
        TextView tvTitle = (TextView) convertView.findViewById(R.id.route_item_title);
        TextView tvDescription = (TextView) convertView.findViewById(R.id.route_item_description);

        // Populate the data into the template view using the data object
        tvIndex.setText(String.valueOf(position + 1));
        tvTitle.setText(route.getTitle());
        tvDescription.setText(makeRouteDescription(route.getLocations()));

        // Return the completed view to render on screen
        return convertView;
    }

    private String makeRouteDescription(ArrayList<Location> route) {
        return android.text.TextUtils.join(", ", route);
    }
}
