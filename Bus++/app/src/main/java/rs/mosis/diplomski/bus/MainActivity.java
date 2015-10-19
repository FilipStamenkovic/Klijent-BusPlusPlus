package rs.mosis.diplomski.bus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;

import strukture.BusDBAdapter;
import strukture.Graf;

public class MainActivity extends Activity
{

    public static Graf graf;
    public static MainActivity aplikacija;
    public static int[] ikonice;
    public static int progres;
    public ProgressBar mProgress;
    public static Komunikacija_Server komunikacija;
    public static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

      //  this.requestWindowFeature(Window.FEATURE_NO_TITLE);

      //  this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);

        mProgress = (ProgressBar) findViewById(R.id.progressBar);

        komunikacija = new Komunikacija_Server();

        progres = 0;

        preferences = getSharedPreferences(Constants.rezim, MODE_PRIVATE);
        Constants.numberTokens = preferences.getInt("tokens", 1000);

        int jez = preferences.getInt("jezik", -1);
        if (jez != -1)
        {
            // Constants.jezik = jez;
                Podesavanja.setLocale(this, 1, jez);
        }


        aplikacija = this;

        if(BusDatabasesHelper.getInstance() == null)
        {
            new BusDatabasesHelper(aplikacija,"",null,1);
        }

    }

    public void namestiProgres()
    {
        progres++;
        int ukupno = (int)((double)progres / Constants.BROJ_PROGRES  * 100);
        mProgress.setProgress(ukupno);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
       // namestiProgres();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                graf = null;
                boolean b = komunikacija.proveriVerzije('S');
                if (b)
                    b = komunikacija.proveriVerzije('R');
                if (b)
                    b = komunikacija.proveriVerzije('P');
                if (b)
                    graf = komunikacija.loadGraf();
                else
                    graf = null;
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (graf == null)
                        {
                            Toast.makeText(getApplicationContext(), "Greska, konektuj se na internet", Toast.LENGTH_LONG).show();

                            progres = 0;

                            File[] fajlovi = (new File(BusDatabasesHelper.getDatabasePath())).listFiles();
                            for (int i = 0; i < fajlovi.length; i++)
                            {
                                fajlovi[i].delete();
                            }

                        }else
                        {
                            aplikacija.namestiProgres();

                            Intent i = new Intent(getApplicationContext(), Glavna_Aktivnost.class);
                            finish();
                            startActivity(i);
                        }
                    }
                });


                if (graf != null)
                {
                    ikonice = new int[graf.getGl().linije.length];
                    for (int i = 0; i < graf.getGl().linije.length; i++)
                        if (graf.getGl().linije[i] != null)
                        {
                            String broj = graf.getGl().linije[i].broj;
                            broj = broj.replace("*", "");
                            int brojLinije = Integer.parseInt(broj);

                            switch (brojLinije)
                            {
                                case 1:
                                    ikonice[i] = R.mipmap.ic_linija_1;
                                    break;
                                case 2:
                                    ikonice[i] = R.mipmap.ic_linija_2;
                                    break;
                                case 3:
                                    ikonice[i] = R.mipmap.ic_linija_3;
                                    break;
                                case 4:
                                    ikonice[i] = R.mipmap.ic_linija_4;
                                    break;
                                case 5:
                                    ikonice[i] = R.mipmap.ic_linija_5;
                                    break;
                                case 6:
                                    ikonice[i] = R.mipmap.ic_linija_6;
                                    break;
                                case 7:
                                    ikonice[i] = R.mipmap.ic_linija_7;
                                    break;
                                case 8:
                                    ikonice[i] = R.mipmap.ic_linija_8;
                                    break;
                                case 9:
                                    ikonice[i] = R.mipmap.ic_linija_9;
                                    break;
                                case 10:
                                    ikonice[i] = R.mipmap.ic_linija_10;
                                    break;
                                case 12:
                                    ikonice[i] = R.mipmap.ic_linija_12;
                                    break;
                                case 13:
                                    ikonice[i] = R.mipmap.ic_linija_13;
                                    break;
                                case 36:
                                    ikonice[i] = R.mipmap.ic_linija_36;
                                    break;
                                case 34:
                                    if (graf.getGl().linije[i].smer.equalsIgnoreCase("A"))
                                        ikonice[i] = R.mipmap.ic_linija_34a;
                                    else
                                        ikonice[i] = R.mipmap.ic_linija_34b;
                                    break;

                            }

                        }

                    BusDBAdapter.setPolilinije();
                    graf.inicijalizujMatricu();
                }

            }
        }).start();





    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
