package strukture;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Veza
{
	public Cvor destination = null;
	public Integer weight = null;
	public Linija linija = null;
	public List<LatLng> putanje = null;
	public Gson gson;
	public Veza() {}
	
	public Veza(Cvor destination, Integer weight, Linija linija)
	{
		this.destination = destination;
		this.weight = weight;
		this.linija = linija;
	}

	public void setPutanje(String json)
	{
		if (putanje == null)
		{
			gson = new GsonBuilder().create();
			putanje = new ArrayList<>();
			putanje = gson.fromJson(json, new TypeToken<ArrayList<LatLng>>() {}.getType());
		}
	}

	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
	}
}
