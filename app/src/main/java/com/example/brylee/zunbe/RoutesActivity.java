package com.example.brylee.zunbe;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.brylee.zunbe.aco.Route;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by brylee on 5/5/17.
 */

public class RoutesActivity extends AppCompatActivity {
    ListView mRouteList;
    RouteAdapter adapter;
    ArrayList<Route> routes = new ArrayList<>();
    public static final int STATUS_NEW_ROUTE = 100;
    public static final int STATUS_VIEW_ROUTE = 200;
    DatabaseHelper dbHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        mRouteList = (ListView) findViewById(R.id.list_route);
        adapter = new RouteAdapter(this, routes);
        mRouteList.setAdapter(adapter);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_route_toolbar);
        myToolbar.setTitle("");

        setSupportActionBar(myToolbar);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(getApplicationContext());

        // load data from sqlite
        routes.addAll(dbHelper.getAllRoutes());

        // view action
        mRouteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
                intent.putExtra("route", routes.get(position));
                intent.putExtra("index", position);
                intent.putExtra("status", STATUS_VIEW_ROUTE);
                startActivityForResult(intent, STATUS_VIEW_ROUTE);
            }
        });

        // delete action
        mRouteList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                createDeleteConfirmDialog(position).show();

                return true;
            }
        });

    }

    private AlertDialog createDeleteConfirmDialog(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this route?\nThis will delete all the locations as well.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbHelper.deleteRoute(routes.get(pos).getId());
                        adapter.remove(adapter.getItem(pos));
                        Toast.makeText(getApplicationContext(), "Deleted the route!", Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_route_item, menu);
        return true;
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_route) {

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Title of this route?"); //Message here

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            input.setMaxLines(1);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            alert.setView(input);

            alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String title = input.getEditableText().toString();
                    if (title.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please input title", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Route newRoute = new Route(title);
                    long route_id = dbHelper.createRoute(newRoute);
                    newRoute.setId((int) route_id);
                    adapter.add(newRoute);

                    Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
                    intent.putExtra("route", routes.get(routes.size()-1));
                    intent.putExtra("index", routes.size()-1);
                    intent.putExtra("status", STATUS_NEW_ROUTE);
                    startActivityForResult(intent, STATUS_NEW_ROUTE);
                }
            }); //End of alert.setPositiveButton
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            }); //End of alert.setNegativeButton
            AlertDialog alertDialog = alert.create();
            alertDialog.show();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (STATUS_NEW_ROUTE):
            case (STATUS_VIEW_ROUTE):
                adapter.clear();
                adapter.addAll(dbHelper.getAllRoutes());

        }
        // TODO
    }
}
