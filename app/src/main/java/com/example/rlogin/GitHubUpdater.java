package com.example.rlogin;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GitHubUpdater {
    private static final String GITHUB_RELEASES_API = "https://api.github.com/repos/albinmmathew/R-Login/releases/latest";
    private static final String CURRENT_VERSION = "1.0";

    public static void checkForUpdates(Context context, boolean showToastIfLatest) {
        if (showToastIfLatest) {
            Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show();
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(GITHUB_RELEASES_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                conn.setRequestProperty("User-Agent", "R-Login-Android-App");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();

                    JSONObject releaseJson = new JSONObject(builder.toString());
                    String tagName = releaseJson.optString("tag_name", "");
                    String body = releaseJson.optString("body", "");
                    JSONArray assets = releaseJson.optJSONArray("assets");

                    String apkDownloadUrl = null;
                    if (assets != null) {
                        for (int i = 0; i < assets.length(); i++) {
                            JSONObject asset = assets.getJSONObject(i);
                            String name = asset.optString("name", "");
                            if (name.endsWith(".apk")) {
                                apkDownloadUrl = asset.optString("browser_download_url", "");
                                break;
                            }
                        }
                    }

                    final String finalApkUrl = apkDownloadUrl;
                    String latestVersion = tagName.replaceAll("[^0-9.]", "");

                    if (isNewerVersion(latestVersion, CURRENT_VERSION) && finalApkUrl != null) {
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).runOnUiThread(() -> showUpdateDialog(context, tagName, body, finalApkUrl));
                        }
                    } else if (showToastIfLatest) {
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).runOnUiThread(() ->
                                    Toast.makeText(context, "You are on the latest version (" + CURRENT_VERSION + ")", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                } else if (showToastIfLatest) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Unable to check for updates right now.", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                if (showToastIfLatest) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).runOnUiThread(() ->
                                Toast.makeText(context, "Update check failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            } finally {
                executor.shutdown();
            }
        });
    }

    private static boolean isNewerVersion(String latest, String current) {
        if (latest.isEmpty()) return false;
        try {
            String[] latestParts = latest.split("\\.");
            String[] currentParts = current.split("\\.");
            int length = Math.max(latestParts.length, currentParts.length);
            for (int i = 0; i < length; i++) {
                int l = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
                int c = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                if (l > c) return true;
                if (l < c) return false;
            }
        } catch (Exception e) {
            return !latest.equals(current);
        }
        return false;
    }

    private static void showUpdateDialog(Context context, String tagName, String changelog, String apkUrl) {
        new AlertDialog.Builder(context)
                .setTitle("New Update Available (" + tagName + ")")
                .setMessage(changelog.isEmpty() ? "A new release is available for R-Login. Would you like to download and install it?" : changelog)
                .setPositiveButton("Download & Install", (dialog, which) -> startApkDownload(context, apkUrl))
                .setNegativeButton("Later", null)
                .show();
    }

    public static void startApkDownload(Context context, String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                new AlertDialog.Builder(context)
                        .setTitle("Permission Required")
                        .setMessage("To install the update directly, please allow R-Login to install unknown apps.")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                            intent.setData(Uri.parse("package:" + context.getPackageName()));
                            context.startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }
        }

        File apkFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "R-Login-update.apk");
        if (apkFile.exists()) {
            boolean deleted = apkFile.delete();
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle("Downloading R-Login Update");
        request.setDescription("Downloading latest release APK...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(apkFile));

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null) {
            long downloadId = downloadManager.enqueue(request);
            Toast.makeText(context, "Downloading update in background...", Toast.LENGTH_SHORT).show();

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadId) {
                        try {
                            context.unregisterReceiver(this);
                        } catch (Exception ignored) {}
                        installApk(context, apkFile);
                    }
                }
            };

            ContextCompat.registerReceiver(
                    context,
                    onComplete,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    ContextCompat.RECEIVER_EXPORTED
            );
        }
    }

    private static void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) return;
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(installIntent);
    }
}
