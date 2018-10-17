package com.example.brylee.zunbe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.brylee.zunbe.aco.Route;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by brylee on 5/5/17.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "zunbeDB";

    // Table Names
    private static final String TABLE_ROUTE = "route";
    private static final String TABLE_LOCATION = "location";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // ROUTE Table - column names
    private static final String KEY_ROUTE_TITLE = "title";
    private static final String KEY_ROUTE_DISTANCE = "distance";

    // LOCATION Table - column names
    private static final String KEY_LOC_ROUTE = "location_route";
    private static final String KEY_LOC_NAME = "name";
    private static final String KEY_LOC_LATITUDED = "latituded";
    private static final String KEY_LOC_LATITUDER = "latituder";
    private static final String KEY_LOC_LONGITUDED = "longituded";
    private static final String KEY_LOC_LONGITUDER = "longituder";
    private static final String KEY_LOC_ADDRESS = "address";


    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_ROUTE = "CREATE TABLE "
            + TABLE_ROUTE + "(" + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ROUTE_TITLE
            + " TEXT," + KEY_ROUTE_DISTANCE + " REAL," + KEY_CREATED_AT
            + " DATETIME" + ")";

    // Location table create statement
    private static final String CREATE_TABLE_LOCATION = "CREATE TABLE " + TABLE_LOCATION
            + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_LOC_ROUTE + " INTEGER, "
            + KEY_LOC_NAME + " TEXT,"
            + KEY_LOC_LATITUDED + " REAL,"
            + KEY_LOC_LATITUDER + " REAL, "
            + KEY_LOC_LONGITUDED + " REAL, "
            + KEY_LOC_LONGITUDER + " REAL, "
            + KEY_LOC_ADDRESS + " TEXT, "
            + KEY_CREATED_AT + " DATETIME" + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_ROUTE);
        db.execSQL(CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROUTE);

        // create new tables
        onCreate(db);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    /*
 * Creating a Route
 */
    public long createRoute(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_TITLE, route.getTitle());
        values.put(KEY_ROUTE_DISTANCE, route.getDistance());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long route_id = db.insert(TABLE_ROUTE, null, values);

        // adding locations
        for (Location loc : route.getLocations()) {
            createLocation(route_id, loc);
        }

        return route_id;
    }

    public ArrayList<Route> getAllRoutes() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_ROUTE;
        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        ArrayList<Route> routes = new ArrayList<>();

        if (c != null && c.moveToFirst()) {
            do {
                long route_id = c.getLong(c.getColumnIndex(KEY_ID));
                Route r = getRoute(route_id);
                routes.add(r);
            } while (c.moveToNext());
            c.close();
        }
        return routes;
    }
    /*
 * get single Route
 */
    public Route getRoute(long route_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_ROUTE + " WHERE "
                + KEY_ID + " = " + route_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null && c.moveToFirst()) {
            Route route = new Route();
            route.setId(c.getInt(c.getColumnIndex(KEY_ID)));
            route.setTitle((c.getString(c.getColumnIndex(KEY_ROUTE_TITLE))));
            route.setDistance(c.getDouble(c.getColumnIndex(KEY_ROUTE_DISTANCE)));
            route.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));

            ArrayList<Location> locations = getLocations(route_id);

            route.setLocations(locations);
            c.close();
            return route;
        }
        Log.e(LOG, "DOES NOT EXIST. QUERY: " + selectQuery);
        return null;
    }

    /*
 * Updating a Route
 */
    public int updateRoute(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_TITLE, route.getTitle());
        values.put(KEY_ROUTE_DISTANCE, route.getDistance());

        // updating row
        return db.update(TABLE_ROUTE, values, KEY_ID + " = ?",
                new String[] { String.valueOf(route.getId()) });
    }

    public int updateRouteAndLocations(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROUTE_TITLE, route.getTitle());
        values.put(KEY_ROUTE_DISTANCE, route.getDistance());
        values.put(KEY_CREATED_AT, getDateTime());

        for (Location loc : route.getLocations()) {
            ContentValues locValues = new ContentValues();
            locValues.put(KEY_LOC_ROUTE, route.getId());
            locValues.put(KEY_LOC_NAME, loc.getName());
            locValues.put(KEY_LOC_LATITUDED, loc.getLatitudeDegrees());
            locValues.put(KEY_LOC_LATITUDER, loc.getLatitudeRadians());
            locValues.put(KEY_LOC_LONGITUDED, loc.getLongitudeDegrees());
            locValues.put(KEY_LOC_LONGITUDER, loc.getLongitudeRadians());
            locValues.put(KEY_LOC_ADDRESS, loc.getAddress());
            db.update(TABLE_LOCATION, locValues, KEY_ID + " = ?",
                    new String[] { String.valueOf(loc.getId()) });
        }

        return db.update(TABLE_ROUTE, values, KEY_ID + " = ?",
                new String[] { String.valueOf(route.getId()) });
    }

    /*
     * Deleting a Route
     */
    public void deleteRoute(long route_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION, KEY_LOC_ROUTE + " = ?", new String[]{String.valueOf(route_id)});
        db.delete(TABLE_ROUTE, KEY_ID + " = ?",
                new String[]{String.valueOf(route_id)});
    }

    public long createLocation(long route_id, Location loc) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LOC_ROUTE, route_id);
        values.put(KEY_LOC_NAME, loc.getName());
        values.put(KEY_LOC_LATITUDED, loc.getLatitudeDegrees());
        values.put(KEY_LOC_LATITUDER, loc.getLatitudeRadians());
        values.put(KEY_LOC_LONGITUDED, loc.getLongitudeDegrees());
        values.put(KEY_LOC_LONGITUDER, loc.getLongitudeRadians());
        values.put(KEY_LOC_ADDRESS, loc.getAddress());
        values.put(KEY_CREATED_AT, getDateTime());

        // insert row
        long loc_id = db.insert(TABLE_LOCATION, null, values);

        return loc_id;
    }



    public Location getLocation(long loc_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION + " WHERE "
                + KEY_ID + " = " + loc_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null && c.moveToFirst()) {
            Location location = new Location();
            location.setId(c.getInt((c.getColumnIndex(KEY_ID))));
            location.setName(c.getString(c.getColumnIndex(KEY_LOC_NAME)));
            location.setLatitudeDegrees((c.getDouble(c.getColumnIndex(KEY_LOC_LATITUDED))));
            location.setLatitudeRadians((c.getDouble(c.getColumnIndex(KEY_LOC_LATITUDER))));
            location.setLongitudeDegrees((c.getDouble(c.getColumnIndex(KEY_LOC_LONGITUDED))));
            location.setLongitudeRadians((c.getDouble(c.getColumnIndex(KEY_LOC_LONGITUDER))));
            location.setAddress(c.getString(c.getColumnIndex(KEY_LOC_ADDRESS)));
            location.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));
            c.close();
            return location;
        }

        Log.e(LOG, "DOES NOT EXIST. QUERY: " + selectQuery);
        return null;
    }



    public ArrayList<Location> getLocations(long route_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_LOCATION + " WHERE "
                + KEY_LOC_ROUTE + " = " + route_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        ArrayList<Location> locations = new ArrayList<>();

        if (c != null && c.moveToFirst()) {
            do {
                Location location = new Location();
                location.setId(c.getInt((c.getColumnIndex(KEY_ID))));
                location.setName(c.getString(c.getColumnIndex(KEY_LOC_NAME)));
                location.setLatitudeDegrees((c.getDouble(c.getColumnIndex(KEY_LOC_LATITUDED))));
                location.setLatitudeRadians((c.getDouble(c.getColumnIndex(KEY_LOC_LATITUDER))));
                location.setLongitudeDegrees((c.getDouble(c.getColumnIndex(KEY_LOC_LONGITUDED))));
                location.setLongitudeRadians((c.getDouble(c.getColumnIndex(KEY_LOC_LONGITUDER))));
                location.setAddress(c.getString(c.getColumnIndex(KEY_LOC_ADDRESS)));
                location.setCreatedAt(c.getString(c.getColumnIndex(KEY_CREATED_AT)));
                locations.add(location);
            } while (c.moveToNext());
            c.close();
        }
        return locations;
    }


    /*
 * Deleting a Route
 */
    public void deleteLocation(long loc_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION, KEY_ID + " = ?",
                new String[] { String.valueOf(loc_id) });
    }

    /**
     * get datetime
     */
    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


}
