package com.rlogin.rlogin;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Net {
    public static void loginWifi(Context ctx, String user, String pass) {
        Toast.makeText(ctx, "Connecting to M-WiFi...", Toast.LENGTH_SHORT).show();
        Executors.newSingleThreadExecutor().execute(() -> {
            String msg;
            HttpURLConnection conn = null;
            try {
                ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network wifiNetwork = findWifiNetwork(cm);

                Network defaultNet = cm != null ? cm.getActiveNetwork() : null;
                NetworkCapabilities defaultCaps = defaultNet != null ? cm.getNetworkCapabilities(defaultNet) : null;
                boolean defaultIsCellular = defaultCaps != null && defaultCaps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);

                if (wifiNetwork == null && defaultIsCellular) {
                    msg = "Wi-Fi is not connected. Please connect to M-WiFi first.";
                } else {
                    long ts = System.currentTimeMillis();
                    String data = "mode=191&username=" + URLEncoder.encode(user, "UTF-8") +
                                  "&password=" + URLEncoder.encode(pass, "UTF-8") +
                                  "&a=" + ts + "&producttype=0";

                    URL url = new URL("http://172.16.0.20:8090/login.xml");
                    if (wifiNetwork != null) {
                        conn = (HttpURLConnection) wifiNetwork.openConnection(url);
                    } else {
                        conn = (HttpURLConnection) url.openConnection();
                    }

                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    conn.setUseCaches(false);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(data.getBytes());
                        os.flush();
                    }

                    String res = "";
                    try (InputStream is = conn.getInputStream();
                         Scanner sc = new Scanner(is).useDelimiter("\\A")) {
                        res = sc.hasNext() ? sc.next() : "";
                    }

                    if (res.contains("LIVE")) {
                        msg = "M-WiFi Connected successfully!";
                        if (cm != null) {
                            cm.reportNetworkConnectivity(wifiNetwork, true);
                        }
                    } else if (res.contains("LOGIN") || res.contains("REJECTED")) {
                        msg = "Login failed: Invalid credentials or rejected";
                    } else {
                        msg = "Login failed: Unexpected response";
                    }
                }
            } catch (Exception e) {
                msg = "Connection error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            String finalMsg = msg;
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ctx, finalMsg, Toast.LENGTH_LONG).show());
        });
    }

    private static Network findWifiNetwork(ConnectivityManager cm) {
        if (cm == null) return null;
        for (Network network : cm.getAllNetworks()) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
                return network;
            }
        }
        return null;
    }
}
