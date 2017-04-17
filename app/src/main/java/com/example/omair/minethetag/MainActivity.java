package com.example.omair.minethetag;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;
import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;

import static com.example.omair.minethetag.LoginActivity.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static com.example.omair.minethetag.LoginActivity.latitude;
import static com.example.omair.minethetag.LoginActivity.longitude;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    LocationManager locationManager;
    double posMinaX, posMinaY;
    int pos = 1;
    double radi = 0.00019;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(30 * 1000)
                        .setMetersBetweenUpdates(5);

        LocationTracker tracker = new LocationTracker(getApplicationContext(), settings) {

            @Override
            public void onLocationFound(Location location) {
                Toast.makeText(getApplicationContext(), "Latitude - " + location.getLatitude(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTimeout() {

            }
        };
        //tracker.startListening();

        if (SmartLocation.with(getApplicationContext()).location().state().locationServicesEnabled())
        {
            Toast.makeText(getApplicationContext(), "ENABLED", Toast.LENGTH_SHORT).show();
        }
        else if (SmartLocation.with(getApplicationContext()).location().state().isAnyProviderAvailable())
        {
            Toast.makeText(getApplicationContext(), "Providers", Toast.LENGTH_SHORT).show();
        }

        SmartLocation.with(getApplicationContext()).activityRecognition()
                .start(new OnActivityUpdatedListener() {

                    @Override
                    public void onActivityUpdated(DetectedActivity a)
                    {
                        Toast.makeText(getApplicationContext(), "Latitude : " + latitude, Toast.LENGTH_SHORT).show();
                        posMinaX = latitude;
                        posMinaY = longitude;
                    }
                });

        final MapView map = (MapView) findViewById(R.id.mapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        IMapController mapController = map.getController();
        mapController.setZoom(20);
        GeoPoint startPoint = new GeoPoint(latitude, longitude);
        mapController.setCenter(startPoint);

        ArrayList<OverlayItem> overlayItemArray;
        overlayItemArray = new ArrayList<OverlayItem>();
        OverlayItem linkopingItem = new OverlayItem("Current", "Location", new GeoPoint(latitude, longitude));
        Drawable newMarker = this.getResources().getDrawable(R.drawable.icon);
        linkopingItem.setMarker(newMarker);
        overlayItemArray.add(linkopingItem);
        final ItemizedIconOverlay<OverlayItem> itemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(this, overlayItemArray, null);

        // Add the overlay to the MapView
        map.getOverlays().add(itemizedIconOverlay);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(getApplicationContext(), map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);
        map.setBuiltInZoomControls(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.mina);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<OverlayItem> overlayItemArray = new ArrayList<OverlayItem>();
                OverlayItem mina = new OverlayItem("New", "Mina", new GeoPoint(posMinaX, posMinaY));//marker
                Drawable newMarker = getResources().getDrawable(R.drawable.mine28);
                mina.setMarker(newMarker);
                overlayItemArray.add(mina);
                if (pos == 1)
                {
                    posMinaX = posMinaX + radi;
                    //posMinaY = posMinaY + radi;
                    pos = 2;
                    Toast.makeText(getApplicationContext(),"POS=1", Toast.LENGTH_SHORT).show();
                }
                else if (pos == 2)
                {
                    //posMinaX = posMinaX + radi;
                    posMinaY = posMinaY - radi;
                    pos = 3;
                    Toast.makeText(getApplicationContext(),"POS=2", Toast.LENGTH_SHORT).show();
                }
                else if (pos == 3)
                {
                    posMinaX = posMinaX - radi;
                    posMinaY = posMinaY - radi;
                    pos = 4;
                    Toast.makeText(getApplicationContext(),"POS=3", Toast.LENGTH_SHORT).show();
                }
                else if (pos == 4)
                {
                    posMinaX = posMinaX - radi;
                    //posMinaY = posMinaY - radi;
                    pos = 5;
                    Toast.makeText(getApplicationContext(),"POS=4", Toast.LENGTH_SHORT).show();
                }
                else if (pos == 5)
                {
                    posMinaX = posMinaX - radi;
                    posMinaY = posMinaY + radi;
                    pos = 6;
                    Toast.makeText(getApplicationContext(),"POS=5", Toast.LENGTH_SHORT).show();
                }
                else if (pos == 6)
                {
                    posMinaY = posMinaY - radi;
                    pos = 7;
                    Toast.makeText(getApplicationContext(),"POS=6", Toast.LENGTH_SHORT).show();
                }
                else if (pos == 7)
                {
                    posMinaX = posMinaX + radi;
                    posMinaY = posMinaY - radi;
                    pos = 1;
                    Toast.makeText(getApplicationContext(),"POS=8", Toast.LENGTH_SHORT).show();
                }
                MyOwnItemizedOverlay overlay = new MyOwnItemizedOverlay(getApplicationContext(), overlayItemArray);
                map.getOverlays().add(overlay);
                map.invalidate();
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
