package com.example.brylee.zunbe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.brylee.zunbe.aco.Route;
import com.yelp.fusion.client.connection.YelpFusionApi;
import com.yelp.fusion.client.connection.YelpFusionApiFactory;
import com.yelp.fusion.client.models.Business;
import com.yelp.fusion.client.models.SearchResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by brylee on 5/2/17.
 */

public class MapsListFragment extends Fragment implements MapsActivity.OptimalRouteUpdateListener {

    private Route currOptimalRoute;
    private ListView lv;
    private LocationAdapter adapter;
    YelpFusionApiFactory apiFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.maps_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currOptimalRoute = (Route) getArguments().getSerializable("route");
        lv = (ListView) getView().findViewById(R.id.list_optimal_loc);
        adapter = new LocationAdapter(this.getContext(), currOptimalRoute.getLocations());
        lv.setAdapter(adapter);

        apiFactory = new YelpFusionApiFactory();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (apiFactory != null) {
                    AlertDialog ad = createNearbySearchDialog(position);
                    ad.show();
                }
            }
        });
    }

    @Override
    public void onOptimalRouteUpdated(Route route) {
        adapter.clear();
        adapter.addAll(route.getLocations());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MapsActivity) getActivity()).registerOptimalRouteUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MapsActivity) getActivity()).unregisterOptimalRouteUpdateListener(this);
    }

    class RetrieveYelpTask extends AsyncTask<Map<String, String>, Void, SearchResponse> {

        @Override
        protected SearchResponse doInBackground(Map<String, String>... p) {
            YelpFusionApi yelpFusionApi;
            try {
                yelpFusionApi = apiFactory.createAPI(getString(R.string.yelp_client_id), getString(R.string.yelp_client_secret));
                Map<String, String> params = p[0];
                Call<SearchResponse> call = yelpFusionApi.getBusinessSearch(params);
                Response<SearchResponse> r = call.execute();
                System.out.println(r.message());
                return r.body();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(SearchResponse searchResponseResponse) {
            super.onPostExecute(searchResponseResponse);
            for (Business b : searchResponseResponse.getBusinesses()) {

            }
            System.out.println(searchResponseResponse.getBusinesses().size());
        }
    }

    private ProgressDialog generateLoadingDialog() {
        ProgressDialog loading = new ProgressDialog(getContext());
        //Create a new progress dialog
        loading.setTitle("Gathering Yelp Info for this location");
        loading.setMessage("Please wait...");
        loading.setIndeterminate(true);
        loading.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        loading.setMax(100);
        loading.setProgressPercentFormat(null);
        loading.setProgressNumberFormat(null);
        return loading;
    }


    private AlertDialog createNearbySearchDialog(final int position) {
        final CharSequence[] items = {"Restaurant [uses Yelp API]","Bar", "Hotel", "ATMs", "Grocery stores", "Pharmacies"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("What would you like to search for this location?");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    Intent intent = new Intent(getActivity(), YelpListActivity.class);
                    intent.putExtra("latitude", adapter.getItem(position).getLatitudeDegrees());
                    intent.putExtra("longitude", adapter.getItem(position).getLongitudeDegrees());
                    getActivity().startActivity(intent);
                    return;
                }
                Uri gmmIntentUri = Uri.parse(String.format("geo:%s,%s?q=" + items[item],adapter.getItem(position).getLatitudeDegrees(), adapter.getItem(position).getLongitudeDegrees()));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        AlertDialog alert = builder.create();
        return alert;
    }
}
