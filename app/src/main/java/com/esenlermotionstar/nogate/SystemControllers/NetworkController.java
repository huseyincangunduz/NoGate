package com.esenlermotionstar.nogate.SystemControllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContentManager;

public class NetworkController extends BroadcastReceiver {

    public enum ConnectionMode {
        Wireless, Cellular, Other, NoConnection, Unknown;
    }

    private static ConnectionMode connectionMode = ConnectionMode.Unknown;

    public static ConnectionMode getConnectionMode() {
        return connectionMode;
    }

    public static void setConnectionMode(ConnectionMode connectionMode) {
        NetworkController.connectionMode = connectionMode;
        ApprovedContentManager.setIterationListByNetwork(connectionMode);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        controlConnection(context);
    }

    public void controlConnection(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network network;
                NetworkCapabilities capabilities = null;
                network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    capabilities = connectivityManager.getNetworkCapabilities(network);
                }
                if (network == null || capabilities == null) {
                    setConnectionMode(ConnectionMode.NoConnection);
                } else {

                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        setConnectionMode(ConnectionMode.Wireless);
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        setConnectionMode(ConnectionMode.Cellular);
                    } else
                        setConnectionMode(ConnectionMode.Other);
                }
            } else {
                ConnectivityManager connMgr =
                        connectivityManager;
                boolean isConnected = false;
                boolean isWifiConn = false;
                boolean isMobileConn = false;
                for (NetworkInfo netinf : connMgr.getAllNetworkInfo()) {
                    if (netinf.isConnectedOrConnecting()) {
                        isConnected = true;
                        if (netinf.getType() == ConnectivityManager.TYPE_WIFI)
                            isWifiConn = true;
                        else if (netinf.getType() == ConnectivityManager.TYPE_MOBILE)
                            isMobileConn = true;
                    }
                    Log.i(null, "Network Info: " + netinf.getTypeName());
                }
                if (!isConnected) {
                    setConnectionMode(ConnectionMode.NoConnection);
                } else if (isMobileConn) {
                    setConnectionMode(ConnectionMode.Cellular);
                } else if (isWifiConn) {
                    setConnectionMode(ConnectionMode.Wireless);

                } else
                    setConnectionMode(ConnectionMode.Other);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setConnectionMode(ConnectionMode.NoConnection);
        }
    }

    static boolean registered = false;
    public static boolean isRegistered()
    {
        return registered;
    }

    public void registerThis(Context context) {
        if (!registered) {
            IntentFilter filter1 = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

            context.registerReceiver(this, filter1);
            controlConnection(context);
            registered = true;
        }

    }

    public void unregisterThis(Context context) {
        if (registered) {
            context.unregisterReceiver(this);
            registered = false;
        }
    }


}
