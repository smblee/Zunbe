/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.brylee.zunbe;

import com.example.brylee.zunbe.aco.Route;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends AppCompatActivity implements PlaceSelectionListener {
    public static final String TAG = "RouteActivity";
    private ListView mListLocation;


    private Route currRoute;
    private LocationAdapter adapter;

    private DatabaseHelper dbHelper;

    private PlaceAutocompleteFragment autocompleteFragment;


    private int startingLocation = -1;
    private int accuracyValue = 0;

    private int tempSeekval = 0;

    String[] locations = new String[]{"Oklahoma City, Oklahoma", "Boise, Idaho", "Wichita, Kansas", "Denver, Colorado", "Albuquerque, New Mexico", "Phoenix, Arizona", "Las Vegas, Nevada", "San Francisco, California", "Portland, Oregon", "Seattle, Washington",
            "Boise, Idaho", "Chicago, Illinois", "Park City, Utah", "Chicago, Illinois", "Providence, Rhode Island", "Indianapolis, Indiana", "Louisville, Kentucky", "Columbus, Ohio", "Detroit, Michigan", "Cleveland, Ohio", "Manchester, New Hampshire", "Portland, Maine", "Boston, Massachusetts",
            "Providence, Rhode Island", "Charlotte, North Carolina", "New Haven, Connecticut", "New York City, New York", "Ocean City, New Jersey", "Philadelphia, Pennsylvania", "Wilmington, Delaware", "Baltimore, Maryland", "Washington, D.C.", "Virginia Beach, Virginia",
            "Charlotte, North Carolina", "Little Rock, Arkansas", "Charleston, South Carolina", "Orlando, Florida"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        currRoute = (Route) getIntent().getSerializableExtra("route");

        // Retrieve the PlaceAutocompleteFragment.
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);

        // Retrieve the TextViews that will display details about the selected place.
        mListLocation = (ListView) findViewById(R.id.list_loc);

        dbHelper = new DatabaseHelper(getApplicationContext());

        adapter = new LocationAdapter(this, currRoute.getLocations());
        mListLocation.setAdapter(adapter);

//        addAllUS();


        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        EditText title = (EditText) findViewById(R.id.edittext);
        title.setText(currRoute.getTitle());
        title.setTypeface(null, Typeface.BOLD);

        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currRoute.setTitle(s.toString());
                dbHelper.updateRoute(currRoute);
            }
        });

        mListLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                resetStartingLocation();
                view.setSelected(true);
//                view.setBackgroundColor(getColor(R.color.colorAccent));
                startingLocation = position;
//                View vItem = mListLocation.getChildAt(position);
//                if (vItem == null)
//                    return;
//                TextView tv = (TextView) vItem.findViewById(R.id.starting_location_text);
//                tv.setVisibility(View.VISIBLE);
            }
        });

//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mListLocation.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        createDeleteConfirmDialog(position).show();
                        return true;
                    }
                }
        );

        // EMPTY VIEW FOR LIST

        TextView emptyView = new TextView(getApplicationContext());
        // add empty view for list view
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyView.setText(R.string.info_add_loc);
        emptyView.setTextSize(20);
        emptyView.setVisibility(View.GONE);
        emptyView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        ((ViewGroup) mListLocation.getParent()).addView(emptyView);
        mListLocation.setEmptyView(emptyView);
    }

    private void resetStartingLocation() {
        if (startingLocation >= 0) {
            View vItem = mListLocation.getChildAt(startingLocation);
            if (vItem == null)
                return;
            vItem.setBackgroundColor(getColor(R.color.white));
            vItem.setSelected(false);
            TextView tv = (TextView) vItem.findViewById(R.id.starting_location_text);
            tv.setVisibility(View.INVISIBLE);
            startingLocation = -1;

        }
    }

    private AlertDialog createDeleteConfirmDialog(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this location: " + currRoute.getLocations().get(pos) + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        if (pos == startingLocation)
//                            resetStartingLocation();
                        dbHelper.deleteLocation(currRoute.getLocations().get(pos).getId());
                        adapter.remove(currRoute.getLocations().get(pos));
                        startingLocation = -1;
                        mListLocation.setSelection(-1);

                        mListLocation.setSelected(false);
                        Toast.makeText(getApplicationContext(), "Location deleted! Selected" + mListLocation.getSelectedItemPosition(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // CANCEL
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private AlertDialog createAccuracySeekbar() {
        tempSeekval = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout linear = new LinearLayout(this);

        linear.setOrientation(LinearLayout.VERTICAL);
        final TextView text = new TextView(this);
        text.setText(String.valueOf(accuracyValue));
        text.setPadding(10, 10, 10, 10);

        SeekBar seek = new SeekBar(this);
        seek.setMax(10);
        seek.setProgress(accuracyValue);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tempSeekval = progress;
                text.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        linear.addView(seek);
        linear.addView(text);


        builder.setTitle("Set your route search speed/accuracy.")
                .setMessage("Lower value = faster, less accurate")
                .setView(linear)
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        accuracyValue = tempSeekval;
//                        System.out.println(accuracyValue);
                        // CANCEL
                        Toast.makeText(getApplicationContext(), "Accuracy set!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());
        Location newLoc = new Location(place.getName().toString(), place.getLatLng(), place.getAddress().toString());
        dbHelper.createLocation(currRoute.getId(), newLoc);
        adapter.add(newLoc);
        autocompleteFragment.setText("");
    }


    /**
     * Debug function used to populate data
     */
    public void addAllUS() {
        for (String s : locations) {
            LatLng ll = getLocationFromAddress(s);
            Location newLoc = new Location(s, ll, s);

            dbHelper.createLocation(currRoute.getId(), newLoc);
            adapter.add(newLoc);
        }
    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        Barcode.GeoPoint p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 1);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            return new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_forward) {
            if (currRoute.getLocations().size() > 0) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("locations", currRoute.getLocations());
                intent.putExtra("startingLocation", startingLocation);
                intent.putExtra("accuracy", accuracyValue);
                startActivity(intent);
                //TODO: add intent to map
                return true;
            }
            Toast.makeText(this.getApplicationContext(), "Please add some locations!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (id == R.id.action_settings) {
            createAccuracySeekbar().show();
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }

}
