package strukture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;

public class Linija
{
	public Integer id = null;
	public String broj = null;
	public String smer = null;
	public String naziv = null;
	public transient String podlinija = null;
	public transient Cvor pocetnaStanica = null;

	public transient int matRadni[][] = null;         	//mat[i][j]==-1 znaci prazno
	public transient int matSubota[][] = null;
	public transient int matNedelja[][] = null;

	public void setMatNedelja(int[][] matNedelja)
	{
		this.matNedelja = matNedelja;
	}

	public void setMatRadni(int[][] matRadni)
	{
		this.matRadni = matRadni;
	}

	public void setMatSubota(int[][] matSubota)
	{
		this.matSubota = matSubota;
	}

	public Linija() {}
	
	public Linija(Integer id, String broj, String smer, String naziv, Cvor pocetnaStanica)
	{
		this.id = id;
		this.broj = broj;
		this.smer = smer;
		this.naziv = naziv;
		this.pocetnaStanica = pocetnaStanica;
	}


	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder().create();
		
		return gson.toJson(this);
		//return id + " " +broj +" " +smer + " " +naziv + " <<" + pocetnaStanica.id +">>";
	}

	private void setPodlinija()
	{
		podlinija = "";
		String brojke = broj.replaceAll("[0-9]","");
		for(int i = 0; i < broj.length() - brojke.length(); i++)
			podlinija += "*";
	}

	public String getPodlinija()
	{
		return podlinija;
	}

	public ArrayList<Integer> getVremena(int korekcija)
	{
		//String vreme = "";

		setPodlinija();

		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DAY_OF_WEEK);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		hour = 14;

		ArrayList<Integer> vremena = new ArrayList<>();
		/*while(korekcija > 3600)
		{
			hour++;
			korekcija -= 3600;
		}
		while(korekcija > 0)
		{
			minute++;
			korekcija -= 60;
		}
		while(minute > 59)
		{
			hour++;
			minute -= 60;
		}*/
		hour += korekcija / 3600;
		minute += (korekcija % 3600) / 60;
		while(minute > 59)
		{
			hour++;
			minute -= 60;
		}



		for(int i = hour; i < 25; i++)
		{
			int j = 0;
			while((j < 60) && (matRadni[i][j] != -1))
			{
				if(minute <= matRadni[i][j])
					//vreme += i + ":" + matRadni[i][j] + " " + podlinija + "\n";
					vremena.add(i * 100 + matRadni[i][j]);
				j++;
			}

			minute = -1;
		}

		return vremena;
	}
}
