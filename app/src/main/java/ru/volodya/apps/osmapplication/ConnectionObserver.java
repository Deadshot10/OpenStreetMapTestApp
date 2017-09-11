package ru.volodya.apps.osmapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.osmdroid.tileprovider.MapTileProviderBase;

public class ConnectionObserver extends PhoneStateListener{

    public static final String TAG = "ConnectionObserver";

    private final TelephonyManager telephonyManager;
    private final MapTileProviderBase tileProvider;
    private Context context;
    private int networkSubtype = -1;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onChangeNetworkType();
        }
    };

    private void onChangeNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            switch (activeNetwork.getType()){
                case ConnectivityManager.TYPE_WIFI:
                    Log.d(TAG, "using Wi-Fi");
                    allowMapDownload(true);
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    Log.d(TAG, "using Mobile");
                    String subtypeName = Utility.getNetworkClassById(networkSubtype);
                    allowMapDownload(!subtypeName.equals("2G"));
                    break;
            }
        } else {
            Log.d(TAG, "No active network");
            allowMapDownload(false);
        }
    }

    ConnectionObserver(Context context, MapTileProviderBase tileProvider){
        this.context = context;
        this.tileProvider = tileProvider;

        context.registerReceiver(broadcastReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(this, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    @Override
    public void onDataConnectionStateChanged(int state, int networkType) {
        networkSubtype = networkType;
        onChangeNetworkType();
    }

    public void allowMapDownload(boolean status){
        Log.d(TAG, "Download tiles: " + status);
        tileProvider.setUseDataConnection(status);
    }

    public void finish(){
        context.unregisterReceiver(broadcastReceiver);
    }
}
