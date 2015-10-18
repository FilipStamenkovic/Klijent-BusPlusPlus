package rs.mosis.diplomski.bus;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import strukture.BusDBAdapter;
import strukture.Cvor;
import strukture.OfflineRezim;
import strukture.Veza;

/**
 * Created by filip on 18.9.15..
 */
public class DirectionsHelper
{
    InputStream is = null;
    String json = "";
    ArrayList<Cvor>  cvorovi = null;
    LatLng [] ulice = null;

    public DirectionsHelper(ArrayList<Cvor> lista)
    {
        cvorovi = lista;
    }

    public DirectionsHelper(LatLng start, LatLng end)
    {
        ulice = new LatLng[2];
        ulice[0] = start;
        ulice[1] = end;
    }


    private String makeURL(int pocetak, int kraj, String mode)
    {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(ulice[pocetak].latitude);
        urlString.append(",");
        urlString
                .append(Double.toString(ulice[pocetak].longitude));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString(ulice[kraj].latitude));
        urlString.append(",");
        urlString.append(Double.toString(ulice[kraj].longitude));
        if(mode.equals("driving"))
        {
            urlString.append("&waypoints=optimize:true");

            for (int i = pocetak + 1; i < kraj - 1; i++)
            {
                String umetni;
                umetni = "|via:" + ulice[i].latitude + "," + ulice[i].longitude;

                urlString.append(umetni);

            }
        }

        urlString.append("&sensor=false&mode=" + mode);
       // urlString.append("&key=AIzaSyC0d76hl9vJDNrNmW5GWTHqQJfutc4OcoQ");
        return urlString.toString();
    }

    private String getJSONFromUrl(String url)
    {

        HttpURLConnection urlConnection = null;
        try
        {
            URL u = new URL(url);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.connect();
            is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            json = sb.toString();

            br.close();
            is.close();
            urlConnection.disconnect();
        } catch (MalformedURLException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return json;
    }

    public List<LatLng> getTacke(String mode)
    {
        int pocetak = 0;
        int granica = 10;
        int kraj = ulice.length - 1;


        List<LatLng> list = null;
        JSONArray routeArray = null;
        try {
            String odgovor = "";
            while(pocetak < kraj) {
               // if (granica >= kraj)
               //     odgovor = getJSONFromUrl(makeURL(pocetak, kraj));
             //   else
             //       odgovor = getJSONFromUrl(makeURL(pocetak, granica));
                odgovor = getJSONFromUrl(makeURL(pocetak, kraj, mode));
                pocetak = granica;
                pocetak = 1999;
                if (granica + 10 < kraj)
                    granica += 10;
                else
                    granica = kraj;


                final JSONObject json = new JSONObject(odgovor);
                routeArray = json.getJSONArray("routes");
                JSONObject routes = routeArray.getJSONObject(0);
                JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");

                String encodedString = overviewPolylines.getString("points");
                if(list == null)
                    list = decodePoly(encodedString);
                else
                    list.addAll(decodePoly(encodedString));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mode.equals("walking"))
            return preurediTacke(list);
        else
            return list;

    }

    public List<LatLng> getDriving()
    {
        List<LatLng> lista = new ArrayList<>();
        if (!BusDBAdapter.setPolilinije())
            return null;

        int size = cvorovi.size() - 1;
        for (int i = 0; i < size; i++)
        {
            ArrayList<Veza> veze = cvorovi.get(i).veze;
            int vsize = veze.size();
            for (int j = 0; j < vsize; j++)
            {
                if (veze.get(j).destination == cvorovi.get(i + 1))
                {
                    lista.addAll(veze.get(j).putanje);
                    break;
                }
            }
        }

        return lista;
    }

    private List<LatLng> preurediTacke(List<LatLng> tacke)
    {
        if (tacke == null)
        {

            tacke = new ArrayList<>();
            tacke.addAll(vratiTackeIzmedju(ulice[0], ulice[1], Constants.udaljenostPesacenje));
            return tacke;
        } else
        {
            ArrayList<LatLng> preuredjene = new ArrayList<>();
            LatLng trenutna = tacke.get(0);
            tacke.remove(0);
            preuredjene.add(trenutna);
            int size = tacke.size();
            for (int i = 0; i < size; i++)
            {
                LatLng radna = tacke.get(0);
                double udaljenost = OfflineRezim.calcDistance(trenutna.latitude, trenutna.longitude, radna.latitude, radna.longitude);
                tacke.remove(0);
                if (udaljenost > Constants.udaljenostPesacenje * 2)
                {
                    preuredjene.addAll(vratiTackeIzmedju(trenutna, radna, Constants.udaljenostPesacenje));
                    trenutna = radna;
                } else if (udaljenost > Constants.udaljenostPesacenje)
                {
                    preuredjene.add(radna);
                    trenutna = radna;
                }
            }
            size = preuredjene.size();
            ArrayList<LatLng> indeksi = new ArrayList<>();
            for (int i = 0; i < size; i++)
                for (int j = i + 1; j < size; j++)
                {
                    double udaljenost = OfflineRezim.calcDistance(
                            preuredjene.get(i).latitude, preuredjene.get(i).longitude,
                            preuredjene.get(j).latitude, preuredjene.get(j).longitude);
                    if (udaljenost < Constants.udaljenostPesacenje)
                        for (int l = i + 1; l < j; l++)
                            indeksi.add(preuredjene.get(l));
                }
            int j = 0;
            while (indeksi.size() > 0)
            {
                size = preuredjene.size();
                int i;
                for (i = j; i < size; i++)
                    if (preuredjene.get(i) == indeksi.get(0))
                    {
                        preuredjene.remove(i);
                        indeksi.remove(0);
                        j = i;
                        break;
                    }
                if (i == size)
                    indeksi.remove(0);
            }

            return preuredjene;
        }
    }

    public List<LatLng> preurediTacke(double udalj, List<LatLng> tacke)
    {
        /*ArrayList<LatLng> tacke = new ArrayList<>();
        tacke.add(ulice[0]);
        tacke.add(ulice[1]);*/

        ArrayList<LatLng> preuredjene = new ArrayList<>();
        LatLng trenutna = tacke.get(0);
        //tacke.remove(0);
        preuredjene.add(trenutna);
        int size = tacke.size();
        for (int i = 1; i < size; i++)
        {
            LatLng radna = tacke.get(i);
            double udaljenost = OfflineRezim.calcDistance(trenutna.latitude, trenutna.longitude, radna.latitude, radna.longitude);
            //tacke.remove(0);
            if (udaljenost >udalj * 2)
            {
                preuredjene.addAll(vratiTackeIzmedju(trenutna, radna, udalj));
                trenutna = radna;
            } else if (udaljenost > udalj)
            {
                preuredjene.add(radna);
                trenutna = radna;
            }
        }
        size = preuredjene.size();
        ArrayList<LatLng> indeksi = new ArrayList<>();
        for (int i = 0; i < size; i++)
            for (int j = i + 1; j < size; j++)
            {
                double udaljenost = OfflineRezim.calcDistance(
                        preuredjene.get(i).latitude, preuredjene.get(i).longitude,
                        preuredjene.get(j).latitude, preuredjene.get(j).longitude);
                if (udaljenost < udalj)
                    for (int l = i + 1; l < j; l++)
                        indeksi.add(preuredjene.get(l));
            }
        int j = 0;
        while (indeksi.size() > 0)
        {
            size = preuredjene.size();
            int i;
            for (i = j; i < size; i++)
                if (preuredjene.get(i) == indeksi.get(0))
                {
                    preuredjene.remove(i);
                    indeksi.remove(0);
                    j = i;
                    break;
                }
            if (i == size)
                indeksi.remove(0);
        }

        return preuredjene;

    }


    private List<LatLng> vratiTackeIzmedju(LatLng source,LatLng dest, double razmak)
    {
        double udaljenost = OfflineRezim.calcDistance(source.latitude,source.longitude,dest.latitude,dest.longitude);
        double latRazlika = dest.latitude - source.latitude;
        double lonRazlika = dest.longitude - source.longitude;
        ArrayList<LatLng> tacke = null;
        int brojTacaka = (int) (Math.round(udaljenost / razmak));
        if (brojTacaka > 0)
        {
            tacke = new ArrayList<>();
            double difLat = latRazlika / (double) brojTacaka;
            double difLon = lonRazlika / (double) brojTacaka;
            double lat,lon;
            lat = source.latitude;
            lon = source.longitude;
            for (int i = 0; i < brojTacaka; i++)
            {
                lat += difLat;
                lon += difLon;
                tacke.add(new LatLng(lat,lon));
            }
        }

        return tacke;
    }

    private List<LatLng> decodePoly(String encoded)
    {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }
}
