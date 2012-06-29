package com.yozio.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class YozioDataStoreImpl implements YozioDataStore {

  private static final String LOGTAG = "YozioDataStoreImpl";
  
  static final int DATABASE_VERSION = 1;
  static final String DATABASE_NAME = "yozio";
  
  static final String EVENTS_TABLE = "events";
  static final String APP_KEY = "app_key";
  static final String DATA = "data";
  
  /**
   * Handles the creation and versioning of the Yozio database.
   */
  static class DatabaseHelper extends SQLiteOpenHelper {
    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(
          "CREATE TABLE " + EVENTS_TABLE + " (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
              APP_KEY + " STRING NOT NULL," + DATA + " STRING NOT NULL)");
      db.execSQL(
          "CREATE INDEX IF NOT EXISTS app_key_idx ON " + EVENTS_TABLE + " (" + APP_KEY + ")");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // The user has upgraded the app. Reinitialize the database.
      db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE);
      onCreate(db);
    }
  }
  
  
  private final SQLiteOpenHelper dbHelper;
  private final String appKey;
  
  YozioDataStoreImpl(SQLiteOpenHelper dbHelper, String appKey) {
    this.dbHelper = dbHelper;
    this.appKey = appKey;
  }
  
  public boolean addEvent(JSONObject event) {
    synchronized (this) {
      try {
        ContentValues cv = new ContentValues();
        cv.put(APP_KEY, appKey);
        cv.put(DATA, event.toString());
        dbHelper.getWritableDatabase().insert(EVENTS_TABLE, null, cv);
        // Don't worry, finally will still be called.
        return true;
      } catch (SQLiteException e) {
        Log.e(LOGTAG, "addEvent", e);
      } finally {
        dbHelper.close();
      }
    }
    return false;
  }
  
  public int getNumEvents() {
    synchronized (this) {
      int count = -1;
      try {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT COUNT(*) FROM " + EVENTS_TABLE + where(), null);
        cursor.moveToFirst();
        count = cursor.getInt(0);
        cursor.close();
      } catch (SQLiteException e) {
        Log.e(LOGTAG, "getNumEvents", e);
      } finally {
        dbHelper.close();
      }
      return count;
    }
  }

  public Events getEvents(int limit) {
    synchronized (this) {
      JSONArray jsonArray = null;
      String lastEventId = null;
      try {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
            "SELECT _id, " + DATA + " FROM "+ EVENTS_TABLE + where() +
            " ORDER BY _id ASC LIMIT " + limit, null);
        jsonArray = new JSONArray();
        while (cursor.moveToNext()) {
          if (cursor.isLast()) {
            lastEventId = cursor.getString(0);
          }
          try {
            jsonArray.put(new JSONObject(cursor.getString(1)));
          } catch (JSONException e) {
            // Ignore event.
          }
        }
        cursor.close();
      } catch (SQLException e) {
        Log.e(LOGTAG, "getEvents", e);
      } finally {
        dbHelper.close();
      }
      if (jsonArray != null && lastEventId != null) {
        return new Events(jsonArray, lastEventId);
      } else {
        return null;
      }
    }
  }

  public boolean removeEvents(String lastEventId) {
    synchronized (this) {
      try {
        dbHelper.getWritableDatabase().delete(EVENTS_TABLE,
            "_id <= " + lastEventId + " AND " + APP_KEY + " = '" + appKey + "'", null);
        // Don't worry, finally will still be called.
        return true;
      } catch (SQLException e) {
        Log.e(LOGTAG, "removeEvents", e);
      } finally {
        dbHelper.close();
      }
      return false;
    }
  }
  
  private String where() {
    return " WHERE " + APP_KEY + " = '" + appKey + "'";
  }
}
