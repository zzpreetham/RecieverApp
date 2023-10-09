package com.royalenfield.recieverapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StatisticsDBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "re_database";
    private static final int DB_VERSION = 4;
    private static final String TABLE_NAME = "statistics_data";
    private static final String ID_COL = "id";
    private static final String ODO = "odo";
    private static final String TIMESTAMP = "timestamp";


    public StatisticsDBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //DB creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ODO + " TEXT,"
                + TIMESTAMP + " TEXT )";

        db.execSQL(query);
    }

    //Insertion
    public void addNewOdo(String odoValue, String timestamp) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ODO, odoValue);
        values.put(TIMESTAMP, timestamp);
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    //update location
    public boolean updateODO(String odoValue, String timestamp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ODO, odoValue);
        contentValues.put(TIMESTAMP, timestamp);

        db.update(TABLE_NAME, contentValues, "id = ?", new String[] { "1" });
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
        Cursor res = db.rawQuery("select * from "+TABLE_NAME +" ORDER BY "+ID_COL+" DESC LIMIT 1",null);
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
