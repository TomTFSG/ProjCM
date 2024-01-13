package com.example.projeto.misc;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FeedReaderDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "data.db";
    public static final String TABLE_NAME = "data";
    public static final String COLUMN_NAME_ATUAL = "atual";
    public static final String COLUMN_NAME_HORAS  = "horas";
    public static final String COLUMN_NAME_MINUTOS = "minutos";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_TEMP  = "temperature";
    public static final String COLUMN_NAME_HUMI = "humidity";
    public static final String COLUMN_NAME_LIGHT = "light";
    public static final String _ID = "id";

    SQLiteDatabase sql;
        public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_HORAS + " INTEGER,"+
                    COLUMN_NAME_MINUTOS + " INTEGER,"+
                    COLUMN_NAME_TIME + " TEXT," +
                    COLUMN_NAME_TEMP + " TEXT,"+
                    COLUMN_NAME_HUMI + " TEXT,"+
                    COLUMN_NAME_LIGHT + " TEXT,"+
                    COLUMN_NAME_ATUAL + " INTEGER)";

    public static final String SQL_DELETE_ENTRIES="DROP TABLE IF EXISTS " + TABLE_NAME;;
    public FeedReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sql) {
        sql.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sql, int oldVersion, int newVersion){
    }
    public void resetDatabase(){
        sql.execSQL(SQL_DELETE_ENTRIES);
        sql.execSQL(SQL_CREATE_ENTRIES);
    }


    public int getValueFromColumnName(String columnName){
        String[] projection = {columnName};

        Cursor cursor = getWritableDatabase().query(
                TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        int value = 0;

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            value = cursor.getInt(columnIndex);
            cursor.close();
        } else {
            Log.e(TAG, "Error reading value from the database");
        }

        return value;
    }


    public void setHoras(int horas, int minutos){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_HORAS, horas);
        values.put(COLUMN_NAME_MINUTOS, minutos);

        int rowsUpdated = db.update(
                FeedReaderDbHelper.TABLE_NAME,
                values,
                null,
                null
        );

        if (rowsUpdated > 0) {
            Log.i(TAG,  "Horas:minutos colocadas com sucesso");
            Log.i("HORAS","HORARIO MARCADO PARA AS: " + horas + ":" + minutos);
        } else {
            Log.e(TAG, "Erro a colocar horas:minutos");
        }
    }
}
