package strukture;

import android.util.Log;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import rs.mosis.diplomski.bus.BusDatabasesHelper;
import rs.mosis.diplomski.bus.MainActivity;


public class Graf
{
	private ArrayList<Cvor> cvorovi = new ArrayList<>();
	private GradskeLinije gl;
	public static int stanicaMaxId;
	public  Double [][]  matricaUdaljenosti;
	private HashMap<Integer, Cvor> hashMap = new HashMap<>();

	
	public Graf(String grafDBName, String redVoznjeDBName,String putanjeDBName) throws Exception
	{
		int maxId = 0;
		Cvor tempArray[] = null;

		BusDatabasesHelper.setDbName(grafDBName);
		
		gl = new GradskeLinije();
		
		// load the sqlite-JDBC driver using the current class loader
		MainActivity.aplikacija.namestiProgres();
	    tempArray = BusDBAdapter.getAllCvorovi(hashMap);
	    
	    for(int i = 0; i < gl.linije.length; ++i)
	    {
	    	if(gl.linije[i] != null)
	    		gl.linije[i].pocetnaStanica = tempArray[gl.linije[i].pocetnaStanica.id];
	    }
		MainActivity.aplikacija.namestiProgres();
	    boolean b = BusDBAdapter.podesiVeze(tempArray,gl.linije);

	    
	    for(int i = 0; i < tempArray.length; ++i)
	    {
	    	if(tempArray[i] != null)
	    	{
	    		cvorovi.add(tempArray[i]);
	    		tempArray[i] = null;
	    	}
	    }

		BusDatabasesHelper.setDbName(redVoznjeDBName);
		MainActivity.aplikacija.namestiProgres();

		for(int i = 0; i < gl.linije.length; ++i)
			if(gl.linije[i] != null)
				BusDBAdapter.podesiRedVoznje(gl.linije[i]);

		BusDatabasesHelper.setDbName(putanjeDBName);
	}
	
	public ArrayList<Cvor> pratiLiniju(int linijaId,int pocetna_id,int krajnja_id)
	{
		Linija l = gl.linije[linijaId];
		ArrayList<Cvor> cvorovi = new ArrayList<>();

		int udaljenost = 0;
		if(l == null)
			return null;


		
		Cvor c = l.pocetnaStanica;

		//cvorovi.add(c);
		Cvor pocetna = c;
		Veza v = null;

		if(pocetna_id != -1)
		{
			while (c.id != pocetna_id)
			{
				v = c.vratiVezu(l);
				c = v.destination;
				//cvorovi.add(c);
				udaljenost += v.weight;
				//System.out.println(c.toString() + " udaljenost = " + udaljenost);
				Log.i("TAG", c.toString() + " udaljenost = " + udaljenost);
				if (c == pocetna)
					break;
			}
		}

		cvorovi.add(c);
		
		//System.out.println("Linija:> " + l.toString());
		//System.out.println(c.toString() + " udaljenost = " + udaljenost);
		Log.i("TAG", "Linija:> " + l.toString());
		Log.i("TAG", c.toString() + " udaljenost = " + udaljenost);
		while(((v = c.vratiVezu(l)) != null) && (c.id != krajnja_id))
		{
			c = v.destination;
			cvorovi.add(c);
			udaljenost += v.weight;
			//System.out.println(c.toString() + " udaljenost = " + udaljenost);
			Log.i("TAG",c.toString() + " udaljenost = " + udaljenost);
			if(c == pocetna)
				break;
		}

		return cvorovi;
	}

	public GradskeLinije getGl() {
		return gl;
	}
	public ArrayList<Cvor> getStanice()
	{
		return this.cvorovi;
	}

	public void resetujCvorove()
	{
		for(Cvor c : cvorovi)
			c.resetStatus();

		for(Linija l : gl.linije)
			if(l != null)
				l.prioritet = Double.MAX_VALUE;
	}

	public void inicijalizujMatricu()
	{

		matricaUdaljenosti = new Double[stanicaMaxId + 3][stanicaMaxId + 3];
		synchronized (matricaUdaljenosti)
		{
			ArrayList<Cvor> stanice = cvorovi;

			for (int i = 0; i < stanice.size(); ++i)
				for (int j = 0; j < stanice.size(); ++j)
				{
					Cvor start = stanice.get(i);
					Cvor finish = stanice.get(j);
					matricaUdaljenosti[start.id][finish.id] = calcDistance(start, finish);
				}
		}
	}

	private double calcDistance(Cvor c1, Cvor c2)
	{
		double a, c;

		a = Math.sin((c2.lat - c1.lat)*Math.PI/360) * Math.sin((c2.lat - c1.lat)*Math.PI/360) +
				Math.sin((c2.lon - c1.lon)*Math.PI/360) * Math.sin((c2.lon - c1.lon)*Math.PI/360) * Math.cos(c2.lat * Math.PI/180) * Math.cos(c1.lat * Math.PI/180);

		c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return 6371000 * c;
	}

    public Cvor getStanica(int stanicaId)
    {
        return hashMap.get(stanicaId);
    }
}
