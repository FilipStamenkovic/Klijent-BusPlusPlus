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
public class LinijeDBAdapter
{

    public static final String TABLE = "LINIJA";
    public static final String PLACE_ID = "ID";
    public static final String PLACE_NAME = "Name";
    public static final String PLACE_DESCRIPTION = "Desc";
    public static final String PLACE_LONG = "Long";
    public static final String PLACE_LAT = "Lat";

    public static void getAllLinije()
    {
        SQLiteDatabase database = BusDatabasesHelper.getDatabase();

        Cursor cursor = null;

        database.beginTransaction();
        try
        {
            cursor = database.query(TABLE, null, null, null, null, null, null);
            database.setTransactionSuccessful();
        }catch (SQLiteException e)
        {
            Log.v("MyPlacesDBAdapter", e.getMessage());
        }finally {
            database.endTransaction();
        }

        if(database.isOpen())
            database.close();
    }
}
