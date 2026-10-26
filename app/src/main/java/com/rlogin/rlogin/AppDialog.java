package com.rlogin.rlogin;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.method.LinkMovementMethod;
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
        if (ThemeManager.isBrutalist(activity)) {
            View v = LayoutInflater.from(activity).inflate(R.layout.dialog_update, null);
            AlertDialog dialog = new AlertDialog.Builder(activity)
                    .setView(v)
                    .create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            ImageView imgIcon = v.findViewById(R.id.img_icon);
            if (imgIcon != null) imgIcon.setImageResource(iconRes);

            TextView txtTitle = v.findViewById(R.id.txt_dialog_title);
            if (txtTitle != null) txtTitle.setText(title);

            TextView badgeVer = v.findViewById(R.id.badge_current_version);
            if (badgeVer != null) {
                badgeVer.setText(version != null ? version : "v" + BuildConfig.VERSION_NAME);
            }

            v.findViewById(R.id.badge_latest_version).setVisibility(View.GONE);
            v.findViewById(R.id.progress_checking).setVisibility(View.GONE);
            v.findViewById(R.id.txt_status_message).setVisibility(View.GONE);

            ScrollView scroll = v.findViewById(R.id.scroll_changelog);
            TextView txtChangelog = v.findViewById(R.id.txt_changelog);
            if (scroll != null && txtChangelog != null) {
                txtChangelog.setText(message);
                txtChangelog.setMovementMethod(LinkMovementMethod.getInstance());
                txtChangelog.setTextColor(ContextCompat.getColor(activity, R.color.neo_ink));
                txtChangelog.setLinkTextColor(ContextCompat.getColor(activity, R.color.neo_brand));
                scroll.setVisibility(View.VISIBLE);
            }

            Button btnNeg = v.findViewById(R.id.btn_negative);
            if (btnNeg != null) {
                btnNeg.setText("CLOSE");
                btnNeg.setOnClickListener(view -> dialog.dismiss());
            }

            Button btnPos = v.findViewById(R.id.btn_positive);
            if (btnPos != null) {
                if (positiveButtonText != null && positiveAction != null) {
                    btnPos.setText(positiveButtonText);
                    btnPos.setVisibility(View.VISIBLE);
                    btnPos.setOnClickListener(view -> {
                        dialog.dismiss();
                        positiveAction.onAction();
                    });
                } else {
                    btnPos.setVisibility(View.GONE);
                }
            }

            // Guarantee scrollable height is capped so buttons are never pushed off-screen
            dialog.setOnShowListener(d -> {
                if (scroll != null) {
                    int maxHeight = (int) (220 * activity.getResources().getDisplayMetrics().density);
                    if (scroll.getHeight() > maxHeight) {
                        scroll.getLayoutParams().height = maxHeight;
                        scroll.requestLayout();
                    }
                }
            });

            dialog.show();
            return;
        }

        // Classic / Default Theme (Material 3)
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Close", null);

        if (positiveButtonText != null && positiveAction != null) {
            builder.setNeutralButton(positiveButtonText, (d, which) -> positiveAction.onAction());
        }

        AlertDialog dialog = builder.show();
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
