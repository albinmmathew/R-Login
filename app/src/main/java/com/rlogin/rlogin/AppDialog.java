package com.rlogin.rlogin;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class AppDialog {

    public interface OnActionClickListener {
        void onAction();
    }

    public static void show(
            Activity activity,
            int iconRes,
            String title,
            String version,
            CharSequence message,
            String positiveButtonText,
            OnActionClickListener positiveAction
    ) {
        boolean isBrutalist = ThemeManager.isBrutalist(activity);
        int layoutId = isBrutalist ? R.layout.dialog_update : R.layout.dialog_classic;
        View v = LayoutInflater.from(activity).inflate(layoutId, null);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(v)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView imgIcon = v.findViewById(R.id.img_icon);
        if (imgIcon != null) {
            imgIcon.setImageResource(iconRes);
            if (!isBrutalist) {
                TypedValue val = new TypedValue();
                activity.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, val, true);
                imgIcon.setImageTintList(ColorStateList.valueOf(val.data));
            }
        }

        TextView txtTitle = v.findViewById(R.id.txt_dialog_title);
        if (txtTitle != null) {
            if (!isBrutalist) {
                if ("WHAT'S NEW".equalsIgnoreCase(title)) {
                    txtTitle.setText("What's New");
                } else if ("ABOUT R-LOGIN".equalsIgnoreCase(title)) {
                    txtTitle.setText("About R-Login");
                } else {
                    txtTitle.setText(title);
                }
            } else {
                txtTitle.setText(title);
            }
        }

        TextView badgeVer = v.findViewById(R.id.badge_current_version);
        if (badgeVer != null) {
            badgeVer.setText(version != null ? version : "v" + BuildConfig.VERSION_NAME);
        }

        View badgeLatest = v.findViewById(R.id.badge_latest_version);
        if (badgeLatest != null) badgeLatest.setVisibility(View.GONE);

        View progress = v.findViewById(R.id.progress_checking);
        if (progress != null) progress.setVisibility(View.GONE);

        View statusMsg = v.findViewById(R.id.txt_status_message);
        if (statusMsg != null) statusMsg.setVisibility(View.GONE);

        ScrollView scroll = v.findViewById(R.id.scroll_changelog);
        TextView txtChangelog = v.findViewById(R.id.txt_changelog);
        if (scroll != null && txtChangelog != null) {
            txtChangelog.setText(message);
            txtChangelog.setMovementMethod(LinkMovementMethod.getInstance());
            if (isBrutalist) {
                txtChangelog.setTextColor(ContextCompat.getColor(activity, R.color.neo_ink));
                txtChangelog.setLinkTextColor(ContextCompat.getColor(activity, R.color.neo_brand));
            } else {
                TypedValue val = new TypedValue();
                activity.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, val, true);
                txtChangelog.setLinkTextColor(val.data);
            }
            scroll.setVisibility(View.VISIBLE);
        }

        Button btnNeg = v.findViewById(R.id.btn_negative);
        if (btnNeg != null) {
            btnNeg.setText(isBrutalist ? "CLOSE" : "Close");
            btnNeg.setOnClickListener(view -> dialog.dismiss());
        }

        Button btnPos = v.findViewById(R.id.btn_positive);
        if (btnPos != null) {
            if (positiveButtonText != null && positiveAction != null) {
                if (isBrutalist && "VIEW ON GITHUB →".equalsIgnoreCase(positiveButtonText)) {
                    btnPos.setText("GITHUB →");
                } else if (!isBrutalist && "VIEW ON GITHUB →".equalsIgnoreCase(positiveButtonText)) {
                    btnPos.setText("View on GitHub →");
                } else {
                    btnPos.setText(positiveButtonText);
                }
                btnPos.setVisibility(View.VISIBLE);
                btnPos.setOnClickListener(view -> {
                    dialog.dismiss();
                    positiveAction.onAction();
                });
            } else {
                btnPos.setVisibility(View.GONE);
            }
        }

        // Guarantee scrollable height is capped dynamically based on screen height
        dialog.setOnShowListener(d -> {
            if (scroll != null) {
                int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
                int maxHeight = Math.max((int) (screenHeight * 0.52f), (int) (320 * activity.getResources().getDisplayMetrics().density));
                if (scroll.getHeight() > maxHeight) {
                    scroll.getLayoutParams().height = maxHeight;
                    scroll.requestLayout();
                }
            }
        });

        dialog.show();
    }
}

