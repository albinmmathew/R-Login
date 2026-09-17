package com.example.sampleproject_rlogin;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class MainActivity extends AppCompatActivity {
    Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDark);
        }

        setContentView(R.layout.activity_main);
        db = new Db(this);

        setup("WIFI", R.id.status_wifi, R.id.btn_edit_wifi, R.id.c_wifi, null);
        setup("FEDENA", R.id.status_fedena, R.id.btn_edit_fedena, R.id.c_fedena, "https://fedena.rajagiri.edu/");
        setup("LMS", R.id.status_lms, R.id.btn_edit_lms, R.id.c_lms, "https://lms.rajagiri.edu/login/index.php");

        Button rFedena = findViewById(R.id.r_fedena);
        if (rFedena != null) {
            rFedena.setOnClickListener(v -> {
                String[] saved = db.get("FEDENA");
                if (saved == null || saved[0].isEmpty() || saved[1].isEmpty()) {
                    Toast.makeText(this, "Setup FEDENA credentials first", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(this, ReportActivity.class);
                i.putExtra("user", saved[0]);
                i.putExtra("pass", Crypt.dec(saved[1]));
                startActivity(i);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus("WIFI", R.id.status_wifi);
        updateStatus("FEDENA", R.id.status_fedena);
        updateStatus("LMS", R.id.status_lms);
    }

    private void updateStatus(String site, int statusId) {
        TextView statusTxt = findViewById(statusId);
        if (statusTxt == null) return;
        String[] saved = db.get(site);
        if (saved != null && !saved[0].isEmpty() && !saved[1].isEmpty()) {
            statusTxt.setText("Configured (" + saved[0] + ")");
            statusTxt.setTextColor(0xFF4CAF50);
        } else {
            statusTxt.setText("Not Configured");
            statusTxt.setTextColor(0xFF9E9E9E);
        }
    }

    private void setup(String site, int statusId, int editBtnId, int cId, String url) {
        ImageButton editBtn = findViewById(editBtnId);
        Button cBtn = findViewById(cId);

        if (editBtn != null) {
            editBtn.setOnClickListener(v -> {
                Intent i = new Intent(this, EditActivity.class);
                i.putExtra("site", site);
                startActivity(i);
            });
        }

        if (cBtn != null) {
            cBtn.setOnClickListener(v -> {
                String[] saved = db.get(site);
                if (saved == null || saved[0].isEmpty() || saved[1].isEmpty()) {
                    Toast.makeText(this, "Setup " + site + " credentials first", Toast.LENGTH_SHORT).show();
                    return;
                }

                String u = saved[0];
                String p = Crypt.dec(saved[1]);

                if (site.equals("WIFI")) {
                    Net.loginWifi(this, u, p);
                } else {
                    Intent i = new Intent(this, WebActivity.class);
                    i.putExtra("site", site);
                    i.putExtra("url", url);
                    i.putExtra("user", u);
                    i.putExtra("pass", p);
                    startActivity(i);
                }
            });
        }
    }
}
