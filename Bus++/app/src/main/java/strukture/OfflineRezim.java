package strukture;

import java.util.ArrayList;

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
        Graf g = MainActivity.graf;
        Cvor stanice[] = g.getStanice().toArray(new Cvor[g.getStanice().size()]);
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
            Cvor c = stanice[i];
            System.out.println("STANICA = " + c);

            for(int j = 0; j < c.veze.size(); ++j)
            {
                c = stanice[i];
                Veza v = c.veze.get(j);
                Linija l = v.linija;
                Double d;
                Cvor destStanica = c;
                Double destPesacenje = calcDistance(c, req.destLat, req.destLon);

                System.out.println("LINIJA = " + l);

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
            responseKorekcije[i] = izracunajKorekciju(linije[responseLinijeArray[i]], stanice[responseStanice[0]]);
        }

        Response odgovor = new Response(req.type, responseStanice, responseLinijeArray, responseKorekcije, null, null, null);

        return odgovor;


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
        return (int) (predjeniPut/ Constants.brzinaAutobusa);
    }


    private static int izracunajKorekciju(Linija linija, Cvor targetStanica)
    {
        int predjeniPut = 0;

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
}
