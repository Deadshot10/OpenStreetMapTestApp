package ru.volodya.apps.osmapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

public class MainActivity extends Activity {

    private ImageButton buttonClose, buttonRefresh, buttonSOS, buttonRoute, buttonMenu, buttonObjects;
    private TextView textViewRouteInfo;
    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.5F);
    private LocationManager locationManager;
    private Location deviceLocation;
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 1L, locationListener);
        }

        MapView map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        final IMapController mapController = map.getController();
        mapController.setZoom(18);

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

        textViewRouteInfo = (TextView) findViewById(R.id.tvRouteInfo);
        textViewRouteInfo.setText("время, расстояние");
        buttonClose = (ImageButton) findViewById(R.id.closeButton);
        buttonRefresh = (ImageButton) findViewById(R.id.refreshButton);
        buttonSOS = (ImageButton) findViewById(R.id.sosButton);
        buttonObjects= (ImageButton) findViewById(R.id.objectsButton);
        buttonRoute = (ImageButton) findViewById(R.id.routeButton);
        buttonMenu = (ImageButton) findViewById(R.id.menuButton);
        ImageButton locationButton = (ImageButton) findViewById(R.id.buttonMyLocation);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                setLocation(mapController);
            }
        });

        initImageButton(buttonSOS);
        initImageButton(buttonRefresh);
        initImageButton(buttonRoute);
        initImageButton(buttonMenu);

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

    private void setLocation(IMapController mc) {
        if (deviceLocation != null) {
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
    public void onResume(){
        super.onResume();
    }
}
