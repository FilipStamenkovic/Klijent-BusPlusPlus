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
import java.util.StringTokenizer;

import strukture.GradskeLinije;
import strukture.Graf;
import strukture.OfflineRezim;

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
            if (file == null)
            {
                busDatabasesHelper.loadFromAsset(baza);
                file = busDatabasesHelper.checkDatabase(baza);
            }
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
            {
                b = false;
                File ff = new File(busDatabasesHelper.getDatabasePath() + imeBaze);
                if (ff.exists())
                    ff.delete();
            }

            bos.flush();
            bos.close();
            printWriter.close();
            out.close();
            socket.close();

        } catch (SocketTimeoutException e) {
            b = true;
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            b = true;
            e.printStackTrace();
        } catch (IOException e) {
            b = true;
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
        Request request = new Request(new Integer(3), new Double(latLng.latitude),
                new Double(latLng.longitude), null, null, linija_id, null, null);
        try
        {


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

        if(odgovor == null)
            return OfflineRezim.handleRequest3(request);

        return odgovor;
    }
    public static ArrayList<String> vremenaPolaska(Response odgovor)
    {
        List<List<Integer>> vremena = new ArrayList<List<Integer>>(odgovor.linije.length);
        ArrayList<String> povratniString = new ArrayList<>();

        for(int i = 0; i < odgovor.linije.length; i++)
            vremena.add(MainActivity.graf.getGl().linije[odgovor.linije[i]].getVremena(odgovor.korekcije[i]));


        for(int i = 0; i < vremena.size(); i++)
        {
            if(vremena.get(i).size() == 0)
                odgovor.linije[i] = -1;
        }

        int pocetak = 0;
        int kraj;
        while(pocetak <= odgovor.stanice.length - 1)
        {
            kraj = pocetak;
            for (int i = pocetak; i < odgovor.stanice.length - 1; i++)
                if (odgovor.stanice[i].intValue() == odgovor.stanice[i + 1].intValue())
                    kraj = i + 1;
                else
                    break;

            String s = "";

            for(int j = pocetak; j <= kraj; j++)
            {

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
                    String sati;
                    if(((min / 100) % 24 ) < 10)
                        sati = "0" + (min / 100) % 24;
                    else
                        sati = "" + (min / 100) % 24;

                    if((min % 100) >= 10)
                        s += sati + ":" + min % 100;
                    else
                        s += sati + ":0" + min % 100;
                    for(int l = 0; l < indeks + pocetak; l++)
                        s += "*";
                    s += "\n";
                    vremena.get(indeks + pocetak).remove(0);


                }


            }
            povratniString.add(s);
            pocetak = kraj + 1;
        }
        return povratniString;
    }

    public static ArrayList<String> vremenaPolaska(Response odgovor,int size)
    {
        List<List<Integer>> vremena = new ArrayList<List<Integer>>(odgovor.linije.length);
        ArrayList<String> povratniString = new ArrayList<>();

        for(int i = 0; i < odgovor.linije.length; i++)
            vremena.add(MainActivity.graf.getGl().linije[odgovor.linije[i]].getVremena(odgovor.korekcije[i],size));




        int pocetak = 0;
        int kraj;
        while(pocetak <= odgovor.linije.length - 1)
        {
            kraj = pocetak;
            for (int i = pocetak; i < odgovor.linije.length - 1; i++)
                if (GradskeLinije.istaOsnovna(odgovor.linije[i],odgovor.linije[i + 1]))
                    kraj = i + 1;
                else
                    break;

            String s = "";

                int brojac = 0;
                while(brojac < size)
                {
                    brojac++;
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

                    int min = 900000;
                    int indeks = 0;
                    for(int k = 0; k < niz.length; k++)
                        if(min > niz[k] && (niz[k] != -1))
                        {
                            min = niz[k];
                            indeks = k;

                        }
                    String sati;
                    if(((min / 100) % 24 ) < 10)
                        sati = "0" + (min / 100) % 24;
                    else
                        sati = "" + (min / 100) % 24;

                    if((min % 100) >= 10)
                        s += sati + ":" + min % 100;
                    else
                        s += sati + ":0" + min % 100;
                    for(int l = 0; l < indeks; l++)
                        s += "*";
                    s += "\n";
                    vremena.get(indeks + pocetak).remove(0);



                }


            povratniString.add(s);
            pocetak = kraj + 1;
        }
        return povratniString;
    }


    public static ArrayList<String> vremenaDolaska(Response odgovor,ArrayList<String> vremenaDolaska)
    {
        ArrayList<String> povratniString = new ArrayList<>();

        int[] minuti_korekcija = new int[odgovor.korekcije.length];
        int[] sati_korekcija = new int[odgovor.korekcije.length];

        for (int i = 0; i < sati_korekcija.length; i++)
        {
            int sekunde = odgovor.korekcije[i].intValue();
            sati_korekcija[i] = sekunde / 3600;
            minuti_korekcija[i] = (sekunde % 3600) / 60;
            while (minuti_korekcija[i] > 59)
            {
                sati_korekcija[i]++;
                minuti_korekcija[i] -= 60;
            }
        }

        int size = vremenaDolaska.size();
        for (int i = 0; i < size; i++)
            if (!vremenaDolaska.get(i).equals(""))
            {
                StringTokenizer tokenizer = new StringTokenizer(vremenaDolaska.get(i), "\n");
                String s = "";
                while (tokenizer.hasMoreTokens())
                {
                    String token = tokenizer.nextToken();
                    int sati, minuti, korekcija_id;
                    sati = Integer.parseInt(token.substring(0, 2));
                    minuti = Integer.parseInt(token.substring(3, 5));
                    korekcija_id = token.length() - 5;

                    sati += sati_korekcija[korekcija_id];
                    minuti += minuti_korekcija[korekcija_id];
                    while (minuti > 59)
                    {
                        sati++;
                        minuti -= 60;
                    }
                    if (minuti >= 10)
                        s += sati + ":" + minuti;
                    else
                        s += sati + ":0" + minuti;
                    for (int l = 0; l < korekcija_id; l++)
                        s += "*";
                    s += "\n";
                }
                povratniString.add(s);
            } else
                povratniString.add("");


        return povratniString;
    }

    public static Response ekonomicniRezim(LatLng source, LatLng destionation)
    {
        Response odgovor = null;
        Request request = new Request(new Integer(4), new Double(source.latitude),
                new Double(source.longitude), new Double(destionation.latitude),
                new Double(destionation.longitude), null, null, null);
        try
        {

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

        if (odgovor == null)
            odgovor = OfflineRezim.handleRequest4(request);

        return odgovor;
    }


}
