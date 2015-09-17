package strukture;

import android.util.Log;

import java.io.Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class Graf
{
	private ArrayList<Cvor> cvorovi = new ArrayList<>();
	private GradskeLinije gl;
	
	public Graf() {}
	
	public Graf(String grafDBName, String redVoznjeDBName) throws ClassNotFoundException, SQLException, Exception
	{
		int maxId = 0;
		Cvor tempArray[] = null;
		
		gl = new GradskeLinije(grafDBName, redVoznjeDBName);
		
		// load the sqlite-JDBC driver using the current class loader
	    tempArray = BusDBAdapter.getAllCvorovi();
	    
	    for(int i = 0; i < gl.linije.length; ++i)
	    {
	    	if(gl.linije[i] != null)
	    		gl.linije[i].pocetnaStanica = tempArray[gl.linije[i].pocetnaStanica.id];
	    }
	    boolean b = BusDBAdapter.podesiVeze(tempArray,gl.linije);

	    
	    for(int i = 0; i < tempArray.length; ++i)
	    {
	    	if(tempArray[i] != null)
	    	{
	    		cvorovi.add(tempArray[i]);
	    		tempArray[i] = null;
	    	}
	    }
	}
	
	public ArrayList<Cvor> pratiLiniju(int linijaId)
	{
		Linija l = gl.linije[linijaId];
		ArrayList<Cvor> cvorovi = new ArrayList<>();

		int udaljenost = 0;
		if(l == null)
			return null;


		
		Cvor c = l.pocetnaStanica;
		cvorovi.add(c);
		Cvor pocetna = c;
		Veza v = null;
		
		//System.out.println("Linija:> " + l.toString());
		//System.out.println(c.toString() + " udaljenost = " + udaljenost);
		Log.i("TAG","Linija:> " + l.toString());
		Log.i("TAG",c.toString() + " udaljenost = " + udaljenost);
		while((v = c.vratiVezu(l)) != null)
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
}
