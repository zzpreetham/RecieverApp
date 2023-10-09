package com.royalenfield.recieverapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.royalenfield.recieverapp.liveDataModel.LocationModel;

import java.util.ArrayList;

public class LocationDBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "re_database";
    private static final int DB_VERSION = 4;
    private static final String TABLE_NAME = "location_data";
    private static final String ID_COL = "id";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String TIMESTAMP = "timestamp";


    public LocationDBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //DB creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LATITUDE + " TEXT,"
                + LONGITUDE + " TEXT,"
                + TIMESTAMP + " TEXT )";

        db.execSQL(query);
    }

    //Insertion
    public void addNewLocation(String latitude, String longitude, String timestamp) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LATITUDE, latitude);
        values.put(LONGITUDE, longitude);
        values.put(TIMESTAMP, timestamp);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    //update location
    public boolean updateLocation(String latitude, String longitude, String timestamp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LATITUDE, latitude);
        contentValues.put(LONGITUDE, longitude);
        contentValues.put(TIMESTAMP, timestamp);

        db.update(TABLE_NAME, contentValues, "ID = ?", new String[] { "1" });
        return true;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Fetching records from DB
    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return res;
    }

    public void createIfNotExists() {
        SQLiteDatabase db = this.getReadableDatabase();
        try{
            String SELECT_QUERY = "SELECT * FROM "+TABLE_NAME;
            Cursor rows = db.rawQuery(SELECT_QUERY, null);
            rows.moveToFirst();

            while(!rows.isAfterLast()){
                rows.moveToNext();
            }
        }
        catch(Exception e){
            if (e.getMessage().toString().contains("no such table")){
                onCreate(db);
            }
        }
    }
}
