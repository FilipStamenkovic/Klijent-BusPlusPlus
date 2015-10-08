package rs.mosis.diplomski.bus;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by filip on 30.8.15..
 */
public class BusDatabasesHelper extends SQLiteOpenHelper
{
    private static String DB_PATH = "";
    private static SQLiteDatabase database = null;
    private final Context mContext;
    private static String DB_NAME = "";
    private static BusDatabasesHelper busDatabasesHelper = null;

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
        busDatabasesHelper = this;
    }

    public boolean loadFromAsset(char baza)
    {
        boolean b = false;
        InputStream myInput;
        String outFileName;

            try
            {
                if(baza == 'S')
                {
                    myInput = mContext.getAssets().open("Strukture_0.5.db");
                    // Path to the just created empty db
                    outFileName = DB_PATH + "Strukture_0.5.db";
                }else if (baza == 'R')
                {
                    myInput = mContext.getAssets().open("Red_Voznje0.3.db");
                    // Path to the just created empty db
                    outFileName = DB_PATH + "Red_Voznje0.3.db";
                }else
                {
                    myInput = mContext.getAssets().open("Putanje_0.1.db");
                    // Path to the just created empty db
                    outFileName = DB_PATH + "Putanje_0.1.db";
                }

                File f = new File(outFileName);
                if (!f.exists())
                {
                    f.getParentFile().mkdirs();
                    f.createNewFile();
                }
                //Open the empty db as the output stream
                OutputStream myOutput = new FileOutputStream(outFileName);

                //transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer))>0){
                    myOutput.write(buffer, 0, length);
                }

                //Close the streams
                myOutput.flush();
                myOutput.close();
                myInput.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }



        return b;
    }

    public static BusDatabasesHelper getInstance()
   {
       return busDatabasesHelper;
   }

    public String checkDatabase(char baza)
    {
        File dbFile = new File(DB_PATH );
        String povratak = null;
        if(dbFile.exists())
        {
            for (int i = 0; i < dbFile.list().length; i++)
                if (dbFile.list()[i].charAt(0) == baza)
                {
                    povratak = dbFile.list()[i];
                    break;
                }
        }
        return povratak;
        //return dbFile.exists();
    }

    public double getVersions(char baza)
    {

        String file = checkDatabase(baza);
        if (file == null)
            return 0.0;
        //double [] niz = new double[files.length];
        double verzija = 0.0;

        File f = new File(file);
        String broj = file.replaceAll("[^0-9 .]", "");
          /*  if(broj.charAt(0) == '.')
            {
                niz[i] = -1.0;
                continue;
            }*/
        broj = broj.substring(0, broj.length() - 1);

        verzija = Double.parseDouble(broj);
        //    niz[i] = br;

        return verzija;
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
