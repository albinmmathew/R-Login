package com.example.sampleproject_rlogin;

import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.appbar.MaterialToolbar;

public class EditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDark);
        }

        setContentView(R.layout.activity_edit);

        String site = getIntent().getStringExtra("site");
        String displayName = site != null ? site : "Service";

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Setup " + displayName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView title = findViewById(R.id.title);
        if (title != null) {
            title.setText("Enter " + displayName + " Credentials");
        }

        EditText uBox = findViewById(R.id.u_box);
        EditText pBox = findViewById(R.id.p_box);
        Button sBtn = findViewById(R.id.s_btn);

        Db db = new Db(this);
        String[] saved = db.get(displayName);
        if (saved != null) {
            uBox.setText(saved[0]);
            pBox.setText(Crypt.dec(saved[1]));
        }

        sBtn.setOnClickListener(v -> {
            String u = uBox.getText().toString().trim();
            String p = pBox.getText().toString();
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Username and Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            db.save(displayName, u, Crypt.enc(p));
            Toast.makeText(this, displayName + " credentials saved securely!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
