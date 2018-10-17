package com.example.brylee.zunbe;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;

/**
 * A fragment containing the YelpListActivity.
 * Responsible for displaying the restaurants the user has saved.
 */
public class YelpListFragment extends Fragment {

    LinearLayout rootLayout;
    RecyclerView listRecyclerView;
    TextView emptyListView;
    CircularProgressBar progressBar;

    ListRestaurantCardAdapter listRestaurantCardAdapter;

//    YelpListHolder savedListHolder;


    AsyncTask initialYelpQuery;
    ArrayList<Restaurant> restaurants = new ArrayList<>();
    int errorInQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        // http://developer.android.com/training/basics/activity-lifecycle/recreating.html
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);

//        if (savedInstanceState != null) {
//            savedListHolder.setSavedList(savedInstanceState.<Restaurant>getParcelableArrayList("savedList"));
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);

//        savedListHolder = YelpListHolder.getInstance();

        rootLayout = (LinearLayout) inflater.inflate(R.layout.fragment_yelp_list, container, false);
        listRecyclerView = (RecyclerView) rootLayout.findViewById(R.id.listRecyclerView);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        emptyListView = (TextView) rootLayout.findViewById(R.id.emptyText);
        progressBar = (CircularProgressBar) rootLayout.findViewById(R.id.circularProgressBarSavedList);


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

                if (direction == 4) {
                    restaurants.remove(viewHolder.getAdapterPosition());
                    listRestaurantCardAdapter.notifyDataSetChanged();
                }

                // If user has swiped right, open Yelp to current restaurant's page.
                if (direction == 8) {

                    // Open restaurant in Yelp.
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(restaurants.get(viewHolder.getAdapterPosition()).getUrl())));

                    // We don't want to remove the list item if user wants to see it in Yelp.
                    // Tell the adapter to refresh so the item is can be visible again.
                    listRestaurantCardAdapter.notifyDataSetChanged();
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(listRecyclerView);

        return rootLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        double latitude = getArguments().getDouble("latitude");
//        double longitude = getArguments().getDouble("longitude");

        // If the list is null or empty, we want to avoid any exceptions thrown from the
        // RecyclerView Adapter. So, set TextView to inform the user no items have been added to the savedList.
//        if (savedListHolder.getSavedList() == null || savedListHolder.getSavedList().isEmpty()) {
            listRecyclerView.setVisibility(View.GONE);
            initialYelpQuery = new GetAllRestaurantsFromAPICallTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((YelpListActivity) getActivity()).getLatlng());
            return;
//        }

        // Else, make the list visible.
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // http://developer.android.com/training/basics/activity-lifecycle/recreating.html
        super.onSaveInstanceState(outState);

//        outState.putParcelableArrayList("savedList", savedListHolder.getSavedList());
    }

    private void showRestaurants() {
        emptyListView.setVisibility(View.GONE);
        listRecyclerView.setVisibility(View.VISIBLE);
        listRestaurantCardAdapter = new ListRestaurantCardAdapter(getContext(), restaurants);
        listRecyclerView.setAdapter(listRestaurantCardAdapter);
    }

    private ArrayList<Restaurant> queryYelp(String lat, String lon) {

        ArrayList<Restaurant> lst = new ArrayList<>();

        // Build Yelp request.
        try {
            URL url;
            HttpURLConnection urlConnection;
            String requestUrl = "https://api.yelp.com/v3/businesses/search";
            StringBuilder builder = new StringBuilder(requestUrl);

            builder.append("?term=").append("food");
            builder.append("&limit=" + 20);
            builder.append("&latitude=").append(lat);
            builder.append("&longitude=").append(lon);

            requestUrl = builder.toString();
            Log.d("YelpJSON", "request made: " + requestUrl);

            url = new URL(requestUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Authorization", String.format("Bearer %s", getString(R.string.yelp_token)));
            urlConnection.setConnectTimeout(10 * 1000);
            urlConnection.setReadTimeout(10 * 1000);

            // Make connection and read the response.
            urlConnection.connect();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String buf, jsonString;
            while ((buf = br.readLine()) != null)
                sb.append(buf);
            br.close();
            jsonString = sb.toString();

            JSONObject response = new JSONObject(jsonString);

            // Get JSON array that holds the listings from Yelp.
            JSONArray jsonBusinessesArray = response.getJSONArray("businesses");
            int length = jsonBusinessesArray.length();
            Log.d("YelpJSON", "json response array length: " + length);

            // This occurs if a network communication error occurs or if no restaurants were found.
            if (length <= 0) {
                errorInQuery = TypeOfError.NO_RESTAURANTS;
                return null;
            }

            for (int i = 0; i < length; i++) {
                if (initialYelpQuery.isCancelled())
                    break;

                Restaurant res = convertJSONToRestaurant(jsonBusinessesArray.getJSONObject(i));
                if (res != null)
                    lst.add(res);
            }

            if (lst.isEmpty()) {
                errorInQuery = TypeOfError.NO_RESTAURANTS;
                return null;
            }

        } catch (JSONException e) {
            if (e.getMessage().contains("No value for businesses"))
                errorInQuery = TypeOfError.NO_RESTAURANTS;
            else if (e.getMessage().contains("No value for"))
                errorInQuery = TypeOfError.MISSING_INFO;
            e.printStackTrace();
            return null;
        } catch (FileNotFoundException e) {
            errorInQuery = TypeOfError.INVALID_LOCATION;
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            if (e.getMessage().contains("timed out")) errorInQuery = TypeOfError.TIMED_OUT;
            e.printStackTrace();
            return null;
        }

        errorInQuery = TypeOfError.NO_ERROR;
        return lst;
    }

    /**
     * Convert JSON to a Restaurant object that encapsulates a restaurant from Yelp.
     *
     * @param obj: JSONObject that holds all restaurant info.
     * @return Restaurant or null if an error occurs.
     */
    private Restaurant convertJSONToRestaurant(JSONObject obj) {
        try {
            // Getting the JSON array of categories
            JSONArray categoriesJSON = obj.getJSONArray("categories");
            ArrayList<String> categories = new ArrayList<>();

            for (int i = 0; i < categoriesJSON.length(); i++)
                categories.add(categoriesJSON.getJSONObject(i).getString("title"));

            // Getting the restaurant's coordinates and price
            double lat = obj.getJSONObject("coordinates").getDouble("latitude");
            double lon = obj.getJSONObject("coordinates").getDouble("longitude");
            double distance = obj.getDouble("distance") * 0.000621371; // Convert to miles

            // Getting restaurant's address
            JSONObject locationJSON = obj.getJSONObject("location");
            JSONArray addressJSON = locationJSON.getJSONArray("display_address");
            ArrayList<String> address = new ArrayList<>();

            for (int i = 0; i < addressJSON.length(); i++)
                address.add(addressJSON.getString(i));

            // Get deals if JSON contains deals object.
            String deals;
            try {
                JSONArray dealsArray = obj.getJSONArray("deals");
                ArrayList<String> dealsList = new ArrayList<>();

                for (int i = 0; i < dealsArray.length(); i++) {
                    JSONObject jsonObject = dealsArray.getJSONObject(i);
                    dealsList.add(jsonObject.getString("title"));
                }
                deals = dealsList.toString().replace("[", "").replace("]", "").trim();
            } catch (Exception ignored) {
                deals = "";
            }

            // If restaurant doesn't have a price, put a question mark.
            String price;
            try {
                price = obj.getString("price");
            } catch (Exception ignored) {
                price = "?";
            }

            // If listing does not have an image, make sure it routes to localhost because
            // Picasso will complain when a URL string is empty.
            String imageUrl = obj.getString("image_url");
            if (imageUrl.length() == 0)
                imageUrl = "localhost";

            // Construct a new Restaurant object with all the info we gathered above and return it
            return new Restaurant(obj.getString("name"), (float) obj.getDouble("rating"),
                    imageUrl, obj.getInt("review_count"), obj.getString("url"),
                    categories, address, deals, price, distance, lat, lon);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("YelpJSON", "error in convertJSONToRestaurant: " + e.getMessage());
            return null;
        }
    }

    /**
     * Background task to query database for the restaurants saved by the user.
     */
    private class GetAllRestaurantsFromAPICallTask extends AsyncTask<LatLng, Void, ArrayList<Restaurant>> {

        @Override
        protected void onPreExecute() {
            emptyListView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            ((CircularProgressDrawable) progressBar.getIndeterminateDrawable()).start();
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Restaurant> doInBackground(LatLng... params) {
            return queryYelp(String.valueOf(params[0].latitude), String.valueOf(params[0].longitude));
        }

        @Override
        protected void onPostExecute(ArrayList<Restaurant> restaurants) {
            progressBar.progressiveStop();
            progressBar.setVisibility(View.GONE);
            System.out.println(restaurants.size());
            YelpListFragment.this.restaurants = restaurants;
//            savedListHolder.setSavedList(restaurants);
            if (YelpListFragment.this.restaurants.isEmpty())
                emptyListView.setVisibility(View.VISIBLE);
            else
                showRestaurants();

            super.onPostExecute(restaurants);
        }
    }
}
