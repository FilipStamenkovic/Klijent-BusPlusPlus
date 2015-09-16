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
    public static final String STANICA_TABLE = "STANICA";
    public static final String VEZE_TABLE = "VEZA";
    public static final String ID = "id";
    public static final String LINIJA_BROJ = "broj";
    public static final String NAZIV = "naziv";
    public static final String LINIJA_SMER = "smer";
    public static final String LINIJA_POCETNA_STANICA = "pocetna_stanica_id";
    public static final String STANICA_LAT = "lat";
    public static final String STANICA_LON = "lon";
    public static final String VEZA_POLAZNA_STANICA = "polazna_stanica_id";
    public static final String VEZA_DOLAZNA_STANICA = "dolazna_stanica_id";
    public static final String VEZA_UDALJENOST = "udaljenost";
    public static final String VEZA_LINIJA_ID = "linija_id";

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
                    int id = cursor.getInt(cursor.getColumnIndex(ID));
                    String broj = cursor.getString(cursor.getColumnIndex(LINIJA_BROJ));
                    String smer = cursor.getString(cursor.getColumnIndex(LINIJA_SMER));
                    String naziv = cursor.getString(cursor.getColumnIndex(NAZIV));
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

        SQLiteDatabase database = BusDatabasesHelper.getDatabase();

        Cursor cursor = null;

        database.beginTransaction();
        try
        {
            cursor = database.rawQuery("select max(id) from " + STANICA_TABLE,null);
            database.setTransactionSuccessful();
            if(cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                maxId = cursor.getInt(0);

                tempArray= new Cvor[maxId + 1];

            }
            database.endTransaction();

            database.beginTransaction();

            cursor = database.query(STANICA_TABLE, null, null, null, null, null, null);
            database.setTransactionSuccessful();
            if(cursor.getCount() > 0)
            {
                while(cursor.moveToNext())
                {
                    int id = cursor.getInt(cursor.getColumnIndex(ID));

                    String naziv = cursor.getString(cursor.getColumnIndex(NAZIV));
                    double lat = cursor.getDouble(cursor.getColumnIndex(STANICA_LAT));
                    double lon = cursor.getDouble(cursor.getColumnIndex(STANICA_LON));

                    tempArray[id] = new Cvor(id, naziv, lat,lon);
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


        return tempArray;
    }

    public static boolean podesiVeze(Cvor[] cvorovi,Linija[] linije)
    {
       /* rs = statement.executeQuery("select * from VEZA");

        while(rs.next())
        {
            // read the result set
            int sourceId = rs.getInt("polazna_stanica_id");
            int destId = rs.getInt("dolazna_stanica_id");
            int weight = rs.getInt("udaljenost");
            int linijaId = rs.getInt("linija_id");

            tempArray[sourceId].dodajVezu(gl.linije[linijaId], weight, tempArray[destId]);;
        }*/

        SQLiteDatabase database = BusDatabasesHelper.getDatabase();

        Cursor cursor = null;
        boolean b = true;

        database.beginTransaction();
        try
        {

            cursor = database.query(VEZE_TABLE, null, null, null, null, null, null);
            database.setTransactionSuccessful();
            if(cursor.getCount() > 0)
            {
                while(cursor.moveToNext())
                {

                    int sourceId = cursor.getInt(cursor.getColumnIndex(VEZA_POLAZNA_STANICA));
                    int destId = cursor.getInt(cursor.getColumnIndex(VEZA_DOLAZNA_STANICA));
                    int weight = cursor.getInt(cursor.getColumnIndex(VEZA_UDALJENOST));
                    int linijaId = cursor.getInt(cursor.getColumnIndex(VEZA_LINIJA_ID));
                   // int id = cursor.getInt(cursor.getColumnIndex(ID));



                    cvorovi[sourceId].dodajVezu(linije[linijaId], weight, cvorovi[destId]);;
                }
            }
        }catch (SQLiteException e)
        {
            Log.v("MyPlacesDBAdapter", e.getMessage());
            b = false;
        }finally {
            database.endTransaction();
        }

        if(database.isOpen())
            database.close();

        return b;
    }

}
