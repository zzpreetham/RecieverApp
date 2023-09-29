package com.royalenfield.recieverapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.royalenfield.recieverapp.model.MqttDataModel;

import java.util.ArrayList;

/**
 * Created by praburaam on 22/06/16.
 */
public class MqttDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "EngineCal";
    private static final int DB_VERSION = 4;

    private static final String TABLENAME = "mqtt_table";
    private static final String ID_COL = "id";
    private static final String RAW_DTA = "raw_data";
    private static final String UPLOAD_STATUS = "upload_status";
    private static final String TIMESTAMP = "timestamp";

    private final String CREATE_TABLE = "CREATE TABLE " + TABLENAME + " ("
            + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RAW_DTA + " TEXT,"
            + UPLOAD_STATUS + " TEXT,"
            + TIMESTAMP + " TEXT )";
            ;

    public MqttDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addMqttData(String rawData, String uploadStatus, String timestamp){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            ContentValues cv = new ContentValues();
            cv.put(RAW_DTA, DatabaseUtils.sqlEscapeString(rawData));
            cv.put(UPLOAD_STATUS, uploadStatus);
            cv.put(TIMESTAMP, timestamp);
            db.insert(TABLENAME, null, cv);
        }
        catch(Exception e){
            if (e.getMessage().toString().contains("no such table")){
                onCreate(db);
            }
        }
        db.close();
    }

    public int getRowsCount() {
        String countQuery = "SELECT  * FROM " + TABLENAME + " WHERE "+ UPLOAD_STATUS + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public ArrayList<MqttDataModel> readMqttData()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursorCourses = db.rawQuery("SELECT  * FROM " + TABLENAME + " WHERE "+ UPLOAD_STATUS + " = 0", null);
        ArrayList<MqttDataModel> mqttDataModelArrayList = new ArrayList<>();
        if (cursorCourses.moveToFirst()) {
            do {
                mqttDataModelArrayList.add(new MqttDataModel(
                        cursorCourses.getString(0),
                        cursorCourses.getString(1),
                        cursorCourses.getString(2),
                        cursorCourses.getString(3)));
            } while (cursorCourses.moveToNext());
        }
        cursorCourses.close();
        return mqttDataModelArrayList;
    }


    // below is the method for updating our courses
    public void updateCourse(String rowId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UPLOAD_STATUS, "1");
        db.update(TABLENAME, values, "ID=?", new String[]{rowId});
        db.close();
    }



    public void deleteAllMqttData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String dropSQL = "DELETE FROM "+TABLENAME;
        try {
            db.execSQL(dropSQL);
            db.close();
        }  catch(Exception e){
            if (e.getMessage().toString().contains("no such table")){
                onCreate(db);
            }
        }
    }

    public void createIfNotExists() {
        SQLiteDatabase db = this.getReadableDatabase();
        try{
            String SELECT_QUERY = "SELECT * FROM "+TABLENAME;
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
