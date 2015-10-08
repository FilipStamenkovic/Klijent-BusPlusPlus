package rs.mosis.diplomski.bus;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

public class Podesavanja extends AppCompatActivity
{

    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podesavanja);
        preferences = Glavna_Aktivnost.preferences;
        switch (Constants.mode)
        {
            case 4:
                ((RadioButton)findViewById(R.id.ekonomicni)).setChecked(true);
                break;
            case 7:
                ((RadioButton)findViewById(R.id.minwalk)).setChecked(true);
                break;
            case 6:
                ((RadioButton)findViewById(R.id.optimalni)).setChecked(true);
                break;
            case 5:
                ((RadioButton)findViewById(R.id.ekoopt)).setChecked(true);
                break;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
      //  getMenuInflater().inflate(R.menu.menu_podesavanja, menu);
        return true;
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
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
