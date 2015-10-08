package strukture;



import android.util.Log;

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
    public static Response handleRequest3(Request req)
    {
        Linija linije[] = MainActivity.graf.getGl().linije;
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
        Integer korekcije[] = new Integer[targetLinije.size()];

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

            korekcije[i] = izracunajKorekciju(l, minDistanceStanice[i], predjeniPutBusaDoNajblizeStanice);
        }

        Response response = new Response(req.type, staniceId, linijeId, korekcije, null, null, null);
        return response;


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

    private static int izracunajKorekciju(Linija linija, Cvor stanica, int predjeniPut)
    {
        return (int) (predjeniPut/Constants.brzinaAutobusa);
    }

    private static int izracunajKorekciju(Linija linija, Cvor targetStanica)
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
    public static Response handleRequest4(Request req)
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
    public static Response handleRequest6(Request req, double brzinaAutobusa, double brzinaPesacenja)
    {
        boolean nadjenPut = false;
        Calendar currentTime = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        Calendar tempTime = Calendar.getInstance();;

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
        pseudoStart.heuristika = calcDistance(req.srcLat, req.srcLon, req.destLat, req.destLon)/brzinaAutobusa;
        pseudoStart.linijom = null;
        pseudoStart.prethodnaStanica = null;
        pseudoStart.cenaPutanje = 0.0;

        Graf graf = MainActivity.graf;
        graf.resetujCvorove();

        double[][] matricaUdaljenosti = graf.matricaUdaljenosti;

                //izvuci sve stanice u niz
        Cvor stanice[] = graf.getStanice().toArray(new Cvor[graf.getStanice().size()]);

        PrioritetnaListaCvorova lista = new PrioritetnaListaCvorova();

        //izracunaj heuristike
        for(int i = 0; i < stanice.length; ++i)
        {
            if(stanice[i] != null)
            {
                stanice[i].heuristika = calcDistance(stanice[i], req.destLat, req.destLon)/brzinaAutobusa;
                stanice[i].cenaPutanje = calcDistance(stanice[i], req.srcLat, req.srcLon)/brzinaPesacenja;

                stanice[i].linijom = null;
                stanice[i].prethodnaStanica = pseudoStart;

                stanice[i].status = StruktureConsts.CVOR_SMESTEN;
            }

            lista.pushPriority(stanice[i]);
        }

        pseudoStart.status = StruktureConsts.CVOR_OBRADJEN;
        pseudoEnd.status = StruktureConsts.CVOR_SMESTEN;
        lista.pushPriority(pseudoEnd); //dodaj i pseudoEnd u priority list

        Cvor radniCvor = null;
        Cvor tempCvor = null;
        ArrayList<Veza> potomciVeze = null;
        Veza v = null;
        Linija l = null;
        long kasnjenje = 0;

        while(!lista.isEmpty())
        {
            radniCvor = lista.remove(0);

            radniCvor.status = StruktureConsts.CVOR_OBRADJEN;

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

                if(tempCvor.status == StruktureConsts.CVOR_OBRADJEN)
                    continue;

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
                    Log.e("tag","USO");
                    if(tempCvor.cenaPutanje > radniCvor.cenaPutanje + v.weight/brzinaAutobusa +
                            (kasnjenje = izracunajKasnjenjeLinije2(v.linija, radniCvor, brzinaAutobusa)))
                    {
                        lista.remove(tempCvor);
                        tempCvor.linijom = v.linija;
                        tempCvor.prethodnaStanica = radniCvor;
                        tempCvor.cenaPutanje = radniCvor.cenaPutanje + v.weight/brzinaAutobusa + kasnjenje;

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
                        tempTime.add(Calendar.DAY_OF_WEEK, (int)(radniCvor.cenaPutanje + kasnjenje)/86400);
                        tempTime.add(Calendar.HOUR_OF_DAY, (int) ((radniCvor.cenaPutanje + kasnjenje) / 3600) % 24);
                        tempTime.add(Calendar.MINUTE, (int) ((radniCvor.cenaPutanje + kasnjenje) / 60) % 60);
                        tempTime.add(Calendar.SECOND, (int) ((radniCvor.cenaPutanje + kasnjenje) % 60));

                        DatumVremeStanica vremeDolaska = new DatumVremeStanica(radniCvor.id, v.linija.id,
                                tempTime.get(Calendar.SECOND), tempTime.get(Calendar.MINUTE),
                                tempTime.get(Calendar.HOUR_OF_DAY),
                                tempTime.get(Calendar.DAY_OF_WEEK),
                                tempTime.get(Calendar.MONTH), tempTime.get(Calendar.YEAR));

                        tempCvor.vremeDolaskaAutobusaNaPrethodnuStanicu = vremeDolaska;


                        lista.pushPriority(tempCvor);
                    }
                    Log.e("tag","izaso");
                }
            }

            Log.e("tag","size je:" + lista.size());
            //obradi pesacenje do svih stanica
            for(int j = 0; j < stanice.length; ++j)
            {
                if(stanice[j] != radniCvor && stanice[j].status != StruktureConsts.CVOR_OBRADJEN)
                {
                    double udaljenost = matricaUdaljenosti[radniCvor.id][stanice[j].id];
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
            Cvor c = pseudoEnd;

            Response response = new Response();
            response.type = req.type;
            response.size = (int) pseudoEnd.cenaPutanje;    //procenjena cena putovanja
            ArrayList<Integer> responseStanice = new ArrayList<>();
            ArrayList<Integer> responseLinije = new ArrayList<>();
            ArrayList<DatumVremeStanica> responseVremenaDolaska = new ArrayList<>();

            while (c != null)
            {
                responseStanice.add(c.id);

                if (c.linijom != null)
                    responseLinije.add(c.linijom.id);
                else
                    responseLinije.add(null);

                if (c.vremeDolaskaAutobusaNaPrethodnuStanicu != null)
                    responseVremenaDolaska.add(c.vremeDolaskaAutobusaNaPrethodnuStanicu);

                //moze da se doda u response i Estimated Time of Arrival (dodao sam ga u Response.size)
                c = c.prethodnaStanica;
            }

            response.stanice = new Integer[responseStanice.size()];
            response.linije = new Integer[responseLinije.size()];
            response.vremenaDolaska = new ArrayList<>();

            int arraySize = responseStanice.size();
            for (int i = arraySize - 1; i >= 0; --i)
            {
                response.stanice[arraySize - 1 - i] = responseStanice.get(i);
                response.linije[arraySize - 1 - i] = responseLinije.get(i);
            }

            arraySize = responseVremenaDolaska.size();
            if (arraySize > 0)
            {
                for (int i = arraySize - 1; i >= 0; --i)
                    response.vremenaDolaska.add(responseVremenaDolaska.get(i));
            } else
                response.vremenaDolaska = null;


            return response;


        }
        else
        {
            return null;
        }
    }

    private static long izracunajKasnjenjeLinije2(Linija l, Cvor c, double brzinaAutobusa)
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
        Log.e("tag","USO korekcija " + l.broj);

        long busTravelSeconds = izracunajKorekciju(l, c);

        Log.e("tag","izaso korekcija");

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
        //provera za nedelju

		/*if(targetDate.getDay() == 0)
			mat = l.matNedelja;
		else if(targetDate.getDay() == 6)
			mat = l.matSubota;
		else
			mat = l.matRadni;*/

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

        Log.e("tag","USO" + targetDateTime.toString());
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
					/*System.out.println();
					System.out.println("Bus travel seconds = " + busTravelSeconds);
					System.out.println("Hvatam bus u " + targetDateTime.getDayOfWeek() + " koji krece u " + h + ":" + mat[h][i]);
					System.out.println();*/
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
        Log.e("tag","izaso" + targetDateTime.toString());

        //targetDateTime = targetDateTime.plusDays(busTravelSeconds/86400);
        //targetDateTime = targetDateTime.plusHours((busTravelSeconds/3600)%24);
        //targetDateTime = targetDateTime.plusMinutes((busTravelSeconds/60)%60);
        //targetDateTime = targetDateTime.plusSeconds(busTravelSeconds%60);

        //DatumVremeStanica vremeDolaska = new DatumVremeStanica(c.id, targetDateTime.getSecond(), targetDateTime.getMinute(), targetDateTime.getHour(), targetDateTime.getDayOfWeek().getValue(), targetDateTime.getMonthValue(), targetDateTime.getYear());
        //c.vremeDolaskaAutobusa = vremeDolaska;

        //System.out.println("bla=" + bla);
        return targetSeconds - sourceSeconds;
    }
}
