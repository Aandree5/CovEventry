package com.example.myapplication;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static android.content.ContentValues.TAG;


public class Databasehelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "event.db";
    public static final String TABLE_NAME = "event";
    public static final String COL_1 = "name";
    public static final String COL_2 = "image";
    public static final String COL_3 = "venuename";
    public static final String COL_4 ="postcode";
    public static final String COL_5 = "time";
    public static final String COL_6 = "price";
    public static final String COL_7 ="description";
    public Databasehelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(name TEXT,venuemane TEXT, postcode TEXT, time TEXT,price TEXT,description TEXT)";
        db.execSQL(createTable);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP IF TABLE EXISTS " +TABLE_NAME);
        onCreate(db);

    }

    public boolean addData(String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        ContentValues.put(COL_2,item);

        Log.d(TAG ,"addData:Adding " + item + " to " + TABLE_NAME);

        long result =db.insert(TABLE_NAME, null, contentValues);

        if (result == -1) {
            return false;
        }else{
            return true;
        }
    }
    //return data from database
    public Cursor GetData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data;
        data = db.rawQuery(query, null);
        return data;
    }

    public Cursor getItemID(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " +COL_1 + " FROM " + TABLE_NAME +
                "WHERE " + COL_2 + " = " + name;
        Cursor data = db.rawQuery(query, null);
        return data;

    }

    public void updataName(String newName, int id, String oldName){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE" + TABLE_NAME + " SET " + COL_2 +
                " = " + newName + " WHERE "
                + COL_1 + " = " + id + "and" + COL_2 +   " = " + oldName);

        db.execSQL(query);

    }


    public void deleteName(int id, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM" + TABLE_NAME + " WHERE " + COL_1 +
                " = '" + id + "'" + " AND " + COL_2 + " = '" + name +  "'";
        Log.d(TAG,"deleteName:quert: " + query);
        Log.d(TAG,"deletaName:Deleting" + name + "from database");
        db.execSQL(query);

    }

}
