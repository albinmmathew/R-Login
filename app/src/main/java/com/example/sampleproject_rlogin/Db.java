package com.example.sampleproject_rlogin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Db extends SQLiteOpenHelper {
    public Db(Context context) {
        super(context, "app.db", null, 1);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE acc (site TEXT PRIMARY KEY, user TEXT, pass TEXT)");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
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
}
