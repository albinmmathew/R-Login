package com.rlogin.rlogin;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;

public class QuickLoginWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_WIDGET_LOGIN = "com.rlogin.rlogin.ACTION_WIDGET_LOGIN";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, "Connect", 0xFF666666);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_WIDGET_LOGIN.equals(intent.getAction())) {
            handleWidgetClick(context);
        }
    }

    private void handleWidgetClick(Context context) {
        // Step 1: Check Wi-Fi
        if (!Net.isWifiConnected(context)) {
            StatusNotifier.notifyError(context, "Wi-Fi is not connected. Please connect to campus Wi-Fi first.");
            updateAllWidgets(context, "Wi-Fi Off", 0xFFD52C36);
            resetWidgetStatusDelayed(context, 3500);
            return;
        }

        // Step 2: Check Credentials
        Db db = new Db(context);
        String[] saved = db.get("WIFI");
        if (saved == null || saved[0] == null || saved[0].trim().isEmpty() || saved[1] == null || saved[1].trim().isEmpty()) {
            StatusNotifier.notifyError(context, "No credentials saved");
            updateAllWidgets(context, "No Creds", 0xFFD52C36);
            resetWidgetStatusDelayed(context, 3500);
            return;
        }

        // Step 3: Indicate Connecting
        updateAllWidgets(context, "Connecting...", 0xFF002868);

        String user = saved[0];
        String pass = Crypt.dec(saved[1]);

        // Step 4: Login
        Net.loginWifi(context, user, pass, false, (success, message) -> {
            if (success) {
                StatusNotifier.clearNotification(context);
                StatusNotifier.vibrate(context, true); // 1-tick
                updateAllWidgets(context, "Connected!", 0xFF11A677);
                resetWidgetStatusDelayed(context, 5000);
            } else {
                String errorMsg = (message != null && !message.isEmpty()) ? message : "Could not reach gateway 172.16.0.20";
                StatusNotifier.notifyError(context, errorMsg);
                updateAllWidgets(context, "Failed", 0xFFD52C36);
                resetWidgetStatusDelayed(context, 4000);
            }
        });
    }

    private static void resetWidgetStatusDelayed(Context context, long delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            updateAllWidgets(context, "Connect", 0xFF666666);
        }, delayMillis);
    }

    private static void updateAllWidgets(Context context, String statusText, int statusColor) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, QuickLoginWidgetProvider.class);
        int[] appWidgetIds = manager.getAppWidgetIds(thisWidget);
        for (int id : appWidgetIds) {
            updateWidget(context, manager, id, statusText, statusColor);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId, String statusText, int statusColor) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_quick_login);

        views.setTextViewText(R.id.widget_title, "M-WiFi");
        views.setTextViewText(R.id.widget_status, statusText);
        views.setTextColor(R.id.widget_status, statusColor);

        Intent intent = new Intent(context, QuickLoginWidgetProvider.class);
        intent.setAction(ACTION_WIDGET_LOGIN);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, flags);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        manager.updateAppWidget(appWidgetId, views);
    }
}
