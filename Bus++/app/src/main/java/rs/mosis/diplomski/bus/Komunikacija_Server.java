package rs.mosis.diplomski.bus;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import strukture.BusDBAdapter;
import strukture.CSInfo;
import strukture.Cvor;
import strukture.GradskeLinije;
import strukture.Graf;
import strukture.Linija;
import strukture.OfflineRezim;
import strukture.Veza;

/**
 * Created by filip on 9/22/15.
 */
public class Komunikacija_Server
{
    private OfflineRezim offline;
    public Komunikacija_Server()
    {
        offline = new OfflineRezim();
    }

    public OfflineRezim getOffline()
    {
        return offline;
    }

    public boolean proveriVerzije(final char baza)
    {
        boolean b = false;
        try
        {

            BusDatabasesHelper busDatabasesHelper = BusDatabasesHelper.getInstance();
            String file = busDatabasesHelper.checkDatabase(baza);
            if (file == null)
            {
                busDatabasesHelper.loadFromAsset(baza);
                file = busDatabasesHelper.checkDatabase(baza);
            }
            MainActivity.aplikacija.namestiProgres();
            double verzija = busDatabasesHelper.getVersions(baza);
            Request request;
            if (baza == 'S')
                request = new Request(0, null, null, null, null, null, new Double(verzija),null);
            else if (baza == 'R')
                request = new Request(1, null, null, null, null, null, new Double(verzija),null);
            else
                request = new Request(2, null, null, null, null, null, new Double(verzija),null);
            String poruka = request.toString();
            InetAddress inetAddress = InetAddress.getByName(Constants.IP);
//            Socket socket = new Socket(inetAddress, Constants.PORT);
            MainActivity.aplikacija.namestiProgres();
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
                input.close();
                printWriter.close();
                out.close();
                socket.close();
                return false;
            }

            Gson gson = new GsonBuilder().create();
            Response odgovor = gson.fromJson(poruka, Response.class);

            if (odgovor.size == -1)
            {
                is.close();
                printWriter.close();
                input.close();
                out.close();
                socket.close();
                MainActivity.aplikacija.namestiProgres();
                return true;
            }
            int filesize = odgovor.size;
            String imeBaze;
            if (baza == 'S')
                imeBaze = "Strukture_" + odgovor.dbVer + ".db";
            else if (baza == 'R')
                imeBaze = "Red_Voznje" + odgovor.dbVer + ".db";
            else
                imeBaze = "Putanje_" + odgovor.dbVer + ".db";

            File f = new File(BusDatabasesHelper.getDatabasePath() + imeBaze);
            f.getParentFile().mkdirs();
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(
                    BusDatabasesHelper.getDatabasePath() + imeBaze);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            byte[] mybytearray = new byte[filesize];

            while (current < filesize)
            {
                int preostalo = filesize - current;
                bytesRead = is.read(mybytearray, current, preostalo);
                current += bytesRead;

                if (bytesRead == -1)
                {
                    break;
                }

            }
            MainActivity.aplikacija.namestiProgres();
            if (bytesRead != -1)
            {
                bos.write(mybytearray, 0, current);
                b = true;
                if (file != null)
                    //   for (int i = 0; i < files.length; i++)
                    if (baza == file.charAt(0))
                    {
                        File ff = new File(BusDatabasesHelper.getDatabasePath() + file);
                        if (ff.exists())
                            ff.delete();
                    }
            } else
            {
                b = false;
                File ff = new File(BusDatabasesHelper.getDatabasePath() + imeBaze);
                if (ff.exists())
                    ff.delete();
            }

            bos.flush();
            bos.close();
            printWriter.close();
            out.close();
            input.close();
            socket.close();

        } catch (SocketTimeoutException e)
        {
            MainActivity.aplikacija.namestiProgres();
            b = true;
            e.printStackTrace();
        } catch (FileNotFoundException e)
        {
            MainActivity.aplikacija.namestiProgres();
            e.printStackTrace();
        } catch (UnknownHostException e)
        {
            b = true;
            MainActivity.aplikacija.namestiProgres();
            e.printStackTrace();
        } catch (IOException e)
        {
            b = true;
            MainActivity.aplikacija.namestiProgres();
            e.printStackTrace();
        }

        return b;
    }

    public Graf loadGraf()
    {
        BusDatabasesHelper busDatabasesHelper = BusDatabasesHelper.getInstance();
       /* String []files = busDatabasesHelper.checkDatabase();
        if(files == null)
            return false;

        String baza = files[0];*/
        Graf graf = null;
        String baza = busDatabasesHelper.checkDatabase('S');
        if (baza == null)
            return graf;
        //BusDatabasesHelper.setDbName(baza);
        // baza = busDatabasesHelper.getDatabasePath() + baza;

        String red_voznje = busDatabasesHelper.checkDatabase('R');
        String putanje = busDatabasesHelper.checkDatabase('P');

        File f = new File(BusDatabasesHelper.getDatabasePath() + baza);
        File f2 = new File(BusDatabasesHelper.getDatabasePath() + red_voznje);
        File f3 = new File(BusDatabasesHelper.getDatabasePath() + putanje);

        try
        {
            graf = new Graf(baza, red_voznje, putanje);
        } catch (Exception e)
        {
            e.printStackTrace();

            File[] fajlovi = (new File(BusDatabasesHelper.getDatabasePath())).listFiles();
            for (int i = 0; i < fajlovi.length; i++)
            {
                fajlovi[i].delete();
            }
            return null;
        }

        return graf;
    }

    public Response ObicanRedVoznje(LatLng latLng, int linija_id)
    {
        Response odgovor = null;
        Request request = new Request(new Integer(3), new Double(latLng.latitude),
                new Double(latLng.longitude), null, null, linija_id, null,null);
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
                input.close();
                out.close();
                socket.close();
                return null;
            }

            Gson gson = new GsonBuilder().create();
            odgovor = gson.fromJson(poruka, Response.class);
            is.close();
            printWriter.close();
            input.close();
            out.close();
            socket.close();
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (odgovor == null)
            odgovor = offline.handleRequest3(request);

        return odgovor;
    }

    public ArrayList<String> vremenaPolaska(Response odgovor)
    {
        List<List<Integer>> vremena = new ArrayList<List<Integer>>(odgovor.linije.length);
        ArrayList<String> povratniString = new ArrayList<>();

        for (int i = 0; i < odgovor.linije.length; i++)
            vremena.add(MainActivity.graf.getGl().linije[odgovor.linije[i]].
                    getVremena(odgovor.korekcije[i * odgovor.korekcije.length / odgovor.linije.length]));


        int pocetak = 0;
        int kraj;
        while (pocetak <= odgovor.stanice.length - 1)
        {
            kraj = pocetak;
            for (int i = pocetak; i < odgovor.stanice.length - 1; i++)
                if (odgovor.stanice[i].intValue() == odgovor.stanice[i + 1].intValue())
                    kraj = i + 1;
                else
                    break;

            String s = "";

            for (int j = pocetak; j <= kraj; j++)
            {

                while (!vremena.get(j).isEmpty())
                {
                    int niz[] = new int[kraj - pocetak + 1];
                    for (int i = pocetak; i <= kraj; i++)
                    {
                        if (vremena.get(i).size() > 0)
                        {
                            niz[i - pocetak] = vremena.get(i).get(0);
                        } else
                            niz[i - pocetak] = -1;
                    }

                    int min = 100000;
                    int indeks = 0;
                    for (int k = 0; k < niz.length; k++)
                        if (min > niz[k] && (niz[k] != -1))
                        {
                            min = niz[k];
                            indeks = k;

                        }
                    String sati;
                    if (((min / 100) % 24) < 10)
                        sati = "0" + (min / 100) % 24;
                    else
                        sati = "" + (min / 100) % 24;

                    if ((min % 100) >= 10)
                        s += sati + ":" + min % 100;
                    else
                        s += sati + ":0" + min % 100;
                    for (int l = 0; l < indeks + pocetak; l++)
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

    public ArrayList<String> vremenaPolaska(Response odgovor, int size)
    {
        List<List<Integer>> vremena = new ArrayList<List<Integer>>(odgovor.linije.length);
        ArrayList<String> povratniString = new ArrayList<>();

        //int dodatak = (int) OfflineRezim.calcDistance()

        for (int i = 0; i < odgovor.linije.length; i++)
            vremena.add(MainActivity.graf.getGl().linije[odgovor.linije[i]].getVremena(odgovor.korekcije[i], size));



        int pocetak = 0;
        int kraj;
        while (pocetak < odgovor.linije.length - 1)
        {
            kraj = pocetak;
            for (int i = pocetak; i < odgovor.linije.length - 1; i++)
                    if (GradskeLinije.istaOsnovna(odgovor.linije[i], odgovor.linije[i + 1]))
                        kraj++;
                    else
                        break;

            String s = "";

            for (int j = pocetak; j <= kraj; j++)
            {

                while (!vremena.get(j).isEmpty())
                {
                    int niz[] = new int[kraj - pocetak + 1];
                    for (int i = pocetak; i <= kraj; i++)
                    {
                        if (vremena.get(i).size() > 0)
                        {
                            niz[i - pocetak] = vremena.get(i).get(0);
                        } else
                            niz[i - pocetak] = -1;
                    }

                    int min = 100000;
                    int indeks = 0;
                    for (int k = 0; k < niz.length; k++)
                        if (min > niz[k] && (niz[k] != -1))
                        {
                            min = niz[k];
                            indeks = k;

                        }
                    String sati;
                    if (((min / 100) % 24) < 10)
                        sati = "0" + (min / 100) % 24;
                    else
                        sati = "" + (min / 100) % 24;

                    if ((min % 100) >= 10)
                        s += sati + ":" + min % 100;
                    else
                        s += sati + ":0" + min % 100;
                    for (int l = 0; l < indeks; l++)
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


    public ArrayList<String> vremenaDolaska(Response odgovor, ArrayList<String> vremenaPolaska)
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

        Calendar calendar = Calendar.getInstance();
        int trenutniSati = calendar.get(Calendar.HOUR_OF_DAY);

        int size = vremenaPolaska.size();
        for (int i = 0; i < size; i++)
            if (!vremenaPolaska.get(i).equals(""))
            {
                StringTokenizer tokenizer = new StringTokenizer(vremenaPolaska.get(i), "\n");
                String s = "";
                boolean b = true;
                while (tokenizer.hasMoreTokens())
                {

                    String token = tokenizer.nextToken();
                    int sati, minuti, korekcija_id;
                    sati = Integer.parseInt(token.substring(0, 2));
                    minuti = Integer.parseInt(token.substring(3, 5));
                    korekcija_id = token.length() - 5;
                    if ((trenutniSati != sati) && (b))
                    {
                        trenutniSati = sati;
                        b = false;
                    } else
                        b = false;



                    int korekcijaIndex = korekcija_id * odgovor.korekcije.length / odgovor.linije.length;


                    if ((sati - trenutniSati) < 0)
                    {
                        sati += sati_korekcija[korekcijaIndex + sati - trenutniSati + 24];
                        minuti += minuti_korekcija[korekcijaIndex + sati - trenutniSati + 24];
                    }
                    else
                    {
                        sati += sati_korekcija[korekcijaIndex + sati - trenutniSati];
                        minuti += minuti_korekcija[korekcijaIndex + sati - trenutniSati];
                    }
                    while (minuti > 59)
                    {
                        sati++;
                        minuti -= 60;
                    }
                    String sSati;
                    if (sati >= 10)
                        sSati = sati % 24 + "";
                    else
                        sSati = "0" + sati;
                    if (minuti >= 10)
                        s += sSati + ":" + minuti;
                    else
                        s += sSati + ":0" + minuti;
                    for (int l = 0; l < korekcija_id; l++)
                        s += "*";
                    s += "\n";
                }
                povratniString.add(s);
            } else
                povratniString.add("");


        return povratniString;
    }

    public Response napredniRezim(LatLng source, LatLng destination)
    {
        Response odgovor = null;
        Request request = new Request(Constants.mode, source.latitude, source.longitude,
                destination.latitude, destination.longitude, null, null,null);
        if (Constants.numberTokens < 1)
        {
            odgovor = new Response(-1,null,null,null,null,null,null,null);
            return odgovor;
        }

        try
        {
            String poruka = request.toString();
            InetAddress inetAddress = InetAddress.getByName(Constants.IP);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(inetAddress, Constants.PORT), Constants.TIMEOUT);

            InputStream is = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            PrintWriter printWriter = new PrintWriter(out);

            printWriter.print(poruka + "\n");
            printWriter.flush();

            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            poruka = input.readLine();
            Gson gson = new GsonBuilder().create();
            odgovor = gson.fromJson(poruka, Response.class);
            is.close();
            printWriter.close();
            out.close();
            input.close();
            socket.close();
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (odgovor == null)
            switch (Constants.mode)
            {
                case 4:
                    odgovor = offline.handleRequest4(request);
                    break;
                case 6:
                    odgovor = offline.handleRequest6(request, Constants.brzinaPesaka);
                    break;
                case 5:
                    odgovor = offline.handleRequest5(request, Constants.brzinaPesaka);
                    break;
                case 7:
                    odgovor = offline.handleRequest6(request, Constants.brzinaPesakaZaMinWalk);
                    break;
            }
        if (odgovor != null)
        {
            SharedPreferences.Editor editor = MainActivity.preferences.edit();
            Constants.numberTokens--;
            editor.putInt("tokens", Constants.numberTokens);
            editor.commit();
        }
        return odgovor;

    }

    private ArrayList<Cvor> getNajblizeStanice(String linija, String smer, LatLng source,Integer linijaId)
    {

        Linija [] linije = MainActivity.graf.getGl().linije;
        BusDBAdapter.setPolilinije();
        int minUdaljenost = 1000000000;
        ArrayList<Cvor> povratak = null;
        for (int i = 0; i < linije.length; i++)
        {
            if (linije[i] != null)
                if ((linija.equals(linije[i].broj.replace("*", ""))) && (smer.equals(linije[i].smer)))
                {
                    Linija l = linije[i];

                    Cvor c = l.pocetnaStanica;

                    //cvorovi.add(c);
                    Cvor pocetna = c;
                    Veza v = null;


                    while ((v = c.vratiVezu(l)) != null)
                    {
                       // Log.e("provera", c.naziv);
                        if ((c.status == Constants.CVOR_OBRADJEN)
                                && (v.destination.status == Constants.CVOR_OBRADJEN))
                        {
                            c = v.destination;
                            if (c == pocetna)
                                break;
                        }
                        else
                        {
                            LatLngBounds.Builder builder = LatLngBounds.builder();
                                    //.include(new LatLng(c.lat, c.lon))
                                  //  .include(new LatLng(v.destination.lat, v.destination.lon));

                            boolean b = false;

                            DirectionsHelper directionsHelper = new DirectionsHelper
                                    (new LatLng(c.lat,c.lon), new LatLng(v.destination.lat,v.destination.lon));
                            List<LatLng> lista = directionsHelper.preurediTacke(Constants.udaljenostPesacenje / 5,v.putanje);

                            int size = lista.size();

                            for (int j = 0; j < size; j++)
                            {
                                builder.include(lista.get(j));
                            }

                            LatLngBounds granice = builder.build();

                            if (granice.contains(source))
                            {
                                ArrayList<Cvor> temp = new ArrayList<>();
                                for (int j = 0; j < size; j++)
                                {
                                    //builder.include(v.putanje.get(j));
                                    double minimum = offline.calcDistance(source.latitude,
                                            source.longitude,lista.get(j).latitude,
                                            lista.get(j).longitude);

                                    if (minUdaljenost > minimum)
                                    {
                                        b = true;
                                        minUdaljenost = (int) minimum;
                                    }
                                }
                                if (b)
                                {
                                    //minUdaljenost = odPrve + doDruge;
                                    temp.add(c);
                                    temp.add(v.destination);
                                    povratak = temp;
                                   // Log.e("0. stanica", temp.get(0).naziv);
                                   // Log.e("1. stanica", temp.get(1).naziv);
                                 //   Log.e("lokacija", Glavna_Aktivnost.MyLocation.toString());

                                }
                                linijaId = i;

                            }
                            c.status = Constants.CVOR_OBRADJEN;
                            c = v.destination;
                            c.status = Constants.CVOR_OBRADJEN;
                            if (c == pocetna)
                                break;
                        }
                    }
                }
        }
        return povratak;
    }

    public void sendInfo(String linija, String smer, int guzva, int klimatizovanost,
                                LatLng source, boolean kontrola, String komentar)
    {
        Integer linijaId = 0;
        ArrayList<Cvor> stanice = getNajblizeStanice(linija,smer,source,linijaId);


        final String sekvenca;
        if(stanice != null)
        {
            int udaljenost = izracunajUdaljenost(stanice,source);

            sekvenca = stanice.get(0).naziv + "\n" + stanice.get(1).naziv + "\n" + udaljenost;

            CSInfo crowdSource = new CSInfo(source.latitude, source.longitude,guzva, klimatizovanost,
                    linija, smer, stanice.get(0).id, udaljenost, komentar,kontrola);
            Request request = new Request(10,null,null,null,null,null,null,crowdSource);

            try
            {

                String poruka = request.toString();
                InetAddress inetAddress = InetAddress.getByName(Constants.IP);
//            Socket socket = new Socket(inetAddress, Constants.PORT);
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(inetAddress, Constants.PORT), Constants.TIMEOUT);
                OutputStream out = socket.getOutputStream();
                PrintWriter printWriter = new PrintWriter(out);


                printWriter.print(poruka + "\n");
                printWriter.flush();



                printWriter.close();
                out.close();
                socket.close();

                SharedPreferences.Editor editor = MainActivity.preferences.edit();
                Constants.numberTokens += 3;
                editor.putInt("tokens", Constants.numberTokens);
                editor.commit();

            } catch (UnknownHostException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        else
        {
            sekvenca = "Nema tih stanica";
            //Log.e("stanice", "nema");
            //Toast.makeText(Glavna_Aktivnost.otac,"Nema tih stanica",Toast.LENGTH_LONG).show();
        }



        Glavna_Aktivnost.UIHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(Glavna_Aktivnost.otac,sekvenca,Toast.LENGTH_LONG).show();
            }
        });



        MainActivity.graf.resetujCvorove();
    }

    private int izracunajUdaljenost(ArrayList<Cvor> stanice, LatLng source)
    {
        int izmedjuStanica,odPrve,doDruge;

        izmedjuStanica = 0;

        Cvor c = stanice.get(0);
        Veza v = null;

        int size = c.veze.size();


        for(int i = 0; i < size; ++i)
        {
            v = c.veze.get(i);
            if (v.destination == stanice.get(1))
            {
                izmedjuStanica = v.weight;
                break;
            }
        }

        odPrve = (int) offline.calcDistance(stanice.get(0),source.latitude,source.longitude);
        doDruge = (int) offline.calcDistance(stanice.get(1),source.latitude,source.longitude);

        double procenat = ((double) odPrve) / (odPrve + doDruge);



        return (int) (procenat * izmedjuStanica);
    }

    public Response zatraziKontrole()
    {
        Response odgovor = null;
        Request request = new Request(new Integer(11), null,
                null, null, null, null, null,null);
        try
        {
            String poruka = request.toString();
            InetAddress inetAddress = InetAddress.getByName(Constants.IP);
//            Socket socket = new Socket(inetAddress, Constants.PORT);
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(inetAddress, Constants.PORT), Constants.TIMEOUT);

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
                input.close();
                out.close();
                socket.close();
                return null;
            }

            Gson gson = new GsonBuilder().create();
            odgovor = gson.fromJson(poruka, Response.class);
            is.close();
            printWriter.close();
            input.close();
            out.close();
            socket.close();
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }


        return odgovor;
    }
}