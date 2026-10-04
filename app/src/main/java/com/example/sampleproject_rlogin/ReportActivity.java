package com.example.sampleproject_rlogin;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView txtLoading;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private boolean autoLoggedIn = false;

    public class Bridge {
        @JavascriptInterface
        public void onData(String json) {
            runOnUiThread(() -> parseAndDisplay(json));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDark);
        }

        setContentView(R.layout.activity_report);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progress_bar);
        txtLoading = findViewById(R.id.txt_loading);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggle_group);
        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked && adapter != null) {
                    adapter.setShowDutyLeave(checkedId == R.id.btn_with_od);
                }
            });
        }

        WebView wv = findViewById(R.id.hidden_web_view);
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);

        wv.addJavascriptInterface(new Bridge(), "Bridge");

        String user = getIntent().getStringExtra("user");
        String pass = getIntent().getStringExtra("pass");

        String u = user != null ? user.replace("'", "\\'").replace("\"", "\\\"") : "";
        String p = pass != null ? pass.replace("'", "\\'").replace("\"", "\\\"") : "";

        String loginJs = "javascript:(function(){" +
                "var u=document.querySelector('#user_username, input[name=\"user[username]\"], input[name=\"username\"], #username, input[type=\"text\"]');" +
                "var p=document.querySelector('#user_password, input[name=\"user[password]\"], input[name=\"password\"], #password, input[type=\"password\"]');" +
                "var s=document.querySelector('input[type=\"submit\"], button[type=\"submit\"], input[name=\"commit\"], #loginbtn');" +
                "if(u && p && s){" +
                "  u.value='" + u + "';" +
                "  p.value='" + p + "';" +
                "  s.click();" +
                "}" +
                "})();";

        String scrapeJs = "javascript:(function(){" +
                "var data = [];" +
                "var tables = document.querySelectorAll('table');" +
                "tables.forEach(function(table){" +
                "  var rows = table.querySelectorAll('tr');" +
                "  if(rows.length < 2) return;" +
                "  var headerRow = rows[0];" +
                "  var ths = headerRow.querySelectorAll('th, td');" +
                "  var attIdx = 2, pctIdx = 3, dutyAttIdx = 4, dutyPctIdx = 5, codeIdx = 1;" +
                "  for(var i=0; i<ths.length; i++){" +
                "    var txt = (ths[i].textContent || '').toLowerCase();" +
                "    if(txt.indexOf('total hours attended') > -1 && txt.indexOf('duty') === -1) attIdx = i;" +
                "    if(txt.indexOf('percentage') > -1 && txt.indexOf('duty') === -1) pctIdx = i;" +
                "    if(txt.indexOf('total hours attended with duty') > -1) dutyAttIdx = i;" +
                "    if(txt.indexOf('percentage with duty') > -1) dutyPctIdx = i;" +
                "    if(txt.indexOf('code') > -1) codeIdx = i;" +
                "  }" +
                "  for(var r=1; r<rows.length; r++){" +
                "    var tds = rows[r].querySelectorAll('td');" +
                "    if(tds.length >= 3){" +
                "      var name = (tds[0].textContent || '').replace(/[\\n\\r\\t]/g, ' ').replace(/\\s+/g, ' ').trim();" +
                "      var code = tds.length > codeIdx ? (tds[codeIdx].textContent || '').replace(/[\\n\\r\\t]/g, ' ').trim() : '';" +
                "      var attStr = tds.length > attIdx ? (tds[attIdx].textContent || '').trim() : '0';" +
                "      var pctStr = tds.length > pctIdx ? (tds[pctIdx].textContent || '').trim() : '';" +
                "      var att = parseFloat(attStr) || 0;" +
                "      var pct = parseFloat(pctStr.replace('%','')) || 0;" +
                "      var total = (!isNaN(att) && !isNaN(pct) && pct > 0) ? Math.round((att / pct) * 100) : 0;" +
                "      var dutyAttStr = tds.length > dutyAttIdx ? (tds[dutyAttIdx].textContent || '').trim() : attStr;" +
                "      var dutyPctStr = tds.length > dutyPctIdx ? (tds[dutyPctIdx].textContent || '').trim() : pctStr;" +
                "      var dutyAtt = parseFloat(dutyAttStr) || att;" +
                "      var dutyPct = parseFloat(dutyPctStr.replace('%','')) || pct;" +
                "      var dutyTotal = (!isNaN(dutyAtt) && !isNaN(dutyPct) && dutyPct > 0) ? Math.round((dutyAtt / dutyPct) * 100) : total;" +
                "      if(name && name.toLowerCase().indexOf('subject') === -1) {" +
                "        data.push({" +
                "          name: name," +
                "          code: code," +
                "          attended: att," +
                "          pct: pctStr," +
                "          totalHeld: total," +
                "          dutyAttended: dutyAtt," +
                "          dutyPct: dutyPctStr," +
                "          dutyTotalHeld: dutyTotal" +
                "        });" +
                "      }" +
                "    }" +
                "  }" +
                "});" +
                "if(window.Bridge && data.length > 0) {" +
                "  window.Bridge.onData(JSON.stringify(data));" +
                "}" +
                "})();";

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String loadedUrl) {
                super.onPageFinished(view, loadedUrl);

                if (loadedUrl != null && (loadedUrl.contains("login") || loadedUrl.endsWith("rajagiri.edu/"))) {
                    if (!autoLoggedIn) {
                        autoLoggedIn = true;
                        view.evaluateJavascript(loginJs, null);
                    }
                } else if (loadedUrl != null && !loadedUrl.contains("attendance_reports")) {
                    view.loadUrl("https://fedena.rajagiri.edu/attendance_reports/consolidated_attendance_report");
                }

                if (loadedUrl != null && loadedUrl.contains("attendance_reports")) {
                    view.evaluateJavascript(scrapeJs, null);
                }
            }
        });

        wv.loadUrl("https://fedena.rajagiri.edu/attendance_reports/consolidated_attendance_report");
    }

    private void parseAndDisplay(String json) {
        try {
            if (json == null || json.isEmpty() || json.equals("[]")) {
                progressBar.setVisibility(View.GONE);
                txtLoading.setText("No attendance records found or login credentials incorrect.");
                return;
            }

            Object tokener = new JSONTokener(json).nextValue();
            JSONArray arr;
            if (tokener instanceof JSONArray) {
                arr = (JSONArray) tokener;
            } else if (tokener instanceof String) {
                arr = new JSONArray((String) tokener);
            } else {
                arr = new JSONArray(json);
            }

            List<SubjectItem> list = new ArrayList<>();
            for (int idx = 0; idx < arr.length(); idx++) {
                JSONObject obj = arr.getJSONObject(idx);
                list.add(new SubjectItem(
                        obj.optString("name"),
                        obj.optString("code"),
                        obj.optDouble("attended"),
                        obj.optString("pct"),
                        obj.optInt("totalHeld"),
                        obj.optDouble("dutyAttended"),
                        obj.optString("dutyPct"),
                        obj.optInt("dutyTotalHeld")
                ));
            }

            if (!list.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                txtLoading.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new ReportAdapter(list);
                recyclerView.setAdapter(adapter);
            } else {
                progressBar.setVisibility(View.GONE);
                txtLoading.setText("No attendance records found.");
            }
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            txtLoading.setText("Parse error: " + e.getMessage());
        }
    }
}
