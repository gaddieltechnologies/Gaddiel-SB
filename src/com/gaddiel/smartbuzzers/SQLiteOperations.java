package com.gaddiel.smartbuzzers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteOperations extends SQLiteOpenHelper {
    public SQLiteOperations(Context applicationcontext) {
        super(applicationcontext, "MagicBuzzer.db", null, 1);
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE targetlocaton(locationname TEXT PRIMARY KEY, lng REAL,lat REAL, range INT)");
        database.execSQL("CREATE TABLE cancelalarm(cancelflag TEXT PRIMARY KEY)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertTargetLocation(LocationInfo locationInfo) {
        SQLiteDatabase database = getWritableDatabase();
        Log.d("SQLiteOperations.insertTargetLocation()", "Deleting any existing record");
        database.execSQL("delete from targetlocaton");
        ContentValues values = new ContentValues();
        values.put("locationname", locationInfo.locationName);
        values.put("range", Integer.valueOf(locationInfo.range));
        values.put("lng", Double.valueOf(locationInfo.lng));
        values.put("lat", Double.valueOf(locationInfo.lat));
        database.insert("targetlocaton", null, values);
        Log.d("SQLiteOperations.insertTargetLocation()", "Insert is Sucessful");
        database.close();
    }

    public void deleteTargetLocation() {
        SQLiteDatabase database = getWritableDatabase();
        Log.d("SQLiteOperations.deleteTargetLocation()", "Deleting any existing record");
        database.execSQL("delete from targetlocaton");
        Log.d("SQLiteOperations.deleteTargetLocation()", "Delete is Successful");
        database.close();
    }

    public LocationInfo getTargetLocation() {
        String locationName = "";
        double lat = 0.0d;
        double lng = 0.0d;
        int range = -1;
        SQLiteDatabase database = getWritableDatabase();
        String selectQuery = "SELECT locationname, lat ,lng , range from targetlocaton";
        Log.d("SQLiteOperations.getTargetLocation()", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count++;
                Log.d("SQLiteOperations.getTargetLocation()", "count=" + count + ", locationname=" + cursor.getString(0) + "Lat:" + cursor.getString(1) + "Lng:" + cursor.getString(2) + "Range:" + cursor.getString(3));
                locationName = cursor.getString(0);
                lat = Double.parseDouble(cursor.getString(1));
                lng = Double.parseDouble(cursor.getString(2));
                range = Integer.parseInt(cursor.getString(3));
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return new LocationInfo(locationName, lat, lng, range);
    }

    public void insertCancelAlarm() {
        SQLiteDatabase database = getWritableDatabase();
        Log.d("SQLiteOperations.insertCancelAlarm()", "Deleting any existing record");
        database.execSQL("delete from cancelalarm");
        ContentValues values = new ContentValues();
        values.put("cancelflag", "Y");
        database.insert("cancelalarm", null, values);
        Log.d("SQLiteOperations.insertCancelAlarm()", "Insert is Sucessful");
        database.close();
    }

    public boolean isCancelAlarmSet() {
        boolean isCancelAlarmSet = false;
        SQLiteDatabase database = getWritableDatabase();
        String selectQuery = "SELECT cancelflag from cancelalarm";
        Log.d("SQLiteOperations.isCancelAlarmSet()", selectQuery);
        Cursor cursor = database.rawQuery(selectQuery, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            do {
                count++;
                Log.d("SQLiteOperations.isCancelAlarmSet()", "count=" + count + ", cancelflag=" + cursor.getString(0));
                if (cursor.getString(0).toString().equals("Y")) {
                    isCancelAlarmSet = true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return isCancelAlarmSet;
    }

    public void deleteCancelAlarm() {
        SQLiteDatabase database = getWritableDatabase();
        Log.d("SQLiteOperations.deleteCancelAlarm()", "Deleting any existing record");
        database.execSQL("delete from cancelalarm");
        Log.d("SQLiteOperations.deleteCancelAlarm()", "Delete is Sucessful");
        database.close();
    }
}
