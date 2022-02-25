package com.florencia.pedidossi.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.florencia.pedidossi.models.Provincia;
import com.florencia.pedidossi.services.SQLite;

import java.util.ArrayList;
import java.util.List;

public class ProvinciaRepository {
    public static SQLiteDatabase sqLiteDatabase;
    public static final String TAG = "TAGPROVINCIA";

    public static Provincia get(Integer codigo) {
        Provincia provincia = null;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM provincia where idprovincia = ?", new String[]{codigo.toString()});
            if (cursor.moveToFirst())
                provincia = AsignaDatos(cursor);
            cursor.close();
            sqLiteDatabase.close();
        } catch (Exception ec) {
            Log.d(TAG, "get()" + ec.getMessage());
            ec.printStackTrace();
        }
        return provincia;
    }

    public static List<Provincia> getList() {
        List<Provincia> lista = new ArrayList<>();
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM provincia ORDER BY nombreprovincia", null);
            Provincia provincia;
            if (cursor.moveToFirst()) {
                do {
                    provincia = AsignaDatos(cursor);
                    if (provincia != null) lista.add(provincia);
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

    public static boolean SaveLista(List<Provincia> provincias) {
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            for (Provincia item : provincias) {
                sqLiteDatabase.execSQL("INSERT OR REPLACE INTO " +
                                "provincia(idprovincia, nombreprovincia)" +
                                "values(?, ?)",
                        new String[]{item.idprovincia.toString(), item.nombreprovincia});
            }
            sqLiteDatabase.close();
            Log.d(TAG, "Guardó lista provincias");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "SaveLista(): " + ex.getMessage());
            return false;
        }
    }

    public static Provincia AsignaDatos(Cursor cursor) {
        Provincia Item = null;
        try {
            Item = new Provincia();
            Item.idprovincia = cursor.getInt(0);
            Item.nombreprovincia = cursor.getString(1);
        } catch (SQLiteException ec) {
            Log.d(TAG, ec.getMessage());
        }
        return Item;
    }

    public static int exists() {
        int retorno = 0;
        try {
            sqLiteDatabase = SQLite.sqlDB.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT count(*) FROM provincia", null);
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
