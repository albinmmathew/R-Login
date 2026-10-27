package com.rlogin.rlogin;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class QuickLoginTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) return;

        // Step 1: Check if device is connected to Wi-Fi
        if (!Net.isWifiConnected(this)) {
            StatusNotifier.notifyError(this, "Wi-Fi is not connected. Please connect to campus Wi-Fi first.");
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("M-WiFi");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Wi-Fi Off");
            }
            tile.updateTile();
            return;
        }

        // Step 2: Check credentials
        Db db = new Db(this);
        String[] saved = db.get("WIFI");
        if (saved == null || saved[0] == null || saved[0].trim().isEmpty() || saved[1] == null || saved[1].trim().isEmpty()) {
            StatusNotifier.notifyError(this, "No credentials saved");
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("M-WiFi");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("No credentials");
            }
            tile.updateTile();
            return;
        }

        // Step 3: Set visual state to "Connecting..."
        tile.setState(Tile.STATE_ACTIVE);
        tile.setLabel("Connecting...");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.setSubtitle("Connecting...");
        }
        tile.updateTile();

        String user = saved[0];
        String pass = Crypt.dec(saved[1]);

        // Step 4: Login
        Net.loginWifi(this, user, pass, false, (success, message) -> {
            Tile currentTile = getQsTile();

            if (success) {
                StatusNotifier.clearNotification(this);
                StatusNotifier.vibrate(this, true); // 1 crisp tick

                if (currentTile != null) {
                    currentTile.setState(Tile.STATE_ACTIVE);
                    currentTile.setLabel("M-WiFi");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        currentTile.setSubtitle("Connected");
                    }
                    currentTile.updateTile();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Tile t = getQsTile();
                        if (t != null) {
                            t.setState(Tile.STATE_INACTIVE);
                            t.setLabel("M-WiFi");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                t.setSubtitle("Tap to connect");
                            }
                            t.updateTile();
                        }
                    }, 6000);
                }
            } else {
                String errorMsg = (message != null && !message.isEmpty()) ? message : "Could not reach gateway 172.16.0.20";
                StatusNotifier.notifyError(this, errorMsg);

                if (currentTile != null) {
                    currentTile.setState(Tile.STATE_INACTIVE);
                    currentTile.setLabel("M-WiFi");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        currentTile.setSubtitle("Failed");
                    }
                    currentTile.updateTile();
                }
            }
        });
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null) return;

        tile.setLabel("M-WiFi");

        if (!Net.isWifiConnected(this)) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Wi-Fi Off");
            }
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Tap to connect");
            }
        }

        tile.updateTile();
    }
}
