package rs.mosis.diplomski.bus;

import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private SQLiteOpenHelper sqLiteOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BusDatabasesHelper busDatabasesHelper = new BusDatabasesHelper(getApplicationContext(),"",null,1);
                    String [] files = busDatabasesHelper.checkDatabase();
                    double [] verzije = busDatabasesHelper.getVersions();
                    String strukture,red;
                    strukture = "0.0";
                    red = "0.0";
                    if(files != null)
                        for(int i = 0; i < files.length; i++)
                        {
                            if (files[i].charAt(0) == 'S')
                                strukture = verzije[i] + "";
                            else if (files[i].charAt(0) == 'R')
                                red = verzije[i] + "";
                        }
                    String poruka = strukture + "\n" + red + "\n";
                    InetAddress inetAddress = InetAddress.getByName("192.168.0.2");
                    //Socket socket = new Socket(inetAddress, 8001);
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(inetAddress,8001),1000);
                    //int filesize = 6022386;
                    int bytesRead;
                    int current = 0;
                    //byte[] mybytearray = new byte[filesize];

                    InputStream is = socket.getInputStream();
                    OutputStream out = socket.getOutputStream();
                    PrintWriter printWriter = new PrintWriter(out);
                    printWriter.print(poruka);
                    printWriter.flush();


                    BufferedReader input = new BufferedReader(new InputStreamReader(is));
                    poruka = input.readLine();
                    int broj = Integer.parseInt(poruka);
                    if(broj == 0)
                        return;
                    int [] filesize = new int[broj];
                    String [] imeBaze = new String[broj];

                    for(int i = 0; i < broj; i++)
                    {
                        imeBaze[i] = input.readLine();
                        filesize[i] = Integer.parseInt(input.readLine());
                    }
                    for(int i = 0; i < broj; i++) {
                        File f = new File(busDatabasesHelper.getDatabasePath() + imeBaze[i]);
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                        FileOutputStream fos = new FileOutputStream(
                                busDatabasesHelper.getDatabasePath() + imeBaze[i]);
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        byte[] mybytearray = new byte[filesize[i]];

                        bytesRead = is.read(mybytearray, 0, mybytearray.length);
                        current = bytesRead;

                        while((current < filesize[i]) && (bytesRead > 0)) {
                            bytesRead = is.read(mybytearray, current,
                                    (mybytearray.length - current));
                            if (bytesRead >= 0)
                                current += bytesRead;
                        }

                        bos.write(mybytearray, 0, current);
                        bos.flush();
                        bos.close();
                    }

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getApplicationContext(),Glavna_Aktivnost.class);
                        startActivity(i);
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
