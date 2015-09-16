package rs.mosis.diplomski.bus;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    private void proveriVerzije(final char baza) {
        try {
            BusDatabasesHelper busDatabasesHelper = new BusDatabasesHelper(getApplicationContext(), "", null, 1);
            String[] files = busDatabasesHelper.checkDatabase();
            double[] verzije = busDatabasesHelper.getVersions();
            String strukture, red;
            strukture = "0.0";
            red = "0.0";
            if (files != null)
                for (int i = 0; i < files.length; i++) {
                    if (files[i].charAt(0) == 'S')
                        strukture = verzije[i] + "";
                    else if (files[i].charAt(0) == 'R')
                        red = verzije[i] + "";
                }
            Request request;
            if (baza == 'S')
                request = new Request(0, null, null, null, null, null, null, new Double(strukture));
            else
                request = new Request(1, null, null, null, null, null, null, new Double(red));
            String poruka = request.toString();
            InetAddress inetAddress = InetAddress.getByName(Constants.IP);
            Socket socket = new Socket(inetAddress, Constants.PORT);
            // Socket socket = new Socket();
            //socket.connect(new InetSocketAddress(inetAddress,Constants.PORT),Constants.TIMEOUT);
            int bytesRead = 0;
            int current = 0;

            InputStream is = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(out);
            // printWriter.print("kurac");
            //printWriter.flush();

            printWriter.print(poruka + "\n");
            printWriter.flush();


            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            poruka = input.readLine();
            if (poruka == null) {
                is.close();
                printWriter.close();
                out.close();
                socket.close();
                return;
            }

            Gson gson = new GsonBuilder().create();
            Response odgovor = gson.fromJson(poruka, Response.class);
            //poruka = input.readLine();
            if (odgovor.size == -1) {
                is.close();
                printWriter.close();
                out.close();
                socket.close();
                return;
            }
            int filesize = odgovor.size;
            String imeBaze;
            if (baza == 'S')
                imeBaze = "Strukture_" + odgovor.dbVer + ".db";
            else
                imeBaze = "Red_Voznje" + odgovor.dbVer + ".db";

            if (files != null)
                for (int i = 0; i < files.length; i++)
                    if (baza == files[i].charAt(0)) {
                        File f = new File(busDatabasesHelper.getDatabasePath() + files[i]);
                        if (f.exists())
                            f.delete();
                    }

            File f = new File(busDatabasesHelper.getDatabasePath() + imeBaze);
            f.getParentFile().mkdirs();
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(
                    busDatabasesHelper.getDatabasePath() + imeBaze);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] mybytearray = new byte[filesize];

            while (current < filesize) {
                int preostalo = filesize - current;
                bytesRead = is.read(mybytearray, current, preostalo);
                current += bytesRead;

                if (bytesRead == -1) {
                    break;
                }

                       /* while ((current < filesize) && (bytesRead > 0)) {
                            bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                            if (bytesRead >= 0)
                                current += bytesRead;
                        }*/
            }

            if (bytesRead != -1)
                bos.write(mybytearray, 0, current);
            bos.flush();
            bos.close();
            printWriter.close();
            out.close();
            socket.close();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                proveriVerzije('S');
                //proveriVerzije('R');
                loadGraf();
            }
        }).start();


    }

    private void loadGraf()
    {

        BusDatabasesHelper busDatabasesHelper = new BusDatabasesHelper(getApplicationContext(), "", null, 1);
        String baza = busDatabasesHelper.checkDatabase()[0];
        BusDatabasesHelper.setDbName(baza);
        baza = busDatabasesHelper.getDatabasePath() + baza;

        try {
            graf = new Graf(baza, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //graf = new Graf()
    }

    private void startApplication()
    {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(getApplicationContext(),Glavna_Aktivnost.class);
                finish();
                startActivity(i);
            }
        });
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
