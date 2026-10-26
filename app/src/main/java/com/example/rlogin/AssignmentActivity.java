package com.example.rlogin;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssignmentActivity extends AppCompatActivity implements AssignmentAdapter.OnAssignmentActionListener {

    private static final int FILTER_PENDING = 1;
    private static final int FILTER_OVERDUE = 2;
    private static final int FILTER_COMPLETED = 3;

    private int currentFilter = FILTER_PENDING;

    private MaterialButtonToggleGroup filterToggleGroup;
    private Button btnFilterPending;
    private Button btnFilterOverdue;
    private Button btnFilterCompleted;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private AssignmentAdapter adapter;

    private LinearLayout layoutLoading;
    private ProgressBar progressBar;
    private TextView txtLoading;

    private LinearLayout layoutEmpty;
    private TextView txtEmptyTitle;
    private TextView txtEmptyDesc;

    private WebView hiddenWebView;
    private Db db;

    private String user;
    private String pass;
    private boolean autoLoggedIn = false;
    private boolean scraped = false;

    private final List<AssignmentItem> allAssignments = new ArrayList<>();

    public class Bridge {
        @JavascriptInterface
        public void onAssignmentsData(String json) {
            runOnUiThread(() -> handleAssignmentsJson(json));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        boolean isDark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(!isDark);
        }

        setContentView(ThemeManager.isBrutalist(this) ? R.layout.activity_assignment_brutalist : R.layout.activity_assignment);

        db = new Db(this);
        db.cleanupOldCompletedAssignments();
        user = getIntent().getStringExtra("user");
        pass = getIntent().getStringExtra("pass");

        initViews();
        loadCachedData();
        startFetch();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        filterToggleGroup = findViewById(R.id.filter_toggle_group);
        btnFilterPending = findViewById(R.id.btn_filter_pending);
        btnFilterOverdue = findViewById(R.id.btn_filter_overdue);
        btnFilterCompleted = findViewById(R.id.btn_filter_completed);

        filterToggleGroup.check(R.id.btn_filter_pending);
        filterToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_filter_pending) {
                currentFilter = FILTER_PENDING;
            } else if (checkedId == R.id.btn_filter_overdue) {
                currentFilter = FILTER_OVERDUE;
            } else if (checkedId == R.id.btn_filter_completed) {
                currentFilter = FILTER_COMPLETED;
            }
            applyFilter();
        });

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(this::startFetch);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        layoutLoading = findViewById(R.id.layout_loading);
        progressBar = findViewById(R.id.progress_bar);
        txtLoading = findViewById(R.id.txt_loading);

        layoutEmpty = findViewById(R.id.layout_empty);
        txtEmptyTitle = findViewById(R.id.txt_empty_title);
        txtEmptyDesc = findViewById(R.id.txt_empty_desc);

        hiddenWebView = findViewById(R.id.hidden_web_view);
        WebSettings ws = hiddenWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        hiddenWebView.addJavascriptInterface(new Bridge(), "Bridge");
    }

    private void loadCachedData() {
        String cacheJson = db.getAssignmentCache();
        if (cacheJson != null && !cacheJson.isEmpty()) {
            parseAndPopulateList(cacheJson);
        }
    }

    private void startFetch() {
        if (user == null || pass == null || user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "LMS credentials not found", Toast.LENGTH_SHORT).show();
            if (swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(false);
            return;
        }

        if (allAssignments.isEmpty()) {
            layoutLoading.setVisibility(View.VISIBLE);
            txtLoading.setText("Connecting to LMS Moodle...");
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        }

        autoLoggedIn = false;
        scraped = false;

        String u = user.replace("'", "\\'").replace("\"", "\\\"");
        String p = pass.replace("'", "\\'").replace("\"", "\\\"");

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
                "function send(items){" +
                "  if(window.Bridge && window.Bridge.onAssignmentsData){" +
                "    window.Bridge.onAssignmentsData(JSON.stringify(items));" +
                "  }" +
                "}" +
                "function parseMoodleDate(str){" +
                "  if(!str) return 0;" +
                "  var s = str.trim();" +
                "  if(s === '-' || s.toLowerCase().indexOf('no due') > -1) return 0;" +
                "  var d = Date.parse(s);" +
                "  if(!isNaN(d)) return Math.floor(d / 1000);" +
                "  var clean = s.replace(/^[A-Za-z]+,\\s*/, '').replace(/,/g, '');" +
                "  var d2 = Date.parse(clean);" +
                "  if(!isNaN(d2)) return Math.floor(d2 / 1000);" +
                "  return 0;" +
                "}" +
                "var sesskey = (window.M && window.M.cfg && window.M.cfg.sesskey) || '';" +
                "if(!sesskey){" +
                "  var input = document.querySelector('input[name=\"sesskey\"]');" +
                "  if(input) sesskey = input.value;" +
                "}" +
                "var courses = [];" +
                "var cSeen = {};" +
                "function addCourse(id, name){" +
                "  if(id && id !== '1' && id !== '0' && !cSeen[id]){" +
                "    cSeen[id] = true;" +
                "    courses.push({ id: id, fullname: name || ('Course ' + id) });" +
                "  }" +
                "}" +
                "var cSelect = document.querySelector('select.cal_courses_flt, select[name=\"course\"], select#calendar-course-filter-1');" +
                "if(cSelect){" +
                "  cSelect.querySelectorAll('option').forEach(function(o){ addCourse(o.value, o.textContent.trim()); });" +
                "}" +
                "document.querySelectorAll('a[href*=\"/course/view.php?id=\"]').forEach(function(cl){" +
                "  var m = cl.href.match(/id=(\\d+)/);" +
                "  if(m) addCourse(m[1], cl.textContent.trim());" +
                "});" +
                "var assignmentMap = {};" +
                "var from = Math.floor(Date.now() / 1000) - (14 * 86400);" +
                "var to = Math.floor(Date.now() / 1000) + (90 * 86400);" +
                "var calPromise = Promise.resolve();" +
                "if(sesskey){" +
                "  var calPayload = [{ index: 0, methodname: 'core_calendar_get_action_events_by_timesort', args: { timesortfrom: from, timesortto: to, limitnum: 100 } }];" +
                "  calPromise = fetch('/lib/ajax/service.php?sesskey=' + sesskey + '&info=core_calendar_get_action_events_by_timesort', {" +
                "    method: 'POST'," +
                "    headers: {'Content-Type': 'application/json'}," +
                "    body: JSON.stringify(calPayload)" +
                "  })" +
                "  .then(function(r){ return r.json(); })" +
                "  .then(function(res){" +
                "    if(res && res[0] && !res[0].error && res[0].data && res[0].data.events){" +
                "      var evs = res[0].data.events;" +
                "      for(var i=0; i<evs.length; i++){" +
                "        var ev = evs[i];" +
                "        var act = ev.action || {};" +
                "        var isSub = false;" +
                "        if(act.actionable === false) isSub = true;" +
                "        if(act.name && act.name.toLowerCase().indexOf('view') > -1) isSub = true;" +
                "        var crs = (ev.course && (ev.course.fullname || ev.course.shortname)) || '';" +
                "        var mUrl = act.url || ev.url || '';" +
                "        var idM = mUrl.match(/id=(\\d+)/);" +
                "        var aId = idM ? idM[1] : ('' + (ev.id || ev.instance || i));" +
                "        assignmentMap[aId] = {" +
                "          id: aId," +
                "          title: ev.activityname || ev.name || ''," +
                "          courseName: crs," +
                "          dueTimestamp: ev.timesort || 0," +
                "          formattedDueDate: ev.formattedtime || ''," +
                "          submissionUrl: mUrl," +
                "          activityType: ev.modulename || 'assign'," +
                "          isSubmitted: isSub" +
                "        };" +
                "      }" +
                "    }" +
                "  })" +
                "  .catch(function(e){});" +
                "}" +
                "var courseDiscoveryPromise = calPromise.then(function(){" +
                "  if(courses.length > 0) return Promise.resolve();" +
                "  if(!sesskey) return Promise.resolve();" +
                "  var crsPayload = [{ index: 0, methodname: 'core_course_get_enrolled_courses_by_timeline_classification', args: { offset: 0, limit: 50, classification: 'all', sort: 'fullname' } }];" +
                "  return fetch('/lib/ajax/service.php?sesskey=' + sesskey + '&info=core_course_get_enrolled_courses_by_timeline_classification', {" +
                "    method: 'POST'," +
                "    headers: {'Content-Type': 'application/json'}," +
                "    body: JSON.stringify(crsPayload)" +
                "  })" +
                "  .then(function(r){ return r.json(); })" +
                "  .then(function(res){" +
                "    if(res && res[0] && !res[0].error && res[0].data && res[0].data.courses){" +
                "      var clist = res[0].data.courses;" +
                "      for(var ci=0; ci<clist.length; ci++){" +
                "        addCourse('' + clist[ci].id, clist[ci].fullname || clist[ci].shortname);" +
                "      }" +
                "    }" +
                "  })" +
                "  .catch(function(e){});" +
                "});" +
                "courseDiscoveryPromise.then(function(){" +
                "  if(courses.length === 0){" +
                "    var list = [];" +
                "    for(var k in assignmentMap){ list.push(assignmentMap[k]); }" +
                "    send(list);" +
                "    return;" +
                "  }" +
                "  var coursePromises = courses.map(function(c){" +
                "    return fetch('/mod/assign/index.php?id=' + c.id)" +
                "      .then(function(cr){ return cr.text(); })" +
                "      .then(function(html){" +
                "        var parser = new DOMParser();" +
                "        var doc = parser.parseFromString(html, 'text/html');" +
                "        var courseName = c.fullname || '';" +
                "        var bc = doc.querySelector('.breadcrumb, ol.breadcrumb');" +
                "        if(bc){" +
                "          var bcLinks = bc.querySelectorAll('a[href*=\"/course/view.php\"]');" +
                "          if(bcLinks.length > 0){" +
                "            courseName = bcLinks[bcLinks.length - 1].textContent.trim();" +
                "          }" +
                "        }" +
                "        var rows = doc.querySelectorAll('table.generaltable tr');" +
                "        for(var r=0; r<rows.length; r++){" +
                "          var a = rows[r].querySelector('a[href*=\"/mod/assign/view.php?id=\"]');" +
                "          if(!a) continue;" +
                "          var title = a.textContent.trim();" +
                "          var link = a.href;" +
                "          var idM = link.match(/id=(\\d+)/);" +
                "          var aId = idM ? idM[1] : (title + link);" +
                "          var tds = rows[r].querySelectorAll('td');" +
                "          var dateStr = '';" +
                "          var dueTs = 0;" +
                "          var statusStr = '';" +
                "          for(var d=0; d<tds.length; d++){" +
                "            if(tds[d].contains(a)) continue;" +
                "            var cellText = tds[d].textContent.trim();" +
                "            var clow = cellText.toLowerCase();" +
                "            if(clow.indexOf('submit') > -1 || (clow.indexOf('grade') > -1 && clow.indexOf('not graded') === -1)){" +
                "              statusStr = cellText;" +
                "            } else {" +
                "              var ts = parseMoodleDate(cellText);" +
                "              if(ts > 0){" +
                "                dueTs = ts;" +
                "                dateStr = cellText;" +
                "              } else if(dueTs === 0 && (clow === '-' || clow.indexOf('no due') > -1)){" +
                "                dateStr = cellText;" +
                "              }" +
                "            }" +
                "          }" +
                "          var isSub = false;" +
                "          var sl = statusStr.toLowerCase();" +
                "          if(sl.indexOf('submitted') > -1 || sl.indexOf('graded') > -1){" +
                "            isSub = true;" +
                "          }" +
                "          if(assignmentMap[aId]){" +
                "            assignmentMap[aId].isSubmitted = isSub;" +
                "            if(courseName) assignmentMap[aId].courseName = courseName;" +
                "            if(dueTs > 0){" +
                "              assignmentMap[aId].dueTimestamp = dueTs;" +
                "              assignmentMap[aId].formattedDueDate = dateStr;" +
                "            }" +
                "          } else {" +
                "            assignmentMap[aId] = {" +
                "              id: aId," +
                "              title: title," +
                "              courseName: courseName || 'General / Course Work'," +
                "              dueTimestamp: dueTs," +
                "              formattedDueDate: dueTs > 0 ? dateStr : 'No Set Deadline'," +
                "              submissionUrl: link," +
                "              activityType: 'assign'," +
                "              isSubmitted: isSub" +
                "            };" +
                "          }" +
                "        }" +
                "      })" +
                "      .catch(function(e){});" +
                "  });" +
                "  Promise.all(coursePromises).then(function(){" +
                "    var list = [];" +
                "    for(var k in assignmentMap){ list.push(assignmentMap[k]); }" +
                "    send(list);" +
                "  });" +
                "})" +
                ".catch(function(err){" +
                "  var list = [];" +
                "  for(var k in assignmentMap){ list.push(assignmentMap[k]); }" +
                "  send(list);" +
                "});" +
                "})();";

        hiddenWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String loadedUrl) {
                super.onPageFinished(view, loadedUrl);

                if (loadedUrl != null && loadedUrl.contains("login")) {
                    if (!autoLoggedIn) {
                        autoLoggedIn = true;
                        view.evaluateJavascript(loginJs, null);
                    }
                } else if (loadedUrl != null) {
                    if (!scraped) {
                        scraped = true;
                        txtLoading.setText("Fetching upcoming assignment deadlines...");
                        view.evaluateJavascript(scrapeJs, null);
                    }
                }
            }
        });

        hiddenWebView.loadUrl("https://lms.rajagiri.edu/my/");
    }

    private void handleAssignmentsJson(String json) {
        if (swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }

        if (json == null || json.isEmpty()) {
            if (allAssignments.isEmpty()) {
                layoutLoading.setVisibility(View.GONE);
                showEmptyState("No response from Moodle", "Please verify your LMS credentials and try again.");
            }
            return;
        }

        db.saveAssignmentCache(json);
        parseAndPopulateList(json);
    }

    private void parseAndPopulateList(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            Set<String> manuallyClosedIds = db.getClosedAssignmentIds();

            allAssignments.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.optString("id");
                String title = obj.optString("title");
                String course = obj.optString("courseName");
                long dueTimestamp = obj.optLong("dueTimestamp", 0);
                String formattedDate = obj.optString("formattedDueDate");
                String url = obj.optString("submissionUrl");
                String activityType = obj.optString("activityType", "assign");
                boolean isSubmitted = obj.optBoolean("isSubmitted", false);
                boolean isManuallyClosed = manuallyClosedIds.contains(id);

                if (isSubmitted) {
                    db.markAssignmentAutoClosed(id, title, course, dueTimestamp);
                } else if (!isManuallyClosed) {
                    db.removeIfAutoClosed(id);
                }

                allAssignments.add(new AssignmentItem(
                        id, title, course, dueTimestamp, formattedDate, url, activityType, isSubmitted, isManuallyClosed
                ));
            }

            // Sort: dated items soonest first, then undated tasks
            Collections.sort(allAssignments, (a, b) -> {
                if (a.getDueTimestamp() > 0 && b.getDueTimestamp() > 0) {
                    return Long.compare(a.getDueTimestamp(), b.getDueTimestamp());
                } else if (a.getDueTimestamp() > 0) {
                    return -1;
                } else if (b.getDueTimestamp() > 0) {
                    return 1;
                } else {
                    return a.getTitle().compareToIgnoreCase(b.getTitle());
                }
            });

            layoutLoading.setVisibility(View.GONE);
            updateFilterCounts();
            applyFilter();

        } catch (Exception e) {
            if (allAssignments.isEmpty()) {
                layoutLoading.setVisibility(View.GONE);
                showEmptyState("Failed to parse assignments", e.getMessage());
            }
        }
    }

    private void updateFilterCounts() {
        int pending = 0;
        int overdue = 0;
        int completed = 0;

        long now = System.currentTimeMillis();
        long oneFortyDaysMillis = 140L * 24L * 3600L * 1000L;

        for (AssignmentItem item : allAssignments) {
            if (item.isClosed()) {
                if (item.getDueTimestamp() <= 0 || (now - item.getDueTimestamp() * 1000L) <= oneFortyDaysMillis || item.isManuallyClosed()) {
                    completed++;
                }
            } else if (item.getState() == AssignmentItem.STATE_OVERDUE) {
                overdue++;
            } else {
                pending++;
            }
        }

        btnFilterPending.setText("Pending (" + pending + ")");
        btnFilterOverdue.setText("Overdue (" + overdue + ")");
        btnFilterCompleted.setText("Completed (" + completed + ")");
    }

    private void applyFilter() {
        List<AssignmentItem> filtered = new ArrayList<>();
        long now = System.currentTimeMillis();
        long oneFortyDaysMillis = 140L * 24L * 3600L * 1000L;

        for (AssignmentItem item : allAssignments) {
            if (currentFilter == FILTER_COMPLETED) {
                if (item.isClosed()) {
                    // Retain completed assignments within 140 days (always retain manually closed tasks)
                    if (item.getDueTimestamp() > 0 && (now - item.getDueTimestamp() * 1000L) > oneFortyDaysMillis && !item.isManuallyClosed()) {
                        continue;
                    }
                    filtered.add(item);
                }
            } else if (currentFilter == FILTER_OVERDUE) {
                if (!item.isClosed() && item.getState() == AssignmentItem.STATE_OVERDUE) {
                    filtered.add(item);
                }
            } else { // FILTER_PENDING
                if (!item.isClosed() && item.getState() != AssignmentItem.STATE_OVERDUE) {
                    filtered.add(item);
                }
            }
        }

        if (filtered.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            if (currentFilter == FILTER_COMPLETED) {
                showEmptyState("No Completed Assignments", "Assignments you submit or mark as done will appear here.");
            } else if (currentFilter == FILTER_OVERDUE) {
                showEmptyState("No Overdue Assignments", "Great job! You have no missed deadlines.");
            } else {
                showEmptyState("All Caught Up!", "No pending assignments due at the moment.");
            }
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateList(filtered);
        }
    }

    private void showEmptyState(String title, String desc) {
        layoutEmpty.setVisibility(View.VISIBLE);
        txtEmptyTitle.setText(title);
        txtEmptyDesc.setText(desc);
    }

    @Override
    public void onOpenAssignment(AssignmentItem item) {
        String targetUrl = item.getSubmissionUrl();
        if (targetUrl == null || targetUrl.isEmpty()) {
            targetUrl = "https://lms.rajagiri.edu/my/";
        }

        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("site", "LMS: " + item.getTitle());
        intent.putExtra("url", targetUrl);
        intent.putExtra("user", user);
        intent.putExtra("pass", pass);
        startActivity(intent);
    }

    @Override
    public void onToggleDone(AssignmentItem item) {
        boolean willClose = !item.isClosed();
        if (willClose) {
            db.markAssignmentClosed(item.getId(), item.getTitle(), item.getCourseName(), "MANUAL_CLOSED", item.getDueTimestamp());
            item.setManuallyClosed(true);

            Snackbar.make(recyclerView, "Marked as completed", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> {
                        db.unmarkAssignmentClosed(item.getId());
                        item.setManuallyClosed(false);
                        updateFilterCounts();
                        applyFilter();
                    })
                    .show();
        } else {
            db.unmarkAssignmentClosed(item.getId());
            item.setManuallyClosed(false);
            item.setSubmitted(false);

            Snackbar.make(recyclerView, "Restored to Pending", Snackbar.LENGTH_SHORT).show();
        }

        updateFilterCounts();
        applyFilter();
    }
}
