package com.rlogin.rlogin;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    public static AlertDialog createThemedDialog(Context context, View view) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    public static AlertDialog createNeoDialog(Context context, View view) {
        return createThemedDialog(context, view);
    }

    public static void checkForUpdates(Context context, boolean showModalImmediately) {
        String currentVersion = BuildConfig.VERSION_NAME;

        final AlertDialog[] liveDialog = new AlertDialog[1];
        final View[] dialogView = new View[1];

        if (showModalImmediately && context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> {
                boolean isBrutalist = ThemeManager.isBrutalist(context);
                int layoutId = isBrutalist ? R.layout.dialog_update : R.layout.dialog_classic;
                View v = LayoutInflater.from(context).inflate(layoutId, null);
                dialogView[0] = v;
                liveDialog[0] = createThemedDialog(context, v);

                ImageView imgIcon = v.findViewById(R.id.img_icon);
                if (imgIcon != null) {
                    imgIcon.setImageResource(R.drawable.ic_update);
                    if (!isBrutalist) {
                        TypedValue val = new TypedValue();
                        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, val, true);
                        imgIcon.setImageTintList(ColorStateList.valueOf(val.data));
                    }
                }

                TextView title = v.findViewById(R.id.txt_dialog_title);
                TextView curVer = v.findViewById(R.id.badge_current_version);
                ProgressBar progress = v.findViewById(R.id.progress_checking);
                TextView statusMsg = v.findViewById(R.id.txt_status_message);
                Button btnNeg = v.findViewById(R.id.btn_negative);
                Button btnPos = v.findViewById(R.id.btn_positive);

                if (title != null) title.setText(isBrutalist ? "CHECK FOR UPDATES" : "Check for Updates");
                if (curVer != null) curVer.setText("v" + currentVersion);
                if (progress != null) progress.setVisibility(View.VISIBLE);
                if (statusMsg != null) {
                    statusMsg.setVisibility(View.VISIBLE);
                    statusMsg.setText(isBrutalist ? "CHECKING GITHUB RELEASES..." : "Checking GitHub releases...");
                }
                if (btnNeg != null) {
                    btnNeg.setText(isBrutalist ? "CANCEL" : "Cancel");
                    btnNeg.setOnClickListener(view -> liveDialog[0].dismiss());
                }
                if (btnPos != null) btnPos.setVisibility(View.GONE);

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
                                if (liveDialog[0] != null && liveDialog[0].isShowing() && dialogView[0] != null) {
                                    populateUpdateDialog(context, dialogView[0], liveDialog[0], tagName, body, finalApkUrl);
                                } else {
                                    showUpdateDialog(context, tagName, body, finalApkUrl, currentVersion);
                                }
                            });
                        }
                    } else {
                        // User is on the latest version or beyond!
                        db.saveReleaseCache("CURRENT", tagName, body, finalApkUrl);
                        db.clearPendingRelease();
                        if (showModalImmediately && context instanceof Activity) {
                            ((Activity) context).runOnUiThread(() -> {
                                boolean isBrutalist = ThemeManager.isBrutalist(context);
                                if (dialogView[0] != null) {
                                    View v = dialogView[0];
                                    ProgressBar progress = v.findViewById(R.id.progress_checking);
                                    if (progress != null) progress.setVisibility(View.GONE);

                                    TextView statusMsg = v.findViewById(R.id.txt_status_message);
                                    if (statusMsg != null) {
                                        statusMsg.setVisibility(View.VISIBLE);
                                        statusMsg.setText(isBrutalist ?
                                                "YOU ARE ON THE LATEST VERSION!\nv" + currentVersion + " is up to date." :
                                                "You are on the latest version!\nv" + currentVersion + " is up to date.");
                                    }

                                    Button btnNeg = v.findViewById(R.id.btn_negative);
                                    if (btnNeg != null) btnNeg.setText(isBrutalist ? "CLOSE" : "Close");

                                    View btnPos = v.findViewById(R.id.btn_positive);
                                    if (btnPos != null) btnPos.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                } else if (showModalImmediately) {
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            boolean isBrutalist = ThemeManager.isBrutalist(context);
                            if (dialogView[0] != null) {
                                View v = dialogView[0];
                                ProgressBar progress = v.findViewById(R.id.progress_checking);
                                if (progress != null) progress.setVisibility(View.GONE);

                                TextView statusMsg = v.findViewById(R.id.txt_status_message);
                                if (statusMsg != null) {
                                    statusMsg.setVisibility(View.VISIBLE);
                                    statusMsg.setText(isBrutalist ?
                                            "COULD NOT REACH GITHUB.\nPlease try again later." :
                                            "Could not reach GitHub.\nPlease check your connection and try again.");
                                }

                                Button btnNeg = v.findViewById(R.id.btn_negative);
                                if (btnNeg != null) btnNeg.setText(isBrutalist ? "CLOSE" : "Close");

                                View btnPos = v.findViewById(R.id.btn_positive);
                                if (btnPos != null) btnPos.setVisibility(View.GONE);
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
                        boolean isBrutalist = ThemeManager.isBrutalist(context);
                        if (dialogView[0] != null) {
                            View v = dialogView[0];
                            ProgressBar progress = v.findViewById(R.id.progress_checking);
                            if (progress != null) progress.setVisibility(View.GONE);

                            TextView statusMsg = v.findViewById(R.id.txt_status_message);
                            if (statusMsg != null) {
                                statusMsg.setVisibility(View.VISIBLE);
                                statusMsg.setText(isBrutalist ?
                                        "UPDATE CHECK FAILED\n" + e.getLocalizedMessage() :
                                        "Update check failed:\n" + e.getLocalizedMessage());
                            }

                            Button btnNeg = v.findViewById(R.id.btn_negative);
                            if (btnNeg != null) btnNeg.setText(isBrutalist ? "CLOSE" : "Close");

                            View btnPos = v.findViewById(R.id.btn_positive);
                            if (btnPos != null) btnPos.setVisibility(View.GONE);
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
        boolean isBrutalist = ThemeManager.isBrutalist(context);

        ProgressBar progress = v.findViewById(R.id.progress_checking);
        if (progress != null) progress.setVisibility(View.GONE);

        TextView title = v.findViewById(R.id.txt_dialog_title);
        if (title != null) title.setText(isBrutalist ? "UPDATE AVAILABLE" : "Update Available");

        TextView latestVer = v.findViewById(R.id.badge_latest_version);
        if (latestVer != null) {
            latestVer.setText(tagName);
            latestVer.setVisibility(View.VISIBLE);
        }

        TextView statusMsg = v.findViewById(R.id.txt_status_message);
        if (statusMsg != null) {
            statusMsg.setVisibility(View.VISIBLE);
            statusMsg.setText(isBrutalist ? "NEW VERSION IS READY!" : "A new version of R-Login is ready!");
        }

        ScrollView scroll = v.findViewById(R.id.scroll_changelog);
        TextView txtChangelog = v.findViewById(R.id.txt_changelog);
        if (!changelog.isEmpty() && scroll != null && txtChangelog != null) {
            txtChangelog.setText(formatMarkdown(changelog));
            txtChangelog.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
            if (isBrutalist) {
                txtChangelog.setTextColor(ContextCompat.getColor(context, R.color.neo_ink));
                txtChangelog.setLinkTextColor(ContextCompat.getColor(context, R.color.neo_brand));
            } else {
                TypedValue val = new TypedValue();
                context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, val, true);
                txtChangelog.setLinkTextColor(val.data);
            }
            scroll.setVisibility(View.VISIBLE);
        }

        Button btnNeg = v.findViewById(R.id.btn_negative);
        if (btnNeg != null) {
            btnNeg.setText(isBrutalist ? "LATER" : "Later");
            btnNeg.setOnClickListener(view -> dialog.dismiss());
        }

        Button btnPos = v.findViewById(R.id.btn_positive);
        if (btnPos != null) {
            btnPos.setText(isBrutalist ? "DOWNLOAD & INSTALL →" : "Download & Install →");
            btnPos.setVisibility(View.VISIBLE);
            btnPos.setOnClickListener(view -> {
                dialog.dismiss();
                startApkDownload(context, apkUrl);
            });
        }

        dialog.setOnShowListener(d -> {
            if (scroll != null && context instanceof Activity) {
                int screenHeight = ((Activity) context).getResources().getDisplayMetrics().heightPixels;
                int maxHeight = Math.max((int) (screenHeight * 0.52f), (int) (320 * context.getResources().getDisplayMetrics().density));
                if (scroll.getHeight() > maxHeight) {
                    scroll.getLayoutParams().height = maxHeight;
                    scroll.requestLayout();
                }
            }
        });
    }

    private static void showUpdateDialog(Context context, String tagName, String changelog, String apkUrl, String currentVersion) {
        boolean isBrutalist = ThemeManager.isBrutalist(context);
        int layoutId = isBrutalist ? R.layout.dialog_update : R.layout.dialog_classic;
        View v = LayoutInflater.from(context).inflate(layoutId, null);
        AlertDialog dialog = createThemedDialog(context, v);

        ImageView imgIcon = v.findViewById(R.id.img_icon);
        if (imgIcon != null) {
            imgIcon.setImageResource(R.drawable.ic_update);
            if (!isBrutalist) {
                TypedValue val = new TypedValue();
                context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, val, true);
                imgIcon.setImageTintList(ColorStateList.valueOf(val.data));
            }
        }

        TextView curVer = v.findViewById(R.id.badge_current_version);
        if (curVer != null) curVer.setText("v" + currentVersion);

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
                html.append("<br><b>").append(trimmed.substring(4)).append("</b><br>");
            } else if (trimmed.startsWith("## ")) {
                html.append("<br><b><big>").append(trimmed.substring(3)).append("</big></b><br>");
            } else if (trimmed.startsWith("# ")) {
                html.append("<b><big><big>").append(trimmed.substring(2)).append("</big></big></b><br>");
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                html.append("&nbsp;&nbsp;•&nbsp;").append(trimmed.substring(2)).append("<br>");
            } else if (!trimmed.isEmpty()) {
                html.append(trimmed).append("<br>");
            } else {
                html.append("<br>");
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
                boolean isBrutalist = ThemeManager.isBrutalist(context);
                int layoutId = isBrutalist ? R.layout.dialog_update : R.layout.dialog_classic;
                View v = LayoutInflater.from(context).inflate(layoutId, null);
                AlertDialog dialog = createThemedDialog(context, v);

                ImageView imgIcon = v.findViewById(R.id.img_icon);
                if (imgIcon != null) {
                    imgIcon.setImageResource(R.drawable.ic_lock);
                    if (!isBrutalist) {
                        TypedValue val = new TypedValue();
                        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, val, true);
                        imgIcon.setImageTintList(ColorStateList.valueOf(val.data));
                    }
                }

                TextView title = v.findViewById(R.id.txt_dialog_title);
                if (title != null) title.setText(isBrutalist ? "PERMISSION REQUIRED" : "Permission Required");

                ProgressBar progress = v.findViewById(R.id.progress_checking);
                if (progress != null) progress.setVisibility(View.GONE);

                TextView statusMsg = v.findViewById(R.id.txt_status_message);
                if (statusMsg != null) {
                    statusMsg.setVisibility(View.VISIBLE);
                    statusMsg.setText("To install the update, please enable 'Install unknown apps' for R-Login in Android Settings.");
                }

                Button btnNeg = v.findViewById(R.id.btn_negative);
                if (btnNeg != null) {
                    btnNeg.setText(isBrutalist ? "CANCEL" : "Cancel");
                    btnNeg.setOnClickListener(view -> dialog.dismiss());
                }

                Button btnPos = v.findViewById(R.id.btn_positive);
                if (btnPos != null) {
                    btnPos.setText(isBrutalist ? "OPEN SETTINGS →" : "Open Settings →");
                    btnPos.setVisibility(View.VISIBLE);
                    btnPos.setOnClickListener(view -> {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    });
                }

                dialog.show();
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

