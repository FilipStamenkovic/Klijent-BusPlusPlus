package rs.mosis.diplomski.bus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import java.util.Locale;

public class Podesavanja extends AppCompatActivity
{

    SharedPreferences preferences;
    private static boolean promenaJezina = true;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podesavanja);
        this.setTitle(R.string.action_settings);
        preferences = MainActivity.preferences;

        int jez = preferences.getInt("jezik", -1);
       // Constants.jezik = -1;
        if (jez != -1)
        {
            // Constants.jezik = jez;
            if(promenaJezina)
                Podesavanja.setLocale(this, 2, jez);
            promenaJezina = !promenaJezina;
        }
        switch (Constants.mode)
        {
            case 4:
                ((RadioButton) findViewById(R.id.ekonomicni)).setChecked(true);
                break;
            case 7:
                ((RadioButton) findViewById(R.id.minwalk)).setChecked(true);
                break;
            case 6:
                ((RadioButton) findViewById(R.id.optimalni)).setChecked(true);
                break;
            case 5:
                ((RadioButton) findViewById(R.id.ekoopt)).setChecked(true);
                break;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_podesavanja, menu);
        return true;
    }

    public static void setLocale(Activity aktivnost, int num,int jezik)
    {
        Constants.jezik = jezik;

        String lang = "en";
        if (Constants.jezik == 2)
            lang = "sr";
        Locale myLocale = new Locale(lang);
        Resources res = aktivnost.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        //res.updateConfiguration(conf, dm);
        aktivnost.getBaseContext().getResources().updateConfiguration(conf, aktivnost.getBaseContext().getResources().getDisplayMetrics());
        Intent refresh;

        if (num == 1)
            refresh = new Intent(aktivnost, Glavna_Aktivnost.class);
        else
        {
            refresh = new Intent(aktivnost, Podesavanja.class);
            aktivnost.startActivity(refresh);
            aktivnost.finish();
        }
    }

    public void onRadioButtonClicked(View view)
    {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId())
        {
            case R.id.ekonomicni:
                if (checked)
                {
                    Constants.mode = 4;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("mode", 4);
                    editor.commit();
                }
                break;
            case R.id.minwalk:
                if (checked)
                {
                    Constants.mode = 7;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("mode", 7);
                    editor.commit();
                }
                break;
            case R.id.optimalni:
                if (checked)
                {
                    Constants.mode = 6;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("mode", 6);
                    editor.commit();
                }
                break;
            case R.id.ekoopt:
                if (checked)
                {
                    Constants.mode = 5;
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("mode", 5);
                    editor.commit();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_english)
        {

            //Constants.jezik = 1;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("jezik", 1);
            editor.commit();
            setLocale(this, 2,1);

            return true;
        } else if (id == R.id.action_serbian)
        {

           // Constants.jezik = 2;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("jezik", 2);
            editor.commit();
            setLocale(this, 2,2);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }
}
