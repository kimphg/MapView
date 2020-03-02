package com.Seamap.app.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHanlder extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SeaMap";
    private static final int DATABASE_VERSION = 1;


    public DatabaseHanlder(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String Line  =String.format("CREATE TABLE Location(   PRIMARY KEY,  TEXT, %s TEXT, %s TEXT");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
