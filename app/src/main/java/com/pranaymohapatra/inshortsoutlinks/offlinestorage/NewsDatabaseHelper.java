package com.pranaymohapatra.inshortsoutlinks.offlinestorage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class NewsDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private final static String CREATE = "CREATE TABLE " + NewsDBContract.Schema.TABLE_NAME + " (" +
            NewsDBContract.Schema.COLUMN_NAME_ID + " INTEGER PRIMARY KEY , " +
            NewsDBContract.Schema.COLUMN_NAME_TITLE + " TEXT , " +
            NewsDBContract.Schema.COLUMN_NAME_URL + " TEXT , " +
            NewsDBContract.Schema.COLUMN_NAME_PUBLISHER + " TEXT , " +
            NewsDBContract.Schema.COLUMN_NAME_CATEGORY + " TEXT , " +
            NewsDBContract.Schema.COLUMN_NAME_HOSTNAME + " TEXT, " +
            NewsDBContract.Schema.COLUMN_NAME_TIMESTAMP + " TIMESTAMP , " +
            NewsDBContract.Schema.COLUMN_NAME_FAVORITE + " INTEGER )";

    private final static String DROP_TABLE = "DROP TABLE IF EXISTS " + NewsDBContract.Schema.TABLE_NAME;

    public NewsDatabaseHelper(Context context) {
        super(context, NewsDBContract.Schema.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

}
