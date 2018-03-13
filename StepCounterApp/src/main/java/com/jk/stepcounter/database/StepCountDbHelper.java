package com.jk.stepcounter.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 17652 on 2018/1/29.
 */

public class StepCountDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "StepCountDbHelper";

    private static final String DATABASE_NAME = "StepCount.db";

    /**
     * 建表语句
     */
    private static final String SQL_CREATE_PETS_TABLE =
            "CREATE TABLE " + StepCountContract.StepData.TABLE_NAME
            + " (" + StepCountContract.StepData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + StepCountContract.StepData.COLUMN_NAME_DATE + " TEXT UNIQUE NOT NULL, "
            + StepCountContract.StepData.COLUMN_NAME_TIMESTAMP + " TEXT NOT NULL, "
            + StepCountContract.StepData.WRITE_DATABASE_TIMESTAMP + " TEXT NOT NULL,"
            + StepCountContract.StepData.COLUMN_NAME_TODAY_FINAL_STEP_COUNT + " TEXT "
            + ");";
    /**
     * 删除表语句
     */
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + StepCountContract.StepData.TABLE_NAME;

    private static final int DATABASE_VERSION = 1;

    private static volatile StepCountDbHelper INSTANCE = null;

    // Private constructor suppresses
    // default public constructor
    private StepCountDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //thread safe and performance  promote
    public static  StepCountDbHelper getInstance(Context context) {
        if(INSTANCE == null){
            synchronized(StepCountDbHelper.class){
                //when more than two threads run into the first null check same time, to avoid instanced more than one time, it needs to be checked again.
                if(INSTANCE == null){
                    INSTANCE = new StepCountDbHelper(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);

    }
}
