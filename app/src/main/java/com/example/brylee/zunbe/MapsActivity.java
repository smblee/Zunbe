package com.example.brylee.zunbe;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.brylee.zunbe.aco.Ant;
import com.example.brylee.zunbe.aco.AntColonyOptimization;
import com.example.brylee.zunbe.aco.Driver;
import com.example.brylee.zunbe.aco.Route;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends AppCompatActivity implements MapLoadingDismissReadyListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    public ProgressDialog loading;
    private int[] tabIcons = {
            R.drawable.map,
            R.drawable.list
    };

    private DatabaseHelper dbHelper;

    private ArrayList<Location> locations = new ArrayList<>();

    private List<OptimalRouteUpdateListener> listeners = new ArrayList<>();

    private int startingLocation = -1;
    private int accuracyValue = 0;




    public interface LoadingDismissListener {
        void onLoadDismiss();
    }

    public interface OptimalRouteUpdateListener {
        void onOptimalRouteUpdated(Route route);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        startingLocation = getIntent().getIntExtra("startingLocation", -1);
        accuracyValue = getIntent().getIntExtra("accuracy", 0);
//        System.out.println(accuracyValue);
        locations = (ArrayList<Location>) getIntent().getSerializableExtra("locations");

        dbHelper = new DatabaseHelper(this);

        toolbar = (Toolbar) findViewById(R.id.my_maps_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");


        getOptimalRoute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(Route route) {

        viewPager = (ViewPager) findViewById(R.id.pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        bundle.putSerializable("route", route);
        MapsFragment maps = new MapsFragment();
        maps.setArguments(bundle);
        adapter.addFragment(maps, "Map");

        MapsListFragment list = new MapsListFragment();
        list.setArguments(bundle);
        adapter.addFragment(list, "List");
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();

    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    public synchronized void registerOptimalRouteUpdateListener(OptimalRouteUpdateListener listener) {
        listeners.add(listener);
    }

    public synchronized void unregisterOptimalRouteUpdateListener(OptimalRouteUpdateListener listener) {
        listeners.remove(listener);
    }

    public synchronized void optimalRouteUpdated(Route route) {
        for (OptimalRouteUpdateListener listener : listeners) {
            listener.onOptimalRouteUpdated(route);
        }
    }

    public void updateLocations(ArrayList<Location> locs) {
        this.locations = locs;
        getOptimalRoute();
    }

    public synchronized void getOptimalRoute() {
        if (locations.size() > 0)
            new GetShortestRouteTask().execute();
    }


    @Override
    public void onLoadDismiss() {
        if (this.loading != null) {
            this.loading.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

//    ProgressDialog setupViewPagerLoading;
//    class SetupViewPagerTask extends AsyncTask<Route, Void, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            setupViewPagerLoading = Tools.createLoadingDialog(MapsActivity.this, "Loading Google map");
//            setupViewPagerLoading.show();
//        }
//
//        @Override
//        protected Void doInBackground(Route... params) {
//
//            setupViewPager(params[0]);
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//        }
//    }
    ProgressDialog shortestRouteTaskLoading;


    class GetShortestRouteTask extends AsyncTask<Void, Integer, Route> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            shortestRouteTaskLoading = Tools.createLoadingDialog(MapsActivity.this, "Calculating the optimal path");
            shortestRouteTaskLoading.show();
        }



        protected Route doInBackground(Void... locs) {

            if (Driver.executorService.isShutdown()) Driver.resetExecutorService();

//            ArrayList<Location> loc = locs[0];
            Driver driver = new Driver(accuracyValue);

            AntColonyOptimization aco = new AntColonyOptimization(locations);

            for (int i = 1; i < driver.getNumOfAnts(); i++) {
                Driver.executorCompletionService.submit(new Ant(aco, i, locations, startingLocation));
                driver.activeAnts++;
                if (Math.random() > Driver.PROCESSING_CYCLE_PROBABILITY) {
                    driver.processAnts();
                }
            }
            driver.processAnts();
            Driver.executorService.shutdownNow();
            return driver.shortestRoute;
        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        protected void onPostExecute(Route result) {
//            showDialog("Downloaded " + result + " bytes");
//            System.out.println("OptimalRoute: " + Arrays.toString(result.getLocations().toArray()));
//            mMap.stopAnimation();

            shortestRouteTaskLoading.dismiss();
            if (viewPager == null) {
//                new SetupViewPagerTask().execute(result);
                setupViewPager(result);
                return;
            }
            optimalRouteUpdated(result);


        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}