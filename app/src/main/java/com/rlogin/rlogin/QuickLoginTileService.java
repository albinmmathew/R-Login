package com.rlogin.rlogin;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class QuickLoginTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState(false, null);
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) return;

        // Check if device is connected to Wi-Fi
        if (!Net.isWifiConnected(this)) {
            Toast.makeText(this, "Wi-Fi is not connected. Connect to M-WiFi first.", Toast.LENGTH_SHORT).show();
            tile.setState(Tile.STATE_UNAVAILABLE);
            tile.setLabel("M-WiFi");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Wi-Fi Off");
            }
            tile.updateTile();
            return;
        }

        // Check credentials
        Db db = new Db(this);
        String[] saved = db.get("WIFI");
        if (saved == null || saved[0] == null || saved[0].trim().isEmpty() || saved[1] == null || saved[1].trim().isEmpty()) {
            Toast.makeText(this, "No credentials saved. Save Wi-Fi login in R-Login first.", Toast.LENGTH_LONG).show();
            tile.setState(Tile.STATE_INACTIVE);
            tile.setLabel("M-WiFi");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("No credentials");
            }
            tile.updateTile();
            return;
        }

        // Set visual state to "Connecting..."
        tile.setState(Tile.STATE_ACTIVE);
        tile.setLabel("M-WiFi");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.setSubtitle("Connecting...");
        }
        tile.updateTile();

        String user = saved[0];
        String pass = Crypt.dec(saved[1]);

        Net.loginWifi(this, user, pass, false, (success, message) -> {
            Tile currentTile = getQsTile();
            if (currentTile == null) return;

            if (success) {
                currentTile.setState(Tile.STATE_ACTIVE);
                currentTile.setLabel("M-WiFi");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    currentTile.setSubtitle("Connected");
                }
                currentTile.updateTile();

                // Revert to Inactive ("Tap to connect") after 6 seconds so user can click it again if disconnected later
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
            } else {
                currentTile.setState(Tile.STATE_INACTIVE);
                currentTile.setLabel("M-WiFi");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    currentTile.setSubtitle("Failed");
                }
                currentTile.updateTile();
            }
        });
    }

    private void updateTileState(boolean connecting, String customSubtitle) {
        Tile tile = getQsTile();
        if (tile == null) return;

        tile.setLabel("M-WiFi");

        if (!Net.isWifiConnected(this)) {
            tile.setState(Tile.STATE_UNAVAILABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle("Wi-Fi Off");
            }
        } else if (connecting) {
            tile.setState(Tile.STATE_ACTIVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle(customSubtitle != null ? customSubtitle : "Connecting...");
            }
        } else {
            tile.setState(Tile.STATE_INACTIVE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle(customSubtitle != null ? customSubtitle : "Tap to connect");
            }
        }

        tile.updateTile();
    }
}
