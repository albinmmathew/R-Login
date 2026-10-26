package com.rlogin.rlogin;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public class WifiAutoConnectManager {
    public static final String PREFS_NAME = "rlogin_settings";
    public static final String KEY_AUTO_WIFI = "pref_auto_wifi_login";

    private static ConnectivityManager.NetworkCallback networkCallback;
    private static long lastLoginAttempt = 0;

    public static boolean isAutoLoginEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_AUTO_WIFI, false);
    }

    public static void setAutoLoginEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_AUTO_WIFI, enabled).apply();
        if (enabled) {
            register(context.getApplicationContext());
        } else {
            unregister(context.getApplicationContext());
        }
    }

    public static void init(Context context) {
        if (isAutoLoginEnabled(context)) {
            register(context.getApplicationContext());
        }
    }

    public static synchronized void register(Context context) {
        if (networkCallback != null) {
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                // Debounce by 8 seconds to prevent duplicate hits during handshake
                long now = System.currentTimeMillis();
                if (now - lastLoginAttempt < 8000) {
                    return;
                }
                lastLoginAttempt = now;

                Db db = new Db(context);
                String[] saved = db.get("WIFI");
                if (saved != null && saved[0] != null && !saved[0].isEmpty() && saved[1] != null && !saved[1].isEmpty()) {
                    String pass = Crypt.dec(saved[1]);
                    Net.loginWifi(context, saved[0], pass, true);
                }
            }
        };

        try {
            cm.registerNetworkCallback(request, networkCallback);
        } catch (Exception e) {
            e.printStackTrace();
            networkCallback = null;
        }
    }

    public static synchronized void unregister(Context context) {
        if (networkCallback == null) return;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            try {
                cm.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {}
        }
        networkCallback = null;
    }
}
