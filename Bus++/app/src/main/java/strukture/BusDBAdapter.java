package strukture;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

import rs.mosis.diplomski.bus.BusDatabasesHelper;

/**
 * Created by filip on 9/16/15.
 */
public class BusDBAdapter
{

    public static final String LINIJA_TABLE = "LINIJA";
    public static final String LINIJA_ID = "id";
    public static final String LINIJA_BROJ = "broj";
    public static final String LINIJA_NAZIV = "naziv";
    public static final String LINIJA_SMER = "smer";
    public static final String LINIJA_POCETNA_STANICA = "pocetna_stanica_id";

    public static Linija[] getAllLinije()
    {
        Linija [] linije = null;
        SQLiteDatabase database = BusDatabasesHelper.getDatabase();

        Cursor cursor = null;

        database.beginTransaction();
        try
        {
            cursor = database.rawQuery("select max(id) from " + LINIJA_TABLE,null);
            database.setTransactionSuccessful();
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                int maxId = cursor.getInt(0);

                linije = new Linija[maxId + 1];

            }
            database.endTransaction();

            database.beginTransaction();

            cursor = database.query(LINIJA_TABLE, null, null, null, null, null, null);
            database.setTransactionSuccessful();
            if(cursor.getCount() > 0)
            {
                while(cursor.moveToNext())
                {
                    int id = cursor.getInt(cursor.getColumnIndex(LINIJA_ID));
                    String broj = cursor.getString(cursor.getColumnIndex(LINIJA_BROJ));
                    String smer = cursor.getString(cursor.getColumnIndex(LINIJA_SMER));
                    String naziv = cursor.getString(cursor.getColumnIndex(LINIJA_NAZIV));
                    int stanicaId = cursor.getInt(cursor.getColumnIndex(LINIJA_POCETNA_STANICA));
                    //dummy cvor da sacuva ID za pravi cvor koji se tek kasnije ucitava iz baze i kreira
                    Cvor pocetnaStanica = new Cvor(stanicaId, null, null, null);
                    linije[id] = new Linija(id, broj, smer, naziv, pocetnaStanica);
                }
            }
        }catch (SQLiteException e)
        {
            Log.v("MyPlacesDBAdapter", e.getMessage());
        }finally {
            database.endTransaction();
        }

        if(database.isOpen())
            database.close();

        return linije;
    }

    public static Cvor[] getAllCvorovi()
    {
        int maxId = 0;
        Cvor tempArray[] = null;

        return tempArray;
    }

}
