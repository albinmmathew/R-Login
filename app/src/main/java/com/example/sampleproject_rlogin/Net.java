package com.example.sampleproject_rlogin;

import android.content.Context;
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
            try {
                long ts = System.currentTimeMillis();
                String data = "mode=191&username=" + URLEncoder.encode(user, "UTF-8") +
                              "&password=" + URLEncoder.encode(pass, "UTF-8") +
                              "&a=" + ts + "&producttype=0";
                
                URL url = new URL("http://172.16.0.20:8090/login.xml");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();
                
                InputStream is = conn.getInputStream();
                Scanner sc = new Scanner(is).useDelimiter("\\A");
                String res = sc.hasNext() ? sc.next() : "";
                
                if (res.contains("LIVE")) {
                    msg = "M-WiFi Connected successfully!";
                } else if (res.contains("LOGIN") || res.contains("REJECTED")) {
                    msg = "Login failed: Invalid credentials or rejected";
                } else {
                    msg = "Login failed: Unexpected response";
                }
            } catch (Exception e) {
                msg = "Connection error: " + e.getMessage();
            }
            String finalMsg = msg;
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ctx, finalMsg, Toast.LENGTH_LONG).show());
        });
    }
}
