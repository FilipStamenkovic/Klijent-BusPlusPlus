package strukture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import rs.mosis.diplomski.bus.Constants;
import rs.mosis.diplomski.bus.MainActivity;
import rs.mosis.diplomski.bus.Request;
import rs.mosis.diplomski.bus.Response;

/**
 * Created by filip on 9/24/15.
 */
public class OfflineRezim
{
    public OfflineRezim(){}
    public Response handleRequest3(Request req)
    {
        Linija linije[] = MainActivity.graf.getGl().linije;
        //ArrayList<CSInfo> targetCrowdInfo = new ArrayList<>();
        //Graf g = owner.getGraf();
        //ArrayList<Cvor> stanice = g.getStanice();
        ArrayList<Linija> targetLinije = new ArrayList<>();

        if(req.linija<1 || req.linija>=linije.length)
            return null;

        String brojLinije = linije[req.linija].broj.replace("*", "");
        String smer = linije[req.linija].smer;

        Linija l = null;
        for(int i = 0; i < linije.length; ++i)
        {
            if(linije[i] == null)
                continue;

            if(brojLinije.equals(linije[i].broj.replace("*", "")) && smer.equalsIgnoreCase(linije[i].smer))
                targetLinije.add(linije[i]);
        }

        //Integer minDistance = Integer.MAX_VALUE;
        Double minDistance[] = new Double[targetLinije.size()];
        Cvor minDistanceStanice[] = new Cvor[targetLinije.size()];

        Integer linijeId[] = new Integer[targetLinije.size()];
        Integer staniceId[] = new Integer[targetLinije.size()];
        //Integer korekcije[] = new Integer[targetLinije.size()];
        ArrayList<Integer> targetKorekcije = new ArrayList<>();
        int korekcija = 0;

        for(int i = 0; i < targetLinije.size(); ++i)
        {
            minDistance[i] = Double.MAX_VALUE;
            Double d;

            l = targetLinije.get(i);

            linijeId[i] = l.id;

            int predjeniPutBusa = 0;
            int predjeniPutBusaDoNajblizeStanice = 0;

            Cvor c = l.pocetnaStanica;
            Cvor pocetna = c;
            Veza v = null;

            if((d = calcDistance(req.srcLat, req.srcLon, c.lat, c.lon)) < minDistance[i])
            {
                minDistance[i] = d;
                minDistanceStanice[i] = c;
            }

            while((v = c.vratiVezu(l)) != null)
            {
                c = v.destination;
                predjeniPutBusa += v.weight;
                if(c == pocetna)
                    break;

                if((d = calcDistance(req.srcLat, req.srcLon, c.lat, c.lon)) < minDistance[i])
                {
                    minDistance[i] = d;
                    minDistanceStanice[i] = c;
                    predjeniPutBusaDoNajblizeStanice = predjeniPutBusa;
                }
            }

            staniceId[i] = minDistanceStanice[i].id;

            korekcija = izracunajKorekciju(l, minDistanceStanice[i], predjeniPutBusaDoNajblizeStanice);

            Calendar calendar = Calendar.getInstance();
            int saaati = calendar.get(Calendar.HOUR_OF_DAY);

            for(int k = saaati; k < 27; ++k)
            {
                targetKorekcije.add(izracunajKorekcijuZaCas(l, minDistanceStanice[i], predjeniPutBusaDoNajblizeStanice, k%24));
            }

        }

       Response response = new Response(req.type, staniceId, linijeId, targetKorekcije.toArray(new Integer[targetKorekcije.size()]), null, null, null);

        return  response;

    }

    private int izracunajKorekcijuZaCas(Linija linija, Cvor stanica, int predjeniPut, int cas)
    {
        return (int) (predjeniPut/this.brzinaAutobusaZaCas(linija, cas));
    }

    public static double calcDistance(Cvor cvor1, Cvor cvor2)
    {
        if(cvor1.id > 0 && cvor2.id > 0)
            return MainActivity.graf.matricaUdaljenosti[cvor1.id][cvor2.id];
        else
            return calcDistance(cvor1.lat, cvor1.lon, cvor2.lat, cvor2.lon);
    }

    public static double calcDistance(Cvor stanica, double lat2, double long2)
    {
        double a, c;

        a = Math.sin((lat2 - stanica.lat)*Math.PI/360) * Math.sin((lat2 - stanica.lat)*Math.PI/360) +
                Math.sin((long2 - stanica.lon)*Math.PI/360) * Math.sin((long2 - stanica.lon)*Math.PI/360) * Math.cos(lat2 * Math.PI/180) * Math.cos(stanica.lat * Math.PI/180);

        c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371000 * c;
    }

    public static double calcDistance(double lat1, double long1, double lat2, double long2)
    {
        double a, c;

        a = Math.sin((lat2 - lat1)*Math.PI/360) * Math.sin((lat2 - lat1)*Math.PI/360) +
                Math.sin((long2 - long1)*Math.PI/360) * Math.sin((long2 - long1)*Math.PI/360) * Math.cos(lat2 * Math.PI/180) * Math.cos(lat1 * Math.PI/180);

        c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371000 * c;
    }

    private int izracunajKorekciju(Linija linija, Cvor stanica, int predjeniPut)
    {
        return (int) (predjeniPut/this.brzinaAutobusa(linija));
    }

    private double brzinaAutobusa(Linija linija)
    {
        if(linija == null)
            return Constants.brzinaPesaka;
        else
            return linija.vratiTrenutnuBrzinu();
    }

    private double brzinaAutobusaZaCas(Linija linija, int cas)
    {
        if(linija == null)
            return Constants.brzinaPesaka;
        else
            return linija.vratiBrzinu(cas);
    }

    private int izracunajKorekciju(Linija linija, Cvor targetStanica)
    {
        int predjeniPut = 0;

        if(linija == null || targetStanica==null)
            return Integer.MAX_VALUE;

        if(linija.pocetnaStanica == targetStanica)
            return izracunajKorekciju(linija, targetStanica, 0);

        Cvor pocetnaStanica = linija.pocetnaStanica;
        Cvor tempStanica = pocetnaStanica;
        Veza v = null;

        while((v = tempStanica.vratiVezu(linija)) != null)
        {
            predjeniPut += v.weight;

            if((v.destination == pocetnaStanica) || (v.destination == targetStanica))
                break;

            tempStanica = v.destination;
        }

        return izracunajKorekciju(linija, targetStanica, predjeniPut);
    }
    public Response handleRequest4(Request req)
    {
        Graf g = MainActivity.graf;
        Cvor stanice[] = g.getStanice().toArray(new Cvor[g.getStanice().size()]);
        Cvor sourceCvor = null;
        Linija linije[] = g.getGl().linije;
        double pesacenje[] = new double[stanice.length];
        Double minimalnoPesacenje = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon);
        Integer responseStanice[] = new Integer[2]; //responseStanice[0] najbliza source stanica, responseStanice[1] najbliza destination stanica
        ArrayList<Integer> responseLinije = null;

        for(int i = 0; i < stanice.length; ++i)
        {
            pesacenje[i] = calcDistance(stanice[i], req.srcLat, req.srcLon);
        }

        for(int i = 1; i < stanice.length; ++i)
        {
            double key = pesacenje[i];
            Cvor pomC = stanice[i];
            int j = i-1;
            while((j >= 0) && (pesacenje[j] > key))
            {
                pesacenje[j+1] = pesacenje[j];
                stanice[j+1] = stanice[j];
                --j;
            }
            pesacenje[j+1] = key;
            stanice[j+1] = pomC;
        }

        for(int i = 0; i<stanice.length && minimalnoPesacenje>pesacenje[i]; ++i)
        {
            for(int j = 0; j < stanice[i].veze.size(); ++j)
            {
                Cvor c = stanice[i];
                Veza v = c.veze.get(j);
                Linija l = v.linija;
                Double d;
                Cvor destStanica = c;
                Double destPesacenje = calcDistance(c, req.destLat, req.destLon);

                //System.out.println("LINIJA = " + l);

                while((v = c.vratiVezu(l)) != null)
                {
                    c = v.destination;

                    if(c == l.pocetnaStanica)
                        break;

                    if(destPesacenje > (d = calcDistance(c, req.destLat, req.destLon)))
                    {
                        destStanica = c;
                        destPesacenje = d;
                    }
                }

                if(destPesacenje + pesacenje[i] < minimalnoPesacenje)
                {
                    minimalnoPesacenje = destPesacenje + pesacenje[i];
                    sourceCvor = stanice[i];
                    responseStanice[0] = stanice[i].id;
                    responseStanice[1] = destStanica.id;
                    responseLinije = new ArrayList<>();
                    responseLinije.add(l.id);
                } else if(destPesacenje + pesacenje[i] == minimalnoPesacenje)
                {
                    responseLinije.add(l.id);
                }

            }
        }

        Integer responseKorekcije[] = new Integer[responseLinije.size()];
        Integer responseLinijeArray[] = (Integer[]) responseLinije.toArray(new Integer[responseLinije.size()]);

        for(int i = 0; i < responseKorekcije.length; ++i)
        {
            responseKorekcije[i] = izracunajKorekciju(linije[responseLinijeArray[i]], sourceCvor);
        }

        Response response = new Response(req.type, responseStanice, responseLinijeArray, responseKorekcije, null, null, null);

        return response;
    }

    private void izracunajPrioriteteLinija(GradskeLinije gradskeLinije)
    {
        Veza v = null;
        Cvor c = null;
        double minPrioritet = Double.MAX_VALUE;

        for(Linija l : gradskeLinije.linije)
            if(l != null)
            {
                c = l.pocetnaStanica;
                l.prioritet = c.heuristika;

                while((v = c.vratiVezu(l)) != null)
                {
                    c = v.destination;

                    if(c == l.pocetnaStanica)
                        break;

                    if(c.heuristika < l.prioritet)
                    {
                        l.prioritet = c.heuristika;
                        if(l.prioritet < minPrioritet)
                            minPrioritet = l.prioritet;
                    }
                }
            }

        for(Linija l : gradskeLinije.linije)
            if(l != null)
                l.prioritet /= minPrioritet;
    }

    public Response handleRequest6(Request req, double brzinaPesacenja)
    {
        boolean nadjenPut = false;
        Calendar currentTime = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        Calendar tempTime = Calendar.getInstance();
        Response response = null;
        ;

        /*calendar.set(Calendar.HOUR_OF_DAY,14);
        calendar.set(Calendar.MINUTE,14);
        currentTime.set(Calendar.HOUR_OF_DAY,14);
        currentTime.set(Calendar.MINUTE,14);
        tempTime.set(Calendar.HOUR_OF_DAY,14);
        tempTime.set(Calendar.MINUTE,14);*/

        //napravi pseudo Stanice start i end
        Cvor pseudoStart = new Cvor(-1, "pseudoStart", req.srcLat, req.srcLon);
        Cvor pseudoEnd = new Cvor(-2, "pseudoEnd", req.destLat, req.destLon);

        pseudoEnd.heuristika = 0.0;
        pseudoEnd.linijom = null;
        pseudoEnd.prethodnaStanica = pseudoStart;
        pseudoEnd.cenaPutanje = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaPesacenja;
        pseudoStart.heuristika = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/Constants.brzinaAutobusa;
        pseudoStart.linijom = null;
        pseudoStart.prethodnaStanica = null;
        pseudoStart.cenaPutanje = 0.0;

        Graf graf = MainActivity.graf;

        graf.resetujCvorove();

        Double [][] matricaUdaljenosti = null;
        if (graf.inicijalizujMatricu())
            matricaUdaljenosti = graf.matricaUdaljenosti;

        //izvuci sve stanice u niz
        Cvor stanice[] = graf.getStanice().toArray(new Cvor[graf.getStanice().size()]);

        PrioritetnaListaCvorova lista = new PrioritetnaListaCvorova();

        //izracunaj heuristike
        for(int i = 0; i < stanice.length; ++i)
        {
            if(stanice[i] != null)
            {
                stanice[i].heuristika = calcDistance(stanice[i], req.destLat, req.destLon)/Constants.brzinaAutobusa;
                stanice[i].cenaPutanje = calcDistance(stanice[i], req.srcLat, req.srcLon)/brzinaPesacenja;

                stanice[i].linijom = null;
                stanice[i].prethodnaStanica = pseudoStart;

                stanice[i].status = Constants.CVOR_SMESTEN;
            }

            lista.pushPriority(stanice[i]);
        }

        pseudoStart.status = Constants.CVOR_OBRADJEN;
        pseudoEnd.status = Constants.CVOR_SMESTEN;
        lista.pushPriority(pseudoEnd); //dodaj i pseudoEnd u priority list

        //izracunaj prioritete linija (koliko linija vodi blizu cilju) ako je zahtevan min walk
        //ovaj korak mora posle racunanja heuristike, jer pretpostavlja da je heuristika izracunata za svaki cvor
        if(req.type == 7)
            izracunajPrioriteteLinija(graf.getGl());


        Cvor radniCvor = null;
        Cvor tempCvor = null;
        ArrayList<Veza> potomciVeze = null;
        Veza v = null;
        Linija l = null;
        long kasnjenje = 0;

        while(!lista.isEmpty())
        {
            radniCvor = lista.remove(0);

            radniCvor.status = Constants.CVOR_OBRADJEN;

            if(radniCvor == pseudoEnd)
            {
                nadjenPut = true;
                break;
            }

            //pokupi decu cvorove i update statistike
            potomciVeze = radniCvor.veze;
            for(int i = 0; i < potomciVeze.size(); ++i)
            {
                v = potomciVeze.get(i);
                tempCvor = v.destination;

                if(tempCvor.status == Constants.CVOR_OBRADJEN)
                    continue;

                double brzinaAutobusa = this.brzinaAutobusa(v.linija);

                if(v.linija == radniCvor.linijom)
                {
                    if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa)
                    {
                        lista.remove(tempCvor);
                        tempCvor.linijom = v.linija;
                        tempCvor.prethodnaStanica = radniCvor;
                        tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa;
                        tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = null;
                        lista.pushPriority(tempCvor);
                    }
                } else
                {
					/*if(req.type == 7)
						kasnjenje = 10;		//za min_walk je kasnjenje konstanta
					else
						kasnjenje = izracunajKasnjenjeLinije2(v.linija, radniCvor, brzinaAutobusa);*/

                    if(req.type == 7)
                        kasnjenje = (long) v.linija.prioritet;		//za min_walk se koristi prioritet linije
                    else
                        kasnjenje = izracunajKasnjenjeLinije2(v.linija, radniCvor/*, brzinaAutobusa*/);

                    if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa + kasnjenje)
                    {
                        lista.remove(tempCvor);
                        tempCvor.linijom = v.linija;
                        tempCvor.prethodnaStanica = radniCvor;
                        tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa + kasnjenje;                        if (req.type == 6) //ako je MIN_WALK ne racunaj vremena dolaska autobusa na stanicu
                        {

                       /* tempTime = currentTime.plusDays(((long) radniCvor.cenaPutanje + kasnjenje) / 86400);
                        tempTime = tempTime.plusHours((((long) radniCvor.cenaPutanje + kasnjenje) / 3600) % 24);
                        tempTime = tempTime.plusMinutes((((long) radniCvor.cenaPutanje + kasnjenje) / 60) % 60);
                        tempTime = tempTime.plusSeconds(((long)radniCvor.cenaPutanje + kasnjenje)%60);
                        DatumVremeStanica vremeDolaska = new DatumVremeStanica(radniCvor.id, v.linija.id,
                                tempTime.getSecond(), tempTime.getMinute(), tempTime.getHour(),
                                tempTime.getDayOfWeek().getValue(),
                                tempTime.getMonthValue(), tempTime.getYear());*/
                            tempTime = Calendar.getInstance();
                            //tempTime.set(Calendar.HOUR_OF_DAY,14);
                            //tempTime.set(Calendar.MINUTE,14);
                            tempTime.add(Calendar.DAY_OF_WEEK, (int) (radniCvor.cenaPutanje + kasnjenje) / 86400);
                            tempTime.add(Calendar.HOUR_OF_DAY, (int) ((radniCvor.cenaPutanje + kasnjenje) / 3600) % 24);
                            tempTime.add(Calendar.MINUTE, (int) ((radniCvor.cenaPutanje + kasnjenje) / 60) % 60);
                            tempTime.add(Calendar.SECOND, (int) ((radniCvor.cenaPutanje + kasnjenje) % 60));

                            DatumVremeStanica vremeDolaska = new DatumVremeStanica(radniCvor.id, v.linija.id,
                                    tempTime.get(Calendar.SECOND), tempTime.get(Calendar.MINUTE),
                                    tempTime.get(Calendar.HOUR_OF_DAY),
                                    tempTime.get(Calendar.DAY_OF_WEEK),
                                    tempTime.get(Calendar.MONTH), tempTime.get(Calendar.YEAR), null);

                            tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = vremeDolaska;
                        } else
                            tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = null;

                        lista.pushPriority(tempCvor);
                    }
                }
            }

                //obradi pesacenje do svih stanica
            for(int j = 0; j < stanice.length; ++j)
            {
                if(stanice[j] != radniCvor && stanice[j].status != Constants.CVOR_OBRADJEN)
                {
                    double udaljenost = matricaUdaljenosti[radniCvor.id][stanice[j].id];

                    //ovo je korekcija za MIN_WALK (treba, popraviti)
                    if(req.type == 7 && radniCvor.linijom != null)
                        udaljenost += 150;


                    if(stanice[j].cenaPutanje > radniCvor.cenaPutanje + udaljenost/brzinaPesacenja)
                    {
                        lista.remove(stanice[j]);
                        stanice[j].linijom = null;
                        stanice[j].prethodnaStanica = radniCvor;
                        stanice[j].cenaPutanje = radniCvor.cenaPutanje + udaljenost/brzinaPesacenja;
                        stanice[j].vremeDolaskaAutobusaNaPrethodnuStanicu = null;
                        lista.pushPriority(stanice[j]);
                    }
                }
            }
            //pesacenje do cilja jer on nije u nizu stanice[]
            double udaljenost = calcDistance(radniCvor, pseudoEnd.lat, pseudoEnd.lon);
            if(pseudoEnd.cenaPutanje > radniCvor.cenaPutanje + udaljenost/brzinaPesacenja)
            {
                lista.remove(pseudoEnd);
                pseudoEnd.linijom = null;
                pseudoEnd.prethodnaStanica = radniCvor;
                pseudoEnd.cenaPutanje = radniCvor.cenaPutanje + udaljenost/brzinaPesacenja;
                lista.pushPriority(pseudoEnd);
            }
        }

        if(nadjenPut)
        {
			/*if(req.type == 7)
				minWalkPostProcessing(pseudoStart, pseudoEnd);*/

			/*if(req.type == 6)
				optimalPostProcessing(pseudoStart, pseudoEnd);*/

            Cvor c = pseudoEnd;

            response = new Response();
            response.type = req.type;

            if(req.type == 6)
                response.size = (int) pseudoEnd.cenaPutanje;	//procenjena cena putovanja

            ArrayList<Integer> responseStanice = new ArrayList<>();
            ArrayList<Integer> responseLinije = new ArrayList<>();
            ArrayList<DatumVremeStanica> responseVremenaDolaska = new ArrayList<>();

            while(c != null)
            {
                responseStanice.add(c.id);

                if(c.linijom != null)
                    responseLinije.add(c.linijom.id);
                else
                    responseLinije.add(null);

                if(c.vremeDolaskaAutobusaNaPrethodnuStanicu != null)
                    responseVremenaDolaska.add(c.vremeDolaskaAutobusaNaPrethodnuStanicu);

                //moze da se doda u response i Estimated Time of Arrival (dodao sam ga u Response.size)
                c = c.prethodnaStanica;
            }

            response.stanice = new Integer[responseStanice.size()];
            response.linije = new Integer[responseLinije.size()];
            response.vremenaDolaska = new ArrayList<>();

            int arraySize = responseStanice.size();
            for(int i = arraySize-1; i >= 0; --i)
            {
                response.stanice[arraySize-1-i] = responseStanice.get(i);
                response.linije[arraySize-1-i] = responseLinije.get(i);
            }

            arraySize = responseVremenaDolaska.size();
            if(arraySize > 0)
            {
                for(int i = arraySize-1; i >= 0; --i)
                    response.vremenaDolaska.add(responseVremenaDolaska.get(i));
            }
            else
                response.vremenaDolaska = null;

        }

        return  response;

    }

    private void minWalkPostProcessing(Cvor pseudoStart, Cvor pseudoEnd)
    {
        double koeficijentUbrzanjaBuseva = 1.0;
        ArrayList<Cvor> putanja = new ArrayList<>();

        Cvor c = pseudoEnd;
        while(c != null)
        {
            putanja.add(0, c);

            c = c.prethodnaStanica;
        }

        double akumuliranaCena = 0.0;

        for(int i = 1; i < putanja.size()-1; ++i)
        {
            c = putanja.get(i);

            if(i > 1)
            {
                if(c.linijom != null)
                    akumuliranaCena += c.prethodnaStanica.vratiVezu(c.linijom).weight/(koeficijentUbrzanjaBuseva * Constants.brzinaAutobusa);
                else
                    akumuliranaCena += calcDistance(c.prethodnaStanica, c)/Constants.brzinaPesaka;

                c.cenaPutanje = c.heuristika*Constants.brzinaAutobusa/Constants.brzinaPesaka
                        + akumuliranaCena;
            }
            else
                c.cenaPutanje = c.heuristika*Constants.brzinaAutobusa/Constants.brzinaPesaka;
        }

        double minCena = Double.MAX_VALUE;
        int minI = -1;

        for(int i = 1; i < putanja.size()-1; ++i)
        {
            c = putanja.get(i);

            if(minCena > c.cenaPutanje)
            {
                minCena = c.cenaPutanje;
                minI = i;
            }
        }

        if(minI != -1)
            pseudoEnd.prethodnaStanica = putanja.get(minI);

        //---------------------------------obradjen je kraj puta

        ArrayList<Cvor> obradjenaPutanja = new ArrayList<>();
        ArrayList<Linija> obradjeneLinije = new ArrayList<>();

        c = pseudoEnd;
        while(c != null)
        {
            c.heuristika = calcDistance(pseudoStart.lat, pseudoStart.lon, c.lat, c.lon)/Constants.brzinaPesaka;

            obradjenaPutanja.add(0, c);
            obradjeneLinije.add(0, c.linijom);

            c = c.prethodnaStanica;
        }

        Linija l;
        akumuliranaCena = 0.0;
        //ovo ovde je jedan veliki znak pitanja :D
        for(int i = 1; i < obradjenaPutanja.size()-1; ++i)
        {
            c = obradjenaPutanja.get(i);
            l = obradjeneLinije.get(i+1);

            if(l != null)
                akumuliranaCena += c.vratiVezu(l).weight/Constants.brzinaAutobusa;
            else
                akumuliranaCena += calcDistance(c, obradjenaPutanja.get(i+1))/Constants.brzinaPesaka;

            c.cenaPutanje = c.heuristika - akumuliranaCena; //pogotovo minus :)
        }

        minCena = Double.MAX_VALUE;
        minI = -1;

        for(int i = 1; i < obradjenaPutanja.size()-1; ++i)
        {
            c = obradjenaPutanja.get(i);

            if(minCena > c.cenaPutanje)
            {
                minCena = c.cenaPutanje;
                minI = i;
            }
        }

        if(minI != -1)
        {
            c = obradjenaPutanja.get(minI).prethodnaStanica = pseudoStart;
            c.linijom = null;
        }

        //---------------------------obradjen pocetak puta

    }

    private long izracunajKasnjenjeLinije2(Linija l, Cvor c)
    {
        //double secondsToWaitForBus = 0.0;

        //LocalDateTime realLifeDateTime = LocalDateTime.now();
        Calendar targetDateTime = Calendar.getInstance();
        /*targetDateTime.set(Calendar.HOUR_OF_DAY,14);
        targetDateTime.set(Calendar.MINUTE,14);*/
        Date realLifeDate = new Date();
        //Date targetDate = null;
        //Date sourceDate = null;

        long realLifeSeconds = realLifeDate.getTime() / 1000;

        long busTravelSeconds = izracunajKorekciju(l, c);

        long futureShiftSeconds = (long) c.cenaPutanje;

        long cvorSeconds = realLifeSeconds + futureShiftSeconds;

        long sourceSeconds = cvorSeconds - busTravelSeconds;

        //targetDateTime = targetDateTime.plusSeconds(futureShiftSeconds);
        //targetDateTime = targetDateTime.minusSeconds(busTravelSeconds);
       // targetDateTime = targetDateTime.plusDays((futureShiftSeconds-busTravelSeconds)/86400);
       // targetDateTime = targetDateTime.plusHours(((futureShiftSeconds - busTravelSeconds) / 3600) % 24);
       // targetDateTime = targetDateTime.plusMinutes(((futureShiftSeconds - busTravelSeconds) / 60) % 60);
       // targetDateTime = targetDateTime.plusSeconds((futureShiftSeconds - busTravelSeconds) % 60);
        targetDateTime.add(Calendar.DAY_OF_YEAR, (int) ((futureShiftSeconds-busTravelSeconds)/86400));
        targetDateTime.add(Calendar.HOUR_OF_DAY, (int) ((futureShiftSeconds - busTravelSeconds) / 3600) % 24);
        targetDateTime.add(Calendar.MINUTE, (int) ((futureShiftSeconds - busTravelSeconds) / 60) % 60);
        targetDateTime.add(Calendar.SECOND, (int) ((futureShiftSeconds - busTravelSeconds) % 60));
        //sourceDate = new Date(sourceSeconds*1000);
        //targetDate = new Date(sourceSeconds*1000);

        //long bla = 0;
        long targetSeconds = sourceSeconds;
        //targetSeconds -= (targetDate.getMinutes()*60 + targetDate.getSeconds());
        targetSeconds -= (targetDateTime.get(Calendar.MINUTE)*60 + targetDateTime.get(Calendar.SECOND));

        int mat[][] = null;
        boolean found = false;


        if(targetDateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            mat = l.matSubota;
        else if(targetDateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            mat = l.matNedelja;
        else
            mat = l.matRadni;
       // targetDateTime = targetDateTime.minusMinutes(targetDateTime.getMinute());
        //targetDateTime = targetDateTime.minusSeconds(targetDateTime.getSecond());
        targetDateTime.add(Calendar.MINUTE, -targetDateTime.get(Calendar.MINUTE));
        targetDateTime.add(Calendar.SECOND, -targetDateTime.get(Calendar.SECOND));
        int i = 0, h = targetDateTime.get(Calendar.HOUR_OF_DAY);

       // Log.e("tag","USO" + targetDateTime.toString());
        while(!found)
        {
            if(mat[h][i] != -1)
            {
                if(targetSeconds + mat[h][i]*60 > sourceSeconds)
                {
                    found = true;
                    targetSeconds += mat[h][i]*60;
                   // targetDateTime = targetDateTime.plusMinutes(mat[h][i]);
                    targetDateTime.add(Calendar.MINUTE, mat[h][i]);

                }
            }
            else
            {
                targetSeconds += 3600;
                //targetDateTime = targetDateTime.plusHours(1);
                targetDateTime.add(Calendar.HOUR_OF_DAY, 1);
                //bla += 3600;
                ++h;
                if(h == 25)
                {
                    h = 0;
                    targetSeconds -= 3600;
                    //targetDateTime = targetDateTime.plusDays(1);
                    targetDateTime.add(Calendar.DATE, 1);
                    //targetDateTime = targetDateTime.minusHours(targetDateTime.getHour());
                    targetDateTime.add(Calendar.HOUR_OF_DAY,-targetDateTime.get(Calendar.HOUR_OF_DAY));
                    if(targetDateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                        mat = l.matSubota;
                    else if(targetDateTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                        mat = l.matNedelja;
                    else
                        mat = l.matRadni;
                }
                i = -1;
            }
            ++i;
        }


        return targetSeconds - sourceSeconds;
    }

    public Response handleRequest5(Request req, double brzinaPesacenja)
    {

        Linija linije[] = MainActivity.graf.getGl().linije;
        MainActivity.graf.resetujCvorove();

        //LocalDateTime currentTime = LocalDateTime.now();
       // LocalDateTime tempTime = null;
        Calendar currentTime = Calendar.getInstance();
        Calendar tempTime = Calendar.getInstance();

        int linijeResenja[][] = new int[linije.length][5]; //start_stanica.id, end_stanica.id, cena_puta, vreme pesacenja do starta, kasnjenje linije na tu stanicu

        for(int i = 0; i < linije.length; ++i)
        {
            if(linije[i] != null)
            {
                Cvor stanica = linije[i].pocetnaStanica;
                Cvor start = null, stop = null;
                Veza v = null;
                int predjeniPut = 0, startPredjeniPut = 0, endPredjeniPut = 0;
                double startnaUdaljenost = Double.MAX_VALUE;//gornjaGranica;
                double zavrsnaUdaljenost = Double.MAX_VALUE;//gornjaGranica;
                double dS, dZ;

                while((v = stanica.vratiVezu(linije[i])) != null)
                {
                    predjeniPut += v.weight;
                    stanica = v.destination;

                    dS = calcDistance(stanica, req.srcLat, req.srcLon);
                    dZ = calcDistance(stanica, req.destLat, req.destLon);
                    //moze da se optimizuje tako sto se jednom izracunaju i zapamte u cvorovima njihove udaljenosti do cilja i starta

                    if(dS<startnaUdaljenost && nijeLazniStart(stanica, linije[i], startnaUdaljenost, zavrsnaUdaljenost, req))
                    {
                        startnaUdaljenost = dS;
                        zavrsnaUdaljenost = Double.MAX_VALUE;;
                        start = stanica;
                        stop = null;
                        startPredjeniPut = predjeniPut;
                    }

                    if(dZ < zavrsnaUdaljenost)
                    {
                        zavrsnaUdaljenost = dZ;
                        stop = stanica;
                        endPredjeniPut = predjeniPut;
                    }

                    if(stanica == linije[i].pocetnaStanica)
                        break;
                }

                linijeResenja[i][0] = start.id;
                linijeResenja[i][1] = stop.id;
                if(start != stop)
                {
                    linijeResenja[i][2] = (int) ((startnaUdaljenost + zavrsnaUdaljenost)/brzinaPesacenja + (endPredjeniPut - startPredjeniPut)/this.brzinaAutobusa(linije[i])); //na ovo treba da se doda jos i vreme cekanja busa na stanici
                    linijeResenja[i][3] = (int) (startnaUdaljenost/brzinaPesacenja);

                    start.cenaPutanje = startnaUdaljenost/brzinaPesacenja;

                    linijeResenja[i][4] = (int) izracunajKasnjenjeLinije2(linije[i], start/*, this.brzinaAutobusa(linije[i])*/);

                    linijeResenja[i][2] += linijeResenja[i][4];
                } else
                    linijeResenja[i][2] = Integer.MAX_VALUE;
            }
        }

        int minCena = Integer.MAX_VALUE;
        ArrayList<Integer> responseLinije = new ArrayList<>();
        //responseLinije[i] odgovara kao start stanica responseStanice[2*i] i kao end stanica responseStanice[2*i+1]
        ArrayList<Integer> responseStanice = new ArrayList<>();
        ArrayList<DatumVremeStanica> vremenaDolaska = new ArrayList<>();
        ArrayList<Integer> responseKorekcije = new ArrayList<>();

        int cenaPesacenja;

        for(int i = 0; i < linije.length; ++i)
            if(linije[i] != null && linijeResenja[i][2] < minCena)
                minCena = linijeResenja[i][2];

        if((cenaPesacenja = (int) (calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaPesacenja)) < minCena)
            minCena = cenaPesacenja;

        for(int i = 0; i < linije.length; ++i)
            if(linije[i] != null && linijeResenja[i][2] <= 1.5 * minCena)
            {
                responseLinije.add(linije[i].id);
                responseKorekcije.add(linijeResenja[i][2]);
                responseStanice.add(linijeResenja[i][0]);
                responseStanice.add(linijeResenja[i][1]);

                tempTime = Calendar.getInstance();
                tempTime.add(Calendar.DAY_OF_WEEK, (int)(linijeResenja[i][3] + linijeResenja[i][4])/86400);
                tempTime.add(Calendar.HOUR_OF_DAY, (int) ((linijeResenja[i][3] + linijeResenja[i][4]) / 3600) % 24);
                tempTime.add(Calendar.MINUTE, (int) ((linijeResenja[i][3] + linijeResenja[i][4]) / 60) % 60);
                tempTime.add(Calendar.SECOND, (int) ((linijeResenja[i][3] + linijeResenja[i][4]) % 60));
               // DatumVremeStanica vremeDolaska = new DatumVremeStanica(linijeResenja[i][0], linije[i].id, tempTime.getSecond(), tempTime.getMinute(), tempTime.getHour(), tempTime.getDayOfWeek().getValue(), tempTime.getMonthValue(), tempTime.getYear());

                DatumVremeStanica vremeDolaska = new DatumVremeStanica(linijeResenja[i][0], linije[i].id,
                        tempTime.get(Calendar.SECOND), tempTime.get(Calendar.MINUTE),
                        tempTime.get(Calendar.HOUR_OF_DAY),
                        tempTime.get(Calendar.DAY_OF_WEEK),
                        tempTime.get(Calendar.MONTH), tempTime.get(Calendar.YEAR),null);

                vremenaDolaska.add(vremeDolaska);
            }

        if(cenaPesacenja <= 1.5 * minCena)
        {
            responseLinije.add(null);
            responseKorekcije.add(cenaPesacenja);
        }

        return (new Response(req.type, responseStanice.toArray(new Integer[responseStanice.size()]),
                responseLinije.toArray(new Integer[responseLinije.size()]),
                responseKorekcije.toArray(new Integer[responseKorekcije.size()]),
                vremenaDolaska, null, null));


    }

    private boolean nijeLazniStart(Cvor stanica, Linija linija,
                                          double startnaUdaljenost, double zavrsnaUdaljenost, Request req)
    {
        boolean p = false;
        double dS, dZ;

        dS = calcDistance(stanica, req.srcLat, req.srcLon);

        Veza v = null;

        while((v = stanica.vratiVezu(linija)) != null)
        {
            stanica = v.destination;

            dZ = calcDistance(stanica, req.destLat, req.destLon);

            if(dS + dZ < startnaUdaljenost + zavrsnaUdaljenost)
            {
                p = true;
                break;
            }

            if(stanica == linija.pocetnaStanica)
                break;
        }

        return p;
    }
}