package rs.mosis.diplomski.bus;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.File;

/**
 * Created by filip on 30.8.15..
 */
public class BusDatabasesHelper extends SQLiteOpenHelper
{
    private static String DB_PATH = "";
    private static SQLiteDatabase database = null;
    private final Context mContext;
    private static String DB_NAME = "";

    public BusDatabasesHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        DB_NAME = name;
        mContext = context;
        if(Build.VERSION.SDK_INT >= 17)
        {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }
        else
        {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
    }

    public String[] checkDatabase()
    {
        File dbFile = new File(DB_PATH );
        return dbFile.list();
        //return dbFile.exists();
    }

    public double[] getVersions()
    {

        String []files = checkDatabase();
        if(files == null)
            return null;
        double [] niz = new double[files.length];

        for(int i = 0;i < files.length; i++ )
        {
            File f = new File(files[i]);
            String broj = files[i].replaceAll("[^0-9 .]", "");
            if(broj.charAt(0) == '.')
            {
                niz[i] = -1.0;
                continue;
            }
            broj = broj.substring(0, broj.length()-1);

            double br = Double.parseDouble(broj);
            niz[i] = br;
        }
        return niz;
    }

    public static void setDbName(String dbName) {
        DB_NAME = dbName;
    }

    private static boolean openDatabase()
    {
        String path = DB_PATH + DB_NAME;
        database = SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READWRITE);
        return database != null;
    }

    public static SQLiteDatabase getDatabase() {
        if(openDatabase())
            return database;
        else
            return null;
    }

    @Override
    public synchronized void close()
    {
        if(database != null)
            database.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    public static String getDatabasePath()
    {

        return DB_PATH;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL("DROP TABLE IF EXISTS " + BusDatabasesHelper.D);
    }
}
