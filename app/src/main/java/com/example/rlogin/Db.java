package com.example.rlogin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Db extends SQLiteOpenHelper {
    public Db(Context context) {
        super(context, "app.db", null, 4);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE acc (site TEXT PRIMARY KEY, user TEXT, pass TEXT)");
        createAssignmentTables(db);
        createReleaseCacheTable(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 2) {
            createAssignmentTables(db);
        }
        if (oldV < 3) {
            try {
                db.execSQL("ALTER TABLE assignment_status ADD COLUMN due_timestamp INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }
        if (oldV < 4) {
            createReleaseCacheTable(db);
        }
    }

    private void createAssignmentTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS assignment_status (" +
                "id TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "course TEXT, " +
                "status TEXT, " +
                "due_timestamp INTEGER DEFAULT 0, " +
                "completed_at INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS assignment_cache (" +
                "id INTEGER PRIMARY KEY CHECK (id = 1), " +
                "json_data TEXT, " +
                "last_synced INTEGER)");
    }

    private void createReleaseCacheTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS release_cache (" +
                "slot TEXT PRIMARY KEY, " +
                "version_tag TEXT NOT NULL, " +
                "changelog TEXT, " +
                "apk_url TEXT, " +
                "fetched_at INTEGER)");
    }
    
    public void save(String site, String user, String pass) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("site", site);
        cv.put("user", user);
        cv.put("pass", pass);
        db.replace("acc", null, cv);
    }
    
    public String[] get(String site) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT user, pass FROM acc WHERE site=?", new String[]{site});
        if (c.moveToFirst()) {
            String[] res = new String[]{c.getString(0), c.getString(1)};
            c.close();
            return res;
        }
        c.close();
        return null;
    }

    public void markAssignmentClosed(String id, String title, String course, String status, long dueTimestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        cv.put("title", title);
        cv.put("course", course);
        cv.put("status", status != null ? status : "MANUAL_CLOSED");
        cv.put("due_timestamp", dueTimestamp);
        cv.put("completed_at", System.currentTimeMillis());
        db.replace("assignment_status", null, cv);
    }

    public void markAssignmentAutoClosed(String id, String title, String course, long dueTimestamp) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM assignment_status WHERE id=?", new String[]{id});
        boolean exists = (c != null && c.moveToFirst());
        if (c != null) c.close();
        if (!exists) {
            ContentValues cv = new ContentValues();
            cv.put("id", id);
            cv.put("title", title);
            cv.put("course", course);
            cv.put("status", "AUTO_CLOSED");
            cv.put("due_timestamp", dueTimestamp);
            cv.put("completed_at", System.currentTimeMillis());
            db.insert("assignment_status", null, cv);
        }
    }

    public void removeIfAutoClosed(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("assignment_status", "id=? AND status='AUTO_CLOSED'", new String[]{id});
    }

    public void cleanupOldCompletedAssignments() {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        long oneFortyDaysMillis = 140L * 24L * 3600L * 1000L;
        long cutoff = now - oneFortyDaysMillis;
        // Purge completed assignments whose completion time is older than 140 days (full semester lifespan):
        db.delete("assignment_status", "(completed_at > 0 AND completed_at < ?) OR (completed_at = 0 AND due_timestamp > 0 AND (due_timestamp * 1000) < ?)", new String[]{String.valueOf(cutoff), String.valueOf(cutoff)});
    }

    public void unmarkAssignmentClosed(String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("assignment_status", "id=?", new String[]{id});
    }

    public java.util.Set<String> getClosedAssignmentIds() {
        java.util.Set<String> set = new java.util.HashSet<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM assignment_status", null);
        if (c != null) {
            while (c.moveToNext()) {
                set.add(c.getString(0));
            }
            c.close();
        }
        return set;
    }

    public void saveAssignmentCache(String json) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", 1);
        cv.put("json_data", json);
        cv.put("last_synced", System.currentTimeMillis());
        db.replace("assignment_cache", null, cv);
    }

    public String getAssignmentCache() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT json_data FROM assignment_cache WHERE id = 1", null);
        if (c != null) {
            if (c.moveToFirst()) {
                String data = c.getString(0);
                c.close();
                return data;
            }
            c.close();
        }
        return null;
    }

    public void saveReleaseCache(String slot, String versionTag, String changelog, String apkUrl) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("slot", slot);
        cv.put("version_tag", versionTag);
        cv.put("changelog", changelog);
        cv.put("apk_url", apkUrl);
        cv.put("fetched_at", System.currentTimeMillis());
        db.replace("release_cache", null, cv);
    }

    public String[] getReleaseCache(String slot) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT version_tag, changelog, apk_url FROM release_cache WHERE slot = ?", new String[]{slot});
        if (c != null) {
            if (c.moveToFirst()) {
                String[] res = new String[]{c.getString(0), c.getString(1), c.getString(2)};
                c.close();
                return res;
            }
            c.close();
        }
        return null;
    }

    public void clearPendingRelease() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("release_cache", "slot = 'PENDING'", null);
    }

    public void syncInstalledRelease(String installedVersionName, String defaultChangelog) {
        SQLiteDatabase db = getWritableDatabase();
        String normalizedVersion = installedVersionName.startsWith("v") ? installedVersionName : "v" + installedVersionName;

        // Check if a PENDING release matches what was just installed
        Cursor cPending = db.rawQuery("SELECT version_tag, changelog, apk_url FROM release_cache WHERE slot = 'PENDING'", null);
        if (cPending != null) {
            if (cPending.moveToFirst()) {
                String pendingTag = cPending.getString(0);
                String pendingChangelog = cPending.getString(1);
                String pendingApkUrl = cPending.getString(2);
                if (pendingTag != null && (pendingTag.equalsIgnoreCase(normalizedVersion) || pendingTag.replace("v", "").equalsIgnoreCase(installedVersionName))) {
                    // User installed the pending update! Promote to CURRENT and clear PENDING
                    saveReleaseCache("CURRENT", pendingTag, pendingChangelog, pendingApkUrl);
                    clearPendingRelease();
                    cPending.close();
                    return;
                }
            }
            cPending.close();
        }

        // If CURRENT is missing or reflects an older version, update CURRENT
        Cursor cCurrent = db.rawQuery("SELECT version_tag FROM release_cache WHERE slot = 'CURRENT'", null);
        boolean currentMatches = false;
        if (cCurrent != null) {
            if (cCurrent.moveToFirst()) {
                String currentTag = cCurrent.getString(0);
                if (currentTag != null && (currentTag.equalsIgnoreCase(normalizedVersion) || currentTag.replace("v", "").equalsIgnoreCase(installedVersionName))) {
                    currentMatches = true;
                }
            }
            cCurrent.close();
        }

        if (!currentMatches) {
            saveReleaseCache("CURRENT", normalizedVersion, defaultChangelog, null);
            clearPendingRelease();
        }
    }
}
