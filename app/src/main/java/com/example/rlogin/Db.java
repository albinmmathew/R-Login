package com.example.rlogin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Db extends SQLiteOpenHelper {
    public Db(Context context) {
        super(context, "app.db", null, 3);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE acc (site TEXT PRIMARY KEY, user TEXT, pass TEXT)");
        createAssignmentTables(db);
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
}
