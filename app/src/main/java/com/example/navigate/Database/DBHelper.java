package com.example.navigate.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class DBHelper extends SQLiteOpenHelper
{
    // Variables
    private static final String DB_NAME = "HistDB";
    private static final int DB_VERSION = 1;

    // Connection between DBHelper and DB
    public DBHelper(Context context)
    {
        super(context, DB_NAME, null ,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // Initialize variables
        String tblHist = "CREATE TABLE HIST(ID text,DATE text,TRANS text,START text,DEST text)";
        db.execSQL(tblHist);
    }

    // Inserting data to DB
    public boolean insertHist(String id, String date, String trans, LatLng start, LatLng dest)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("ID",id);
        contentValues.put("DATE",date);
        contentValues.put("TRANS",trans);
        contentValues.put("START",start.getLatitude()+","+start.getLongitude());
        contentValues.put("DEST",dest.getLatitude()+","+dest.getLongitude());
        db.insert("HIST",null,contentValues);
        db.close();

        return true;
    }

    // Delete history where ID's are the same
    public boolean deleteHist(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("HIST", "ID=?", new String[]{id});

        return true;
    }

    // Pull route history from DB
    public Cursor getHistory(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select ID, DATE, TRANS, START, DEST from HIST where ID=? order by date desc", new String[] {id});

        return cursor;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        String tblHist = "DROP TABLE IF EXISTS HIST";
        db.execSQL(tblHist);
    }
}
