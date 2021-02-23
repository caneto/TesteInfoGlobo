package br.teste.edglobo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    public static final String databseName = "TesteEdGlobo.db";
    public static final String tableName = "lista";

    public DBHelper(Context context) {
        super(context, databseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "CREATE TABLE " + tableName + "(id INTEGER PRIMARY KEY, post_id TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(db);
    }

    public void insertArtigo(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        db.insert(tableName, null, contentValues);
    }

    public Boolean isArtigo(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + tableName + " WHERE id = '" + id + "' order by id desc", null);
        if(res.getCount() > 0) {
            return true;
        }else{
            return false;
        }
    }

    public void deleteArtigos(Context c, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tableName, "id = ? ", new String[]{id});
        try {
            BookmarksActivity.getInstance().reloadListView();
        }catch (Exception e){}
    }

    public String getAllBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select id from " + tableName + " order by id desc", null);
        cursor.moveToFirst();

        ArrayList<String> mylist = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            mylist.add(cursor.getString(cursor.getColumnIndex("id")));
            cursor.moveToNext();
        }
        return android.text.TextUtils.join(",", mylist);
    }
}