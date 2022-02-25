package com.florencia.pedidossi.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.pedidossi.models.Canton;
import com.florencia.pedidossi.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class CantonRepository {
    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAGCANTON";

    public static Canton get(Integer codigo) {
        Canton canton = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM canton where idcanton = ?", new String[]{codigo.toString()});
            if (cursor.moveToFirst())
                canton = AsignaDatos(cursor);
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "get()" + ec.getMessage());
            ec.printStackTrace();
        }
        return canton;
    }

    public static List<Canton> getList(Integer idprovincia) {
        List<Canton> lista = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(
                    "SELECT DISTINCT * FROM canton WHERE provinciaid = ? " +
                            "UNION " +
                            "SELECT * FROM canton WHERE idcanton = 0 " +
                            "ORDER BY nombrecanton", new String[]{idprovincia.toString()});
            Canton canton;
            if (cursor.moveToFirst()) {
                do {
                    canton = AsignaDatos(cursor);
                    if (canton != null) lista.add(canton);
                } while (cursor.moveToNext());
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "getCatalogo" + ec.getMessage());
            ec.printStackTrace();
        }
        return lista;
    }

    public static boolean SaveLista(List<Canton> cantones) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Canton item : cantones) {
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "canton(idcanton, nombrecanton, provinciaid)" +
                                "values(?, ?, ?)",
                        new String[]{item.idcanton.toString(), item.nombrecanton, item.provinciaid.toString()});
            }
            sqLiteDatabase.close();
            Log.d(TAG, "Guardó lista cantones");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "SaveLista(): " + ex.getMessage());
            return false;
        }
    }

    public static Canton AsignaDatos(Cursor cursor) {
        Canton Item = null;
        try {
            Item = new Canton();
            Item.idcanton = cursor.getInt(0);
            Item.nombrecanton = cursor.getString(1);
            Item.provinciaid = cursor.getInt(2);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }
    public static int exists() {
        int retorno = 0;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT count(*) FROM canton", null);
            if (cursor.moveToFirst()) {
                retorno = cursor.getInt(0);
            }
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "exists(): " + ec.getMessage());
            ec.printStackTrace();
        }
        return retorno;
    }
}
