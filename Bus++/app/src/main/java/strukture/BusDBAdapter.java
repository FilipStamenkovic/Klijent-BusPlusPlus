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

                Graf.stanicaMaxId = maxId;

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

    public static boolean podesiRedVoznje(Linija linija)
    {
        boolean b = false;
        SQLiteDatabase database = BusDatabasesHelper.getDatabase();
        Cursor radni_dan = null;
        Cursor subota = null;
        Cursor nedelja = null;
        database.beginTransaction();
        try
        {

            String[] whereArgs = new String[] {
                    linija.broj,
                    linija.smer
            };


            radni_dan = database.rawQuery("select RED_VOZNJE.cas, RADNI_DAN_MINUTA.radni_dan_minuta from " +
                    "RED_VOZNJE, RADNI_DAN_MINUTA where RED_VOZNJE.id = RADNI_DAN_MINUTA.red_voznje_id and " +
                    "RED_VOZNJE.linija= ? and RED_VOZNJE.smer= ? ORDER BY RED_VOZNJE.cas, " +
                    "RADNI_DAN_MINUTA.radni_dan_minuta",whereArgs);

            subota = database.rawQuery("select RED_VOZNJE.cas, SUBOTA_MINUTA.subota_minuta from " +
                    "RED_VOZNJE, SUBOTA_MINUTA where RED_VOZNJE.id = SUBOTA_MINUTA.red_voznje_id and " +
                    "RED_VOZNJE.linija= ? and RED_VOZNJE.smer= ? " +
                    "ORDER BY RED_VOZNJE.cas, SUBOTA_MINUTA.subota_minuta",whereArgs);
            nedelja = database.rawQuery("select RED_VOZNJE.cas, NEDELJA_MINUTA.nedelja_minuta from " +
                    "RED_VOZNJE, NEDELJA_MINUTA where RED_VOZNJE.id = NEDELJA_MINUTA.red_voznje_id and " +
                    "RED_VOZNJE.linija= ? and RED_VOZNJE.smer= ? " +
                    "ORDER BY RED_VOZNJE.cas, NEDELJA_MINUTA.nedelja_minuta",whereArgs);

            database.setTransactionSuccessful();
            if((radni_dan.getCount() > 0) || (subota.getCount() > 0) || (nedelja.getCount() > 0))
            {

                int matRadni[][] = new int[25][60];
                int matSubota[][] = new int[25][60];
                int matNedelja[][] = new int[25][60];

                for(int k = 0; k < 25; ++k)
                    for(int q = 0; q < 60; ++q)
                    {
                        matRadni[k][q] = -1;
                        matSubota[k][q] = -1;
                        matNedelja[k][q] = -1;
                    }
                int index;
                while(radni_dan.moveToNext())
                {

                    index = 0;
                    int cas = radni_dan.getInt(radni_dan.getColumnIndex("cas"));
                    int radni_dan_minuta = radni_dan.getInt(radni_dan.getColumnIndex("radni_dan_minuta"));
                    while(matRadni[cas][index] != -1)
                        ++index;
                    matRadni[cas][index] = radni_dan_minuta;
                    // int id = cursor.getInt(cursor.getColumnIndex(ID));

                }
                linija.setMatRadni(matRadni);
                while(subota.moveToNext())
                {

                    index = 0;
                    int cas = subota.getInt(subota.getColumnIndex("cas"));
                    int radni_dan_minuta = subota.getInt(subota.getColumnIndex("subota_minuta"));
                    while(matSubota[cas][index] != -1)
                        ++index;
                    matSubota[cas][index] = radni_dan_minuta;
                    // int id = cursor.getInt(cursor.getColumnIndex(ID));

                }
                linija.setMatSubota(matSubota);
                while(nedelja.moveToNext())
                {

                    index = 0;
                    int cas = nedelja.getInt(nedelja.getColumnIndex("cas"));
                    int radni_dan_minuta = nedelja.getInt(nedelja.getColumnIndex("nedelja_minuta"));
                    while(matNedelja[cas][index] != -1)
                        ++index;
                    matNedelja[cas][index] = radni_dan_minuta;
                    // int id = cursor.getInt(cursor.getColumnIndex(ID));

                }
                linija.setMatNedelja(matNedelja);
                b = true;
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
