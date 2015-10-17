package strukture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by filip on 17.10.15..
 */
public class CoordTimestamp
{
    public int cas;
    public int minut;
    public double lat;
    public double lon;


    public CoordTimestamp(double lat, double lon, int cas, int minut)
    {
        this.lat = lat;
        this.lon = lon;
        this.cas = cas;
        this.minut = minut;
    }

    public CoordTimestamp(CoordTimestamp ct)
    {
        this.lat = ct.lat;
        this.lon = ct.lon;
        this.cas = ct.cas;
        this.minut = ct.minut;
    }

    @Override
    public String toString()
    {
        Gson gson = new GsonBuilder().create();

        return gson.toJson(this);
    }
}