package com.example.rlogin;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;

public class SettingsActivity extends AppCompatActivity {

    private MaterialCardView cardDefault;
    private MaterialCardView cardCustom;
    private TextView txtStatusDefault;
    private TextView txtStatusCustom;
    private MaterialButtonToggleGroup toggleNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDark);
        }

        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> finish());
            if (ThemeManager.isBrutalist(this)) {
                toolbar.setTitleTextColor(0xFF1A1616);
                if (toolbar.getNavigationIcon() != null) {
                    toolbar.getNavigationIcon().setTint(0xFF1A1616);
                }
            }
        }

        setupThemeCards();
        setupNightModeToggle();
        setupUpdateAndAboutRows();
    }

    private void setupThemeCards() {
        cardDefault = findViewById(R.id.card_theme_default);
        cardCustom = findViewById(R.id.card_theme_custom);
        txtStatusDefault = findViewById(R.id.txt_status_default);
        txtStatusCustom = findViewById(R.id.txt_status_custom);

        updateCardStates(ThemeManager.getSelectedTheme(this));

        cardDefault.setOnClickListener(v -> {
            if (ThemeManager.isBrutalist(this)) {
                ThemeManager.setSelectedTheme(this, ThemeManager.THEME_DEFAULT);
                recreate();
            }
        });

        cardCustom.setOnClickListener(v -> {
            if (!ThemeManager.isBrutalist(this)) {
                ThemeManager.setSelectedTheme(this, ThemeManager.THEME_BRUTALIST);
                recreate();
            }
        });
    }

    private void updateCardStates(String activeTheme) {
        boolean isBrutalist = ThemeManager.isBrutalist(this);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;
        int defaultStrokeColor = 0x33888888;
        int activeStrokeWidth = (int) (2 * getResources().getDisplayMetrics().density);
        int inactiveStrokeWidth = (int) (1 * getResources().getDisplayMetrics().density);

        if (!isBrutalist) {
            cardDefault.setStrokeColor(primaryColor);
            cardDefault.setStrokeWidth(activeStrokeWidth);
            txtStatusDefault.setText("Active (Default)");
            txtStatusDefault.setTextColor(0xFF4CAF50);

            cardCustom.setStrokeColor(defaultStrokeColor);
            cardCustom.setStrokeWidth(inactiveStrokeWidth);
            txtStatusCustom.setText("Tap to select");
            txtStatusCustom.setTextColor(0xFF888888);
        } else {
            cardCustom.setStrokeColor(primaryColor);
            cardCustom.setStrokeWidth(activeStrokeWidth);
            txtStatusCustom.setText("Active");
            txtStatusCustom.setTextColor(0xFF11A677);

            cardDefault.setStrokeColor(defaultStrokeColor);
            cardDefault.setStrokeWidth(inactiveStrokeWidth);
            txtStatusDefault.setText("Tap to select");
            txtStatusDefault.setTextColor(0xFF888888);
        }
    }

    private void setupNightModeToggle() {
        toggleNightMode = findViewById(R.id.toggle_night_mode);
        int currentMode = ThemeManager.getNightMode(this);

        if (currentMode == ThemeManager.MODE_LIGHT) {
            toggleNightMode.check(R.id.btn_mode_light);
        } else if (currentMode == ThemeManager.MODE_DARK) {
            toggleNightMode.check(R.id.btn_mode_dark);
        } else {
            toggleNightMode.check(R.id.btn_mode_system);
        }

        toggleNightMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            int newMode;
            if (checkedId == R.id.btn_mode_light) {
                newMode = ThemeManager.MODE_LIGHT;
            } else if (checkedId == R.id.btn_mode_dark) {
                newMode = ThemeManager.MODE_DARK;
            } else {
                newMode = ThemeManager.MODE_SYSTEM;
            }

            if (newMode != ThemeManager.getNightMode(this)) {
                ThemeManager.setNightMode(this, newMode);
            }
        });
    }

    private void setupUpdateAndAboutRows() {
        TextView txtWhatsNewTitle = findViewById(R.id.txt_whats_new_title);
        if (txtWhatsNewTitle != null) {
            txtWhatsNewTitle.setText("What's New in v" + BuildConfig.VERSION_NAME);
        }
        TextView txtInstalledVersion = findViewById(R.id.txt_installed_version);
        if (txtInstalledVersion != null) {
            txtInstalledVersion.setText("Currently on v" + BuildConfig.VERSION_NAME);
        }

        View rowWhatsNew = findViewById(R.id.row_whats_new);
        if (rowWhatsNew != null) {
            rowWhatsNew.setOnClickListener(v -> showWhatsNewDialog());
        }

        View rowCheckUpdates = findViewById(R.id.row_check_updates);
        if (rowCheckUpdates != null) {
            rowCheckUpdates.setOnClickListener(v -> GitHubUpdater.checkForUpdates(this, true));
        }

        View rowAbout = findViewById(R.id.row_about);
        if (rowAbout != null) {
            rowAbout.setOnClickListener(v -> showAboutDialog());
        }

        View rowGithub = findViewById(R.id.row_github);
        if (rowGithub != null) {
            rowGithub.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/albinmmathew/R-Login"));
                startActivity(browserIntent);
            });
        }
    }

    public static String getFallbackMessage() {
        return "Release notes are not available offline.\n\nConnect to the internet and tap 'Check for Updates' to view the latest changelog.";
    }

    private void showWhatsNewDialog() {
        Db db = new Db(this);
        String[] currentRelease = db.getReleaseCache("CURRENT");
        String changelog = null;
        String versionTitle = "v" + BuildConfig.VERSION_NAME;

        if (currentRelease != null && currentRelease[1] != null && !currentRelease[1].trim().isEmpty()) {
            changelog = currentRelease[1];
            if (currentRelease[0] != null && !currentRelease[0].isEmpty()) {
                versionTitle = currentRelease[0];
            }
        }

        CharSequence formattedMessage = (changelog != null && !changelog.trim().isEmpty()) ?
                GitHubUpdater.formatMarkdown(changelog) :
                getFallbackMessage();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("What's New in " + versionTitle)
                .setMessage(formattedMessage)
                .setPositiveButton("Close", null)
                .setNeutralButton("View on GitHub", (d, which) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/albinmmathew/R-Login/releases/tag/v" + BuildConfig.VERSION_NAME));
                    startActivity(browserIntent);
                })
                .show();

        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setMovementMethod(android.text.method.LinkMovementMethod.getInstance());
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About R-Login")
                .setMessage(
                        "R-Login v" + BuildConfig.VERSION_NAME + "\n\n" +
                        "A lightweight, all-in-one campus portal utility built for Rajagiri College of Social Sciences.\n\n" +
                        "Key Capabilities:\n" +
                        "- Wi-Fi captive portal auto-login\n" +
                        "- Fedena portal launcher and attendance percentage calculator\n" +
                        "- Moodle LMS assignment tracker with due date management\n\n" +
                        "Academic Disclaimer:\n" +
                        "This application is an independent client and is not officially affiliated with or endorsed by Rajagiri College of Social Sciences.\n\n" +
                        "Copyright (c) 2026 Albin M Mathew. All rights reserved."
                )
                .setPositiveButton("Dismiss", null)
                .show();
    }
}
