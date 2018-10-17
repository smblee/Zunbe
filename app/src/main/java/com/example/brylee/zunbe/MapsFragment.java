package com.example.brylee.zunbe;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.brylee.zunbe.aco.Route;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class MapsFragment extends Fragment implements OnMapReadyCallback, MapsActivity.OptimalRouteUpdateListener {

    private GoogleMap mMap;
    private Route currOptimalRoute;
    private Button mBtnCenter;
    private Button mBtnRecalc;
    private ArrayList<Marker> markers;
    private Marker movingMarker;
    private ArrayList<Polyline> polylines;
    private TextView mTotalDistance;

    final CountDownLatch mapLoadedLatch = new CountDownLatch(1);


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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.maps_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


//        setContentView(R.l ayout.maps_fragment);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        new MapLoadingProgressTask().execute();

        mapFragment.getMapAsync(this);
        currOptimalRoute = (Route) getArguments().getSerializable("route");
        markers = new ArrayList<>();
        polylines = new ArrayList<>();

        mBtnCenter = (Button) getView().findViewById(R.id.center_button);
        mBtnCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateToAllLocations();
                Toast.makeText(getActivity(), "Map centered.", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnRecalc = (Button) getView().findViewById(R.id.recalc_button);
        mBtnRecalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear the existing markers/polylines in case the function is running again.
                clearMapGraphics();
                getOptimalRoute();
            }
        });

        mTotalDistance = (TextView) getView().findViewById(R.id.totalDistanceText);
        refreshDistanceText(currOptimalRoute.getDistance());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapLoadedLatch.countDown();
//        mapLoadingDialog.dismiss();




        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });

        drawGraphOnMap();
        //        animateLocationMovement(0);
    }

    private void animateLocationMovement(final int i) {
        if (i > currOptimalRoute.getLocations().size() - 1) {
            return;
        }
        Location loc = currOptimalRoute.getLocations().get(i);

        // Creating a marker
        final LatLng latLng = new LatLng(loc.getLatitudeDegrees(), loc.getLongitudeDegrees());

        // Animating to the touched position


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5), 2000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                // Placing a marker on the position
                animateLocationMovement(i + 1);
            }

            @Override
            public void onCancel() {

            }
        });

        // add marker to position

//        builder.include(latLng);
    }

    private void animateToAllLocations() {
        // final animation to include all regions
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (Location loc : currOptimalRoute.getLocations()) {
            builder.include(new LatLng(loc.getLatitudeDegrees(), loc.getLongitudeDegrees()));
        }

        int width = getContext().getResources().getDisplayMetrics().widthPixels;
        int height = getContext().getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.animateCamera(cu);
    }

    private void getOptimalRoute() {
        ((MapsActivity) getActivity()).getOptimalRoute();
    }

    @Override
    public void onOptimalRouteUpdated(Route route) {
        currOptimalRoute = route;
        refreshDistanceText(route.getDistance());
        drawGraphOnMap();
    }

    private void clearMapGraphics() {
        for (Polyline poly : polylines) {
            poly.remove();
        }
        for (Marker mark : markers) {
            mark.remove();
        }
        polylines.clear();
        markers.clear();
        if (movingMarker != null) movingMarker.remove();
    }

    public void drawGraphOnMap() {
        ArrayList<Location> optimalPath = currOptimalRoute.getLocations();
        for (int i = 0; i < optimalPath.size(); i++) {
            final MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(optimalPath.get(i).getLatlng());
            markerOptions.title(optimalPath.get(i).getName()); //optimalPath.get(i).getLatitudeDegrees() + " : " + optimalPath.get(i).getLongitudeDegrees());

            Marker marker = mMap.addMarker(markerOptions);
            if (i == 0) {
                marker.setIcon(getBitmapDescriptor(R.drawable.marker_pin2_start));
                marker.setSnippet("Start");
                marker.showInfoWindow();
            } else if (i == optimalPath.size() - 1) {
                marker.setIcon(getBitmapDescriptor(R.drawable.marker_pin2_end));
                marker.setSnippet("End");
                marker.showInfoWindow();
            } else {
                marker.setIcon(getBitmapDescriptor(R.drawable.marker_pin2));
                marker.setSnippet("Stop #" + String.valueOf(i + 1));
                marker.showInfoWindow();
            }
            markers.add(marker);
        }
        for (int i = 0; i < optimalPath.size() - 1; i++) {
            Location from = optimalPath.get(i);
            Location to = optimalPath.get(i + 1);
            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .add(from.getLatlng(), to.getLatlng())
                    .width(5)
                    .color(getContext().getColor(R.color.mapLine)));
            polylines.add(polyline);
        }
        animateToAllLocations();

//        movingMarker = mMap.addMarker(new MarkerOptions()
//                .position(optimalPath.get(0).getLatlng())
//                .title("You")
//                .icon(getBitmapDescriptor(R.drawable.marker_pin)));
//
//        if (optimalPath.size() > 1)
//            animateMarker(movingMarker, optimalPath.get(1).getLatlng(), false);
    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        Drawable vectorDrawable = getContext().getDrawable(id);
        int h = vectorDrawable.getIntrinsicHeight();
        int w = vectorDrawable.getIntrinsicWidth();
        vectorDrawable.setBounds(0, 0, w, h);
        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bm);
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;
        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
    private void refreshDistanceText(double distance) {
        mTotalDistance.setText("Total Distance\n" + new DecimalFormat("##.00").format(distance) + " miles");
    }
        ProgressDialog mapLoadingProgressTaskDialog;
    class MapLoadingProgressTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mapLoadingProgressTaskDialog = Tools.createLoadingDialog(getContext(), "Loading Google map");
            mapLoadingProgressTaskDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mapLoadedLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Toast.makeText(getContext(), "Failed to load Google Maps.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return null;
        }



        @Override
        protected void onPostExecute(Void aVoid) {
            mapLoadingProgressTaskDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }


}
