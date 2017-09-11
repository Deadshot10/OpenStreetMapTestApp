package ru.volodya.apps.osmapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final long LOCATION_UPDATE_TIME = 500L;
    private static final long LOCATION_UPDATE_DISTANCE = 1L;

    private ImageButton buttonClose, buttonRefresh, buttonSOS, buttonRoute, buttonMenu,
            buttonObjects, buttonScaleUp, buttonScaleDown, locationButton;
    private TextView textViewRouteInfo;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);

    private LocationManager locationManager;
    private Location deviceLocation;
    private MapView mapView;
    private ConnectionObserver connectionObserver;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            deviceLocation = location;
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);//randomize?
        setContentView(R.layout.activity_main);

        //Get "dangerous" permission on sdk 23.0 and higher
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initLocationManager();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                initLocationManager();
            } else {
                requestRequiredPermissions();
            }
        }

        textViewRouteInfo = (TextView) findViewById(R.id.tvRouteInfo);
        textViewRouteInfo.setText("время, расстояние");
        buttonClose = (ImageButton) findViewById(R.id.closeButton);
        buttonRefresh = (ImageButton) findViewById(R.id.refreshButton);
        buttonSOS = (ImageButton) findViewById(R.id.sosButton);
        buttonObjects= (ImageButton) findViewById(R.id.objectsButton);
        buttonRoute = (ImageButton) findViewById(R.id.routeButton);
        buttonMenu = (ImageButton) findViewById(R.id.menuButton);
        buttonScaleDown = (ImageButton) findViewById(R.id.buttonScaleDown);
        buttonScaleUp = (ImageButton) findViewById(R.id.buttonScaleUp);
        locationButton = (ImageButton) findViewById(R.id.buttonMyLocation);

        initImageButton(buttonSOS);
        initImageButton(buttonRefresh);
        initImageButton(buttonRoute);
        initImageButton(buttonMenu);
        initImageButton(buttonScaleDown);
        initImageButton(buttonScaleUp);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                finish();
            }
        });
        buttonObjects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                startActivity(new Intent(getApplication(), ObjectsActivity.class));
            }
        });
    }

    private void requestRequiredPermissions() {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSION_REQUEST_CODE);
    }

    private void initLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            deviceLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            registerLocationUpdates();
        }
    }

    private void setLocation(IMapController mc) {
        if (deviceLocation != null && mc != null) {
            GeoPoint startPoint = new GeoPoint(deviceLocation.getLatitude(), deviceLocation.getLongitude());
            mc.setCenter(startPoint);
        }
    }

    private void initImageButton(ImageButton imageButton){
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocationManager();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume(){
        super.onResume();
        registerLocationUpdates();
    }

    private void registerLocationUpdates(){
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_TIME, LOCATION_UPDATE_DISTANCE, locationListener);
            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_TIME, LOCATION_UPDATE_DISTANCE, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mapView = (MapView) findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        //mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        final IMapController mapController = mapView.getController();
        mapController.setZoom(16);

        //add listener for 2G traffic block
        connectionObserver = new ConnectionObserver(this, mapView.getTileProvider());

        //center screen on gps position
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (deviceLocation == null){
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                }
                setLocation(mapController);
            }
        }).start();

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                setLocation(mapController);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectionObserver.finish();
    }
}
