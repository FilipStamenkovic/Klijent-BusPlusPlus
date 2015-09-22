package rs.mosis.diplomski.bus;

import com.google.android.gms.maps.model.LatLng;
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import strukture.Graf;

/**
 * Created by filip on 9/22/15.
 */
public class Komunikacija_Server
{

    public static boolean proveriVerzije(final char baza)
    {
        boolean b = false;
        try {

            BusDatabasesHelper busDatabasesHelper = BusDatabasesHelper.getInstance();
            String file = busDatabasesHelper.checkDatabase(baza);
            double verzija = busDatabasesHelper.getVersions(baza);
            Request request;
            if (baza == 'S')
                request = new Request(0, null, null, null, null, null, null, new Double(verzija));
            else
                request = new Request(1, null, null, null, null, null, null, new Double(verzija));
            String poruka = request.toString();
            InetAddress inetAddress = InetAddress.getByName(Constants.IP);
//            Socket socket = new Socket(inetAddress, Constants.PORT);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(inetAddress,Constants.PORT),Constants.TIMEOUT);
            int bytesRead = 0;
            int current = 0;

            InputStream is = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(out);

            printWriter.print(poruka + "\n");
            printWriter.flush();


            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            poruka = input.readLine();
            if (poruka == null) {
                is.close();
                printWriter.close();
                out.close();
                socket.close();
                return false;
            }

            Gson gson = new GsonBuilder().create();
            Response odgovor = gson.fromJson(poruka, Response.class);
            //poruka = input.readLine();
            if (odgovor.size == -1) {
                is.close();
                printWriter.close();
                out.close();
                socket.close();
                return true;
            }
            int filesize = odgovor.size;
            String imeBaze;
            if (baza == 'S')
                imeBaze = "Strukture_" + odgovor.dbVer + ".db";
            else
                imeBaze = "Red_Voznje" + odgovor.dbVer + ".db";


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

            }

            if (bytesRead != -1)
            {
                bos.write(mybytearray, 0, current);
                b = true;
                if (file != null)
                    //   for (int i = 0; i < files.length; i++)
                    if (baza == file.charAt(0)) {
                        File ff = new File(busDatabasesHelper.getDatabasePath() + file);
                        if (ff.exists())
                            ff.delete();
                    }
            }
            else
                b = false;
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

        return b;
    }

    public static Graf loadGraf()
    {
        BusDatabasesHelper busDatabasesHelper = BusDatabasesHelper.getInstance();
       /* String []files = busDatabasesHelper.checkDatabase();
        if(files == null)
            return false;

        String baza = files[0];*/
        Graf graf = null;
        String baza = busDatabasesHelper.checkDatabase('S');
        if(baza == null)
            return graf;
        //BusDatabasesHelper.setDbName(baza);
        // baza = busDatabasesHelper.getDatabasePath() + baza;

        String red_voznje = busDatabasesHelper.checkDatabase('R');

        File f = new File(busDatabasesHelper.getDatabasePath() + baza);
        File f2 = new File(busDatabasesHelper.getDatabasePath() + red_voznje);

        try {
            graf = new Graf(baza, red_voznje);
        } catch (Exception e) {
            e.printStackTrace();

            File [] fajlovi = (new File(busDatabasesHelper.getDatabasePath())).listFiles();
            for(int i = 0; i < fajlovi.length; i++)
            {
                fajlovi[i].delete();
            }

            return null;
        }

        return graf;
    }

    public static Response ObicanRedVoznje(LatLng latLng,int linija_id)
    {
        Response odgovor = null;
        try
        {

            Request request = new Request(new Integer(3), new Double(latLng.latitude),
                    new Double(latLng.longitude), null, null, linija_id, null, null);
            String poruka = request.toString();
            InetAddress inetAddress = InetAddress.getByName(Constants.IP);
//            Socket socket = new Socket(inetAddress, Constants.PORT);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(inetAddress, Constants.PORT), Constants.TIMEOUT);
            int bytesRead = 0;
            int current = 0;

            InputStream is = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(out);


            printWriter.print(poruka + "\n");
            printWriter.flush();


            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            poruka = input.readLine();
            if (poruka == null)
            {
                is.close();
                printWriter.close();
                out.close();
                socket.close();
                return null;
            }

            Gson gson = new GsonBuilder().create();
            odgovor = gson.fromJson(poruka, Response.class);
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return odgovor;
    }
    public static ArrayList<String> vremenaDolaska(Response odgovor)
    {
        List<List<Integer>> vremena= new ArrayList<List<Integer>>(odgovor.linije.length);
        ArrayList<String> povratniString = new ArrayList<>();

        for(int i = 0; i < odgovor.linije.length; i++)
            vremena.add(MainActivity.graf.getGl().linije[odgovor.linije[i]].getVremena(odgovor.korekcije[i]));


        int pocetak = 0;
        int kraj;
        while(pocetak < odgovor.stanice.length - 1)
        {
            kraj = pocetak;
            for (int i = pocetak; i < odgovor.stanice.length - 1; i++)
                if (odgovor.stanice[i].intValue() == odgovor.stanice[i + 1].intValue())
                    kraj = i + 1;
                else
                    break;


            for(int j = pocetak; j <= kraj; j++)
            {
                String s = "";
                while(!vremena.get(j).isEmpty())
                {
                    int niz[] = new int[kraj - pocetak + 1];
                    for (int i = pocetak; i <= kraj; i++)
                    {
                        if (vremena.get(i).size() > 0)
                        {
                            niz[i - pocetak] = vremena.get(i).get(0);
                        }
                        else
                            niz[i - pocetak] = -1;
                    }

                    int min = 100000;
                    int indeks = 0;
                    for(int k = 0; k < niz.length; k++)
                        if(min > niz[k] && (niz[k] != -1))
                        {
                            min = niz[k];
                            indeks = k;

                        }
                    if((min % 100) > 10)
                        s += min / 100 + ":" + min %100;
                    else
                        s += min / 100 + ":0" + min %100;
                    for(int l = 0; l < indeks + pocetak; l++)
                        s += "*";
                    s += "\n";
                    vremena.get(indeks + pocetak).remove(0);


                }
                povratniString.add(s);

            }

            pocetak = kraj + 1;
        }
        return povratniString;
    }
}
