package com.royalenfield.recieverapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHandler extends SQLiteOpenHelper {
    private static final String DB_NAME = "re_database";
    private static final int DB_VERSION = 2;
    private static final String TABLE_NAME = "mqtt_data";
    private static final String ID_COL = "id";
    private static final String RAW_DTA = "raw_data";
    private static final String UPLOAD_STATUS = "upload_status";
    private static final String TIMESTAMP = "timestamp";
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RAW_DTA + " TEXT,"
                + UPLOAD_STATUS + " TEXT,"
                + TIMESTAMP + " TEXT )";

        db.execSQL(query);
    }

    public void addNewCourse(String rawData, String uploadStatus, String timestamp) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(RAW_DTA, rawData);
        values.put(UPLOAD_STATUS, uploadStatus);
        values.put(TIMESTAMP, timestamp);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}