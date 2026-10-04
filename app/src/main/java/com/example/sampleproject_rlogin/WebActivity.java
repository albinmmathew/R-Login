package com.example.sampleproject_rlogin;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.appbar.MaterialToolbar;

public class WebActivity extends AppCompatActivity {
    private boolean autoLoggedIn = false;
    private WebView wv;
    private String currentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDark);
        }

        setContentView(R.layout.activity_web);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String site = getIntent().getStringExtra("site");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(site != null ? site : "Browser");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (wv != null && wv.canGoBack()) {
                    wv.goBack();
                } else {
                    finish();
                }
            }
        });

        wv = findViewById(R.id.web_view);
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);

        currentUrl = getIntent().getStringExtra("url");
        String user = getIntent().getStringExtra("user");
        String pass = getIntent().getStringExtra("pass");

        String u = user != null ? user.replace("'", "\\'").replace("\"", "\\\"") : "";
        String p = pass != null ? pass.replace("'", "\\'").replace("\"", "\\\"") : "";

        String js = "javascript:(function(){" +
                "var u=document.querySelector('#user_username, input[name=\"user[username]\"], input[name=\"username\"], #username, input[type=\"text\"]');" +
                "var p=document.querySelector('#user_password, input[name=\"user[password]\"], input[name=\"password\"], #password, input[type=\"password\"]');" +
                "var s=document.querySelector('input[type=\"submit\"], button[type=\"submit\"], input[name=\"commit\"], #loginbtn');" +
                "if(u && p && s){" +
                "  u.value='" + u + "';" +
                "  p.value='" + p + "';" +
                "  s.click();" +
                "}" +
                "})();";

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String loadedUrl) {
                super.onPageFinished(view, loadedUrl);
                if (getSupportActionBar() != null && view.getTitle() != null && !view.getTitle().isEmpty()) {
                    getSupportActionBar().setSubtitle(view.getTitle());
                }
                if (!autoLoggedIn) {
                    autoLoggedIn = true;
                    view.evaluateJavascript(js, null);
                }
            }
        });

        if (currentUrl != null) {
            wv.loadUrl(currentUrl);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (wv != null && wv.canGoBack()) {
                wv.goBack();
            } else {
                finish();
            }
            return true;
        }
        if (item.getItemId() == R.id.action_open_browser) {
            String urlToOpen = wv != null && wv.getUrl() != null ? wv.getUrl() : currentUrl;
            if (urlToOpen != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlToOpen));
                startActivity(browserIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
