package rs.mosis.diplomski.bus;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

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

import strukture.Cvor;

/**
 * Created by filip on 18.9.15..
 */
public class DirectionsHelper
{
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    Cvor [] cvorovi = null;
    LatLng [] ulice = null;
    double [] ugao;

    boolean [] uzmi;

    public DirectionsHelper(ArrayList<Cvor> lista)
    {
        cvorovi = new Cvor[lista.size()];
        ArrayList<String> listaUlica = new ArrayList<>();
        ArrayList<LatLng> latLngs = new ArrayList<>();
        cvorovi[0] = lista.get(0);
        latLngs.add(new LatLng(cvorovi[0].lat, cvorovi[0].lon));

        lista.remove(0);
      /*  for(int i = 1; i < cvorovi.length - 1; i++)
        {
            cvorovi[i] = lista.get(0);
            lista.remove(0);

            List<Address> addresses;

            try {
                addresses = geocoder.getFromLocation(cvorovi[i].lat, cvorovi[i].lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                String address = addresses.get(0).getFeatureName();
                if(listaUlica.isEmpty() || (address == null))
                {
                    listaUlica.add(address);
                    latLngs.add(new LatLng(cvorovi[i].lat, cvorovi[i].lon));
                }else if(!address.equals(listaUlica.get(listaUlica.size() - 1)))
                {
                    listaUlica.add(address);
                    latLngs.add(new LatLng(cvorovi[i - 1].lat, cvorovi[i - 1].lon));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }*/

        for(int i = 1; i <cvorovi.length; i++)
        {
            cvorovi[i] = lista.get(0);
            lista.remove(0);


        }
        int broj_tacaka = 10;
        if(cvorovi.length < broj_tacaka)
            broj_tacaka = cvorovi.length;
        ugao = new double[cvorovi.length];
        uzmi = new boolean[cvorovi.length];
        uzmi[0] = false;
        ArrayList<Integer> listaIndeksa = new ArrayList<>();
        for(int i = 1; i < cvorovi.length - 1; i++)
        {
            ugao[i] = ugaoPrava(cvorovi[i - 1].lat, cvorovi[i].lat, cvorovi[i - 1].lon,
                    cvorovi[i].lon, cvorovi[i + 1].lat, cvorovi[i + 1].lon);
            int j;
            uzmi[i] = false;
            for(j = 0; j < listaIndeksa.size(); j++)
                if(ugao[i] > ugao[listaIndeksa.get(j).intValue()])
                {
                    listaIndeksa.add(j, i);
                    break;
                }
            if (j == listaIndeksa.size())
                listaIndeksa.add(listaIndeksa.size(), i);
        }

       // indeks = new int[listaIndeksa.size()];
        for(int i = 0; i < broj_tacaka - 2; i++)
        {
            uzmi[listaIndeksa.get(0).intValue()] = true;

            listaIndeksa.remove(0);
        }


        //latLngs.add(new LatLng(cvorovi[cvorovi.length - 1].lat, cvorovi[cvorovi.length - 1].lon));

        ulice = new LatLng[broj_tacaka];
        int j = 1;
        for(int i = 1; i < cvorovi.length; i++)
        {
            if(uzmi[i])
                ulice[j++] = new LatLng(cvorovi[i].lat,cvorovi[i].lon);
            //latLngs.remove(0);
        }
        ulice[0] = new LatLng(cvorovi[0].lat,cvorovi[0].lon);
        ulice[broj_tacaka - 1] = new LatLng(cvorovi[cvorovi.length - 1].lat,cvorovi[cvorovi.length - 1].lon);

    }

    private double ugaoPrava(double x1, double x2, double y1, double y2,double x3, double y3)
    {
        double k1 = (y2 - y1) / (x2 - x1);
        double k2 = (y3 - y2) / (x3 - x2);

        double tangens =  (k2 - k1)/(1 + k1 * k2);
        if(tangens < 0.0)
            tangens *= -1;

        return (Math.atan(tangens) * 180 / Math.PI);
    }

    private String makeURL(int pocetak, int kraj)
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
        urlString.append("&waypoints=optimize:true");

        for(int i = pocetak + 1; i < kraj - 1 ; i++)
        {
            String umetni;
            umetni = "|via:" + ulice[i].latitude + "," + ulice[i].longitude;

            urlString.append(umetni);

        }

        urlString.append("&sensor=false&mode=driving");
       // urlString.append("&key=AIzaSyC0d76hl9vJDNrNmW5GWTHqQJfutc4OcoQ");
        return urlString.toString();
    }

    private String getJSONFromUrl(String url) {

        // Making HTTP request
        // defaultHttpClient

        HttpURLConnection urlConnection = null;
        try {
            URL u = new URL(url);
            urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.connect();
            is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            json = sb.toString();

            br.close();
            is.close();
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return json;
    }



    public List<LatLng> getTacke()
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
                odgovor = getJSONFromUrl(makeURL(pocetak, 9));
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

        return list;

    }

    private List<LatLng> decodePoly(String encoded)
    {
        List<LatLng> poly = new ArrayList<LatLng>();
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
