package rs.mosis.diplomski.bus;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import strukture.Graf;

public class MainActivity extends AppCompatActivity {

    private SQLiteOpenHelper sqLiteOpenHelper;
    public static Graf graf;
    public static Context aplikacija;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        aplikacija = this.getApplicationContext();

        if(BusDatabasesHelper.getInstance() == null)
        {
            new BusDatabasesHelper(aplikacija,"",null,1);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Komunikacija_Server.proveriVerzije('S');
                Komunikacija_Server.proveriVerzije('R');
                graf = Komunikacija_Server.loadGraf();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(graf == null)
                            Toast.makeText(getApplicationContext(),"Greska, konektuj se na internet",Toast.LENGTH_LONG).show();
                        else
                        {
                            Intent i = new Intent(getApplicationContext(),Glavna_Aktivnost.class);
                            finish();
                            startActivity(i);
                        }
                    }
                });

            }
        }).start();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
