package strukture;

import java.sql.SQLException;

import rs.mosis.diplomski.bus.MainActivity;


public class GradskeLinije
{
	//public ArrayList<Linija> linije = new ArrayList<>();
	public Linija linije[];
	
	public GradskeLinije(int size) 
	{
		linije = new Linija[size];
	}
			
	public GradskeLinije() throws ClassNotFoundException, SQLException, Exception
	{
		int maxId = 0;

        linije = BusDBAdapter.getAllLinije();
	}

	public static boolean istaOsnovna(int id1,int id2)
	{
		String prva = MainActivity.graf.getGl().linije[id1].broj;
		String druga = MainActivity.graf.getGl().linije[id2].broj;
		prva = prva.replace("*","");
		druga = druga.replace("*","");
		return prva.equals(druga);

	}

}
