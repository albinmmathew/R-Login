package com.example.rlogin;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
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

    private static AlertDialog createNeoDialog(Context context, View view) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    public static void checkForUpdates(Context context, boolean showModalImmediately) {
        String currentVersion = BuildConfig.VERSION_NAME;

        final AlertDialog[] liveDialog = new AlertDialog[1];
        final View[] dialogView = new View[1];

        if (showModalImmediately && context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                if (!ThemeManager.isBrutalist(context)) {
                    Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show();
                    return;
                }
                View v = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
                dialogView[0] = v;
                liveDialog[0] = createNeoDialog(context, v);

                TextView title = v.findViewById(R.id.txt_dialog_title);
                TextView curVer = v.findViewById(R.id.badge_current_version);
                ProgressBar progress = v.findViewById(R.id.progress_checking);
                TextView statusMsg = v.findViewById(R.id.txt_status_message);
                Button btnNeg = v.findViewById(R.id.btn_negative);
                Button btnPos = v.findViewById(R.id.btn_positive);

                title.setText("CHECK FOR UPDATES");
                curVer.setText("v" + currentVersion);
                progress.setVisibility(View.VISIBLE);
                statusMsg.setText("CHECKING GITHUB RELEASES...");
                btnNeg.setText("CANCEL");
                btnNeg.setOnClickListener(view -> liveDialog[0].dismiss());
                btnPos.setVisibility(View.GONE);

                liveDialog[0].show();
            });
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
                        builder.append(line).append("\n");
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

                    Db db = new Db(context);
                    if (isNewerVersion(latestVersion, currentVersion) && finalApkUrl != null) {
                        db.saveReleaseCache("PENDING", tagName, body, finalApkUrl);
                        if (context instanceof Activity) {
                            ((Activity) context).runOnUiThread(() -> {
                                if (ThemeManager.isBrutalist(context)) {
                                    if (liveDialog[0] != null && liveDialog[0].isShowing()) {
                                        populateUpdateDialog(context, dialogView[0], liveDialog[0], tagName, body, finalApkUrl);
                                    } else {
                                        showUpdateDialog(context, tagName, body, finalApkUrl, currentVersion);
                                    }
                                } else {
                                    CharSequence formattedNotes = formatMarkdown(body.isEmpty() ?
                                            "A new release (" + tagName + ") is available for R-Login. Would you like to download and install it?" : body);
                                    AlertDialog dialog = new AlertDialog.Builder(context)
                                            .setTitle("New Update Available (" + tagName + ")")
                                            .setMessage(formattedNotes)
                                            .setPositiveButton("Download & Install", (d, which) -> startApkDownload(context, finalApkUrl))
                                            .setNegativeButton("Later", null)
                                            .show();
                                    TextView messageView = dialog.findViewById(android.R.id.message);
                                    if (messageView != null) {
                                        messageView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
                                    }
                                }
                            });
                        }
                    } else {
                        // User is on the latest version or beyond!
                        db.saveReleaseCache("CURRENT", tagName, body, finalApkUrl);
                        db.clearPendingRelease();
                        if (showModalImmediately && context instanceof Activity) {
                            ((Activity) context).runOnUiThread(() -> {
                                if (ThemeManager.isBrutalist(context)) {
                                    if (dialogView[0] != null) {
                                        View v = dialogView[0];
                                        v.findViewById(R.id.progress_checking).setVisibility(View.GONE);
                                        TextView statusMsg = v.findViewById(R.id.txt_status_message);
                                        statusMsg.setText("YOU ARE ON THE LATEST VERSION!\nv" + currentVersion + " is up to date.");
                                        Button btnNeg = v.findViewById(R.id.btn_negative);
                                        btnNeg.setText("CLOSE");
                                        v.findViewById(R.id.btn_positive).setVisibility(View.GONE);
                                    }
                                } else {
                                    Toast.makeText(context, "You are on the latest version (" + currentVersion + ")", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else if (showModalImmediately) {
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            if (ThemeManager.isBrutalist(context)) {
                                if (dialogView[0] != null) {
                                    View v = dialogView[0];
                                    v.findViewById(R.id.progress_checking).setVisibility(View.GONE);
                                    TextView statusMsg = v.findViewById(R.id.txt_status_message);
                                    statusMsg.setText("COULD NOT REACH GITHUB.\nPlease try again later.");
                                    Button btnNeg = v.findViewById(R.id.btn_negative);
                                    btnNeg.setText("CLOSE");
                                    v.findViewById(R.id.btn_positive).setVisibility(View.GONE);
                                }
                            } else {
                                Toast.makeText(context, "Unable to check for updates right now.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                conn.disconnect();
            } catch (Exception e) {
                if (showModalImmediately && context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        if (ThemeManager.isBrutalist(context)) {
                            if (dialogView[0] != null) {
                                View v = dialogView[0];
                                v.findViewById(R.id.progress_checking).setVisibility(View.GONE);
                                TextView statusMsg = v.findViewById(R.id.txt_status_message);
                                statusMsg.setText("UPDATE CHECK FAILED\n" + e.getLocalizedMessage());
                                Button btnNeg = v.findViewById(R.id.btn_negative);
                                btnNeg.setText("CLOSE");
                                v.findViewById(R.id.btn_positive).setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(context, "Update check failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
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

    private static void populateUpdateDialog(Context context, View v, AlertDialog dialog, String tagName, String changelog, String apkUrl) {
        v.findViewById(R.id.progress_checking).setVisibility(View.GONE);
        TextView title = v.findViewById(R.id.txt_dialog_title);
        title.setText("UPDATE AVAILABLE");

        TextView latestVer = v.findViewById(R.id.badge_latest_version);
        latestVer.setText(tagName);
        latestVer.setVisibility(View.VISIBLE);

        TextView statusMsg = v.findViewById(R.id.txt_status_message);
        statusMsg.setText("NEW VERSION IS READY!");

        if (!changelog.isEmpty()) {
            ScrollView scroll = v.findViewById(R.id.scroll_changelog);
            TextView txtChangelog = v.findViewById(R.id.txt_changelog);
            txtChangelog.setText(formatMarkdown(changelog));
            scroll.setVisibility(View.VISIBLE);
        }

        Button btnNeg = v.findViewById(R.id.btn_negative);
        btnNeg.setText("LATER");
        btnNeg.setOnClickListener(view -> dialog.dismiss());

        Button btnPos = v.findViewById(R.id.btn_positive);
        btnPos.setText("DOWNLOAD & INSTALL →");
        btnPos.setVisibility(View.VISIBLE);
        btnPos.setOnClickListener(view -> {
            dialog.dismiss();
            startApkDownload(context, apkUrl);
        });
    }

    private static void showUpdateDialog(Context context, String tagName, String changelog, String apkUrl, String currentVersion) {
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
        AlertDialog dialog = createNeoDialog(context, v);

        TextView curVer = v.findViewById(R.id.badge_current_version);
        curVer.setText("v" + currentVersion);

        populateUpdateDialog(context, v, dialog, tagName, changelog, apkUrl);
        dialog.show();
    }

    public static CharSequence formatMarkdown(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String[] lines = text.split("\n");
        StringBuilder html = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("### ")) {
                html.append("<br><b><font color='#002868'>").append(trimmed.substring(4)).append("</font></b><br>");
            } else if (trimmed.startsWith("## ")) {
                html.append("<br><b><big>").append(trimmed.substring(3)).append("</big></b><br>");
            } else if (trimmed.startsWith("# ")) {
                html.append("<b><big>").append(trimmed.substring(2)).append("</big></b><br>");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                html.append("&nbsp;&nbsp;•&nbsp;").append(trimmed.substring(2)).append("<br>");
            } else if (!trimmed.isEmpty()) {
                html.append(trimmed).append("<br>");
            }
        }
        String processed = html.toString()
                .replaceAll("\\[([^\\]]+)\\]\\((https?://[^\\)]+)\\)", "<a href=\"$2\">$1</a>")
                .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                .replaceAll("\\*(.*?)\\*", "<i>$1</i>");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(processed, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(processed);
        }
    }

    public static void startApkDownload(Context context, String apkUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                if (ThemeManager.isBrutalist(context)) {
                    View v = LayoutInflater.from(context).inflate(R.layout.dialog_update, null);
                    AlertDialog dialog = createNeoDialog(context, v);

                    TextView title = v.findViewById(R.id.txt_dialog_title);
                    title.setText("PERMISSION REQUIRED");
                    v.findViewById(R.id.progress_checking).setVisibility(View.GONE);

                    TextView statusMsg = v.findViewById(R.id.txt_status_message);
                    statusMsg.setText("To install the update, please enable 'Install unknown apps' for R-Login in Android Settings.");

                    Button btnNeg = v.findViewById(R.id.btn_negative);
                    btnNeg.setText("CANCEL");
                    btnNeg.setOnClickListener(view -> dialog.dismiss());

                    Button btnPos = v.findViewById(R.id.btn_positive);
                    btnPos.setText("OPEN SETTINGS →");
                    btnPos.setVisibility(View.VISIBLE);
                    btnPos.setOnClickListener(view -> {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    });

                    dialog.show();
                } else {
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
                }
                return;
            }
        }

        Toast.makeText(context, "Starting download...", Toast.LENGTH_SHORT).show();

        try {
            File downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File destinationFile = new File(downloadsDir, "R-Login-update.apk");
            if (destinationFile.exists()) {
                destinationFile.delete();
            }

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl))
                    .setTitle("R-Login Update")
                    .setDescription("Downloading latest release")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationUri(Uri.fromFile(destinationFile))
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager != null) {
                long downloadId = manager.enqueue(request);

                BroadcastReceiver onComplete = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context c, Intent intent) {
                        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                        if (id == downloadId) {
                            try {
                                c.unregisterReceiver(this);
                            } catch (Exception ignored) {}
                            installApk(c, destinationFile);
                        }
                    }
                };

                ContextCompat.registerReceiver(
                        context,
                        onComplete,
                        new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                        ContextCompat.RECEIVER_NOT_EXPORTED
                );
            }
        } catch (Exception e) {
            Toast.makeText(context, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) return;

        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }

        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(install);
        } catch (Exception e) {
            Toast.makeText(context, "Unable to launch installer: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
