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
	public transient Cvor pocetnaStanica = null;

	public transient int matRadni[][] = null;         	//mat[i][j]==-1 znaci prazno
	public transient int matSubota[][] = null;
	public transient int matNedelja[][] = null;

    public transient double prioritet = Double.MAX_VALUE;


    private transient double raspodelaBrzina[] =	{
            8.3,	//00-01
            8.3,	//01-02
            8.0,	//02-03
            7.0,	//03-04
            6.5,	//04-05
            5.4,	//05-06
            5.0,	//06-07
            4.5,	//07-08
            4.15,	//08-09
            5.0,	//09-10
            5.4,	//10-11
            6.0,	//11-12
            6.0,	//12-13
            5.4,	//13-14
            4.5,	//14-15
            4.15,	//15-16
            5.0,	//16-17
            5.4,	//17-18
            6.0,	//18-19
            6.0,	//19-20
            7.0,	//20-21
            7.5,	//21-22
            8.0,	//22-23
            8.3		//23-24
    };


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

	public ArrayList<Integer> getVremena(int korekcija)
	{

		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DAY_OF_WEEK);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);

		//hour = 14;
		ArrayList<Integer> vremena = new ArrayList<>();
		hour -= korekcija / 3600;
		minute -= (korekcija % 3600) / 60;
		while(minute < 0)
		{
			hour--;
			minute += 60;
		}

        if (hour < 0)
        {
            hour += 24;
            day--;
        }

		if(day == Calendar.SUNDAY)
        {

            for (int i = hour; i < 25; i++)
            {
                int j = 0;
                while ((j < 60) && (matNedelja[i][j] != -1))
                {
                    if (minute <= matNedelja[i][j])
                        //vreme += i + ":" + matRadni[i][j] + " " + podlinija + "\n";
                        vremena.add(i * 100 + matNedelja[i][j]);
                    j++;
                }

                minute = -1;
            }
        }else if(day == Calendar.SATURDAY)
        {
            for (int i = hour; i < 25; i++)
            {
                int j = 0;
                while ((j < 60) && (matSubota[i][j] != -1))
                {
                    if (minute <= matSubota[i][j])
                        //vreme += i + ":" + matRadni[i][j] + " " + podlinija + "\n";
                        vremena.add(i * 100 + matSubota[i][j]);
                    j++;
                }

                minute = -1;
            }

            for(int i = 0; i < 3; i++)
            {
                int j = 0;
                while ((j < 60) && (matNedelja[i][j] != -1))
                {
                    if (minute <= matNedelja[i][j])
                        //vreme += i + ":" + matRadni[i][j] + " " + podlinija + "\n";
                        vremena.add(i * 100 + matNedelja[i][j]);
                    j++;
                }
            }
        }else if(day == Calendar.FRIDAY)
        {
            for (int i = hour; i < 25; i++)
            {
                int j = 0;
                while ((j < 60) && (matRadni[i][j] != -1))
                {
                    if (minute <= matRadni[i][j])
                        //vreme += i + ":" + matRadni[i][j] + " " + podlinija + "\n";
                        vremena.add(i * 100 + matRadni[i][j]);
                    j++;
                }

                minute = -1;
            }

            for(int i = 0; i < 3; i++)
            {
                int j = 0;
                while ((j < 60) && (matSubota[i][j] != -1))
                {
                    if (minute <= matSubota[i][j])
                        //vreme += i + ":" + matRadni[i][j] + " " + podlinija + "\n";
                        vremena.add(i * 100 + matSubota[i][j]);
                    j++;
                }
            }
        }
        else
        {
            for (int i = hour; i < 25; i++)
            {
                int j = 0;
                while ((j < 60) && (matRadni[i][j] != -1))
                {
                    if (minute <= matRadni[i][j])
                        //vreme += i + ":" + matRadni[i][j] + " " + podlinija + "\n";
                        vremena.add(i * 100 + matRadni[i][j]);
                    j++;
                }

                minute = -1;
            }
        }
		return vremena;
	}

	public ArrayList<Integer> getVremena(int korekcija, int size)
	{


		Calendar now = Calendar.getInstance();
		int day = now.get(Calendar.DAY_OF_WEEK);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);

		ArrayList<Integer> vremena = new ArrayList<>();
        int brojac = 0;
        hour -= korekcija / 3600;
        minute -= (korekcija % 3600) / 60;
        while(minute < 0)
        {
            hour--;
            minute += 60;
        }
        if(hour < 0)
        {
            day--;
            hour+=24;
        }
        int  dodatak = 0;
        //hour = 14;
        while(brojac < size)
        {


            if (day == Calendar.SUNDAY)
            {

                for (int i = hour; i < 25; i++)
                {
                    int j = 0;
                    if ((i * 60 - hour * 60 - minute) > 60)
                        return vremena;
                    while ((j < 60) && (matNedelja[i][j] != -1))
                    {
                        if ((i > hour) || (minute <= matNedelja[i][j]))
                        {

                            vremena.add((i + dodatak) * 100 + matNedelja[i][j]);
                            brojac +=  minute - matNedelja[i][j];
                            if ((i * 60 + matNedelja[i][j] - hour * 60 - minute) > 60)
                                return vremena;
                        }
                        j++;
                    }
                }
            } else if (day == Calendar.SATURDAY)
            {
                for (int i = hour; i < 25; i++)
                {
                    int j = 0;
                    if ((i * 60 - hour * 60 - minute) > 60)
                        return vremena;
                    while ((j < 60) && (matSubota[i][j] != -1))
                    {
                        if ((i > hour) || (minute <= matSubota[i][j]))
                        {

                            vremena.add((i + dodatak) * 100 + matSubota[i][j]);
                            brojac += ((i - hour) % 24) * 60 + minute - matSubota[i][j];
                            if ((i * 60 + matSubota[i][j] - hour * 60 - minute) > 60)
                                return vremena;
                        }
                        j++;
                    }
                }

                for (int i = 0; i < 3; i++)
                {
                    int j = 0;
                    if ((i * 60 - hour * 60 - minute) > 60)
                        return vremena;
                    while ((j < 60) && (matNedelja[i][j] != -1))
                    {
                        if ((i > hour) || (minute <= matNedelja[i][j]))
                        {

                            vremena.add((i + 24) * 100 + matNedelja[i][j]);
                            brojac += ((i - hour) % 24) * 60 + minute - matNedelja[i][j];
                            if (((i + 24) * 60 + matNedelja[i][j] - hour * 60 - minute) > 60)
                                return vremena;
                        }
                        j++;
                    }
                }
            } else if (day == Calendar.FRIDAY)
            {
                for (int i = hour; i < 25; i++)
                {
                    int j = 0;
                    if ((i * 60 - hour * 60 - minute) > 60)
                        return vremena;
                    while ((j < 60) && (matRadni[i][j] != -1))
                    {
                        if ((i > hour) || (minute <= matRadni[i][j]))
                        {

                            vremena.add((i + dodatak) * 100 + matRadni[i][j]);
                            brojac += ((i - hour) % 24) * 60 + minute - matRadni[i][j];
                            if ((i * 60 + matRadni[i][j] - hour * 60 - minute) > 60)
                                return vremena;
                        }
                        j++;
                    }
                }

                for (int i = 0; i < 3; i++)
                {
                    int j = 0;
                    if ((i * 60 - hour * 60 - minute) > 60)
                        return vremena;
                    while ((j < 60) && (matSubota[i][j] != -1))
                    {
                        if (minute <= matSubota[i][j])
                        {

                            vremena.add((i + 24) * 100 + matSubota[i][j]);
                            brojac += ((i - hour) % 24) * 60 + minute - matSubota[i][j];
                            if (((i + 24) * 60 + matSubota[i][j] - hour * 60 - minute) > 60)
                                return vremena;
                        }
                        j++;
                    }
                }
            } else
            {
                for (int i = hour; i < 25; i++)
                {
                    int j = 0;
                    if ((i * 60 - hour * 60 - minute) > 60)
                        return vremena;
                    while ((j < 60) && (matRadni[i][j] != -1))
                    {
                        if ((i > hour) || (minute <= matRadni[i][j]))
                        {

                            vremena.add((i + dodatak) * 100 + matRadni[i][j]);
                            brojac += ((i - hour) % 24) * 60 + minute - matRadni[i][j];
                            if ((i * 60 + matRadni[i][j] - hour * 60 - minute) > 60)
                                return vremena;
                        }
                        j++;
                    }
                }
            }

            hour = 3;
            minute = 0;
            day++;
            day = day % 7;
            dodatak = 24;
        }
		return vremena;
	}

    public double vratiTrenutnuBrzinu()
    {
        Calendar currentTime = Calendar.getInstance();

        return raspodelaBrzina[currentTime.get(Calendar.HOUR_OF_DAY)];
    }
}
