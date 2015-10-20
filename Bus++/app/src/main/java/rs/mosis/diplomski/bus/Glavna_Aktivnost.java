package rs.mosis.diplomski.bus;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;

import strukture.CSInfo;
import strukture.CoordTimestamp;
import strukture.Cvor;
import strukture.GradskeLinije;
import strukture.Linija;

public class Glavna_Aktivnost extends AppCompatActivity implements ActionBar.TabListener
{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    public static LatLng MyLocation = new LatLng(43.319425, 21.899487);
    public static final LatLng jugozapad = new LatLng(43.2659128, 21.7123964);
    public static final LatLng severoistok = new LatLng(43.4381218, 22.1044385);
    public static Handler UIHandler = new Handler(Looper.getMainLooper());
    LocationManager locationManager;
    LocationListener listener;
    public static Geocoder geocoder;
    private int trenutniTab = 0;
    private SharedPreferences preferences;
    public static ArrayList<String> linije = new ArrayList<>();

    private Komunikacija_Server komunikacija;

    private MapaFragment mapaFragment;
    private Odgovor_Servera odgovor_servera;
    private Linije linijeFragment;

    public static Glavna_Aktivnost otac;
    public double ukupniDojam = 0;

    public boolean landscape = true;
    public boolean promena = true;
    private static boolean promenaJezina = true;
    public static ArrayList<CoordTimestamp> kontrole;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glavna__aktivnost);

        preferences = MainActivity.preferences;
        int jez = preferences.getInt("jezik", -1);
        if (jez != -1)
        {
            // Constants.jezik = jez;
            if(promenaJezina)
                Podesavanja.setLocale(this, 1, jez);
            promenaJezina = !promenaJezina;
        }

        this.setTitle(getString(R.string.app_name));
        geocoder = new Geocoder(this, Locale.US);

        fragmentManager = getSupportFragmentManager();


        int rezim = preferences.getInt("mode", -1);
        if (rezim != -1)
            Constants.mode = rezim;

        komunikacija = MainActivity.komunikacija;


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
        {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.https://github.com/FilipStamenkovic/Klijent-BusPlusPlus
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        if ((otac != this) && (otac != null))
        {
            this.trenutniTab = otac.trenutniTab;
            this.landscape = otac.landscape;
            this.promena = otac.promena;
            this.locationManager = otac.locationManager;
            this.listener = otac.listener;
            this.mapaFragment = otac.mapaFragment;
            this.linijeFragment = otac.linijeFragment;
            this.odgovor_servera = otac.odgovor_servera;
        }

        otac = this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        //mSectionsPagerAdapter.getItem(trenutniTab);
        // promena = true;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        disconnect();
        startActivity(intent);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        disconnect();
    }

    public void postaviComplete(final String[] strings, final AutoCompleteTextView textView)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                ArrayAdapter<?> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_suggest, strings);


                textView.setAdapter(adapter);
                //textView.setDropDownHeight(textView.getHeight());


                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (locationManager == null)
        {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            listener = new LocationListener()
            {
                @Override
                public void onLocationChanged(Location location)
                {
                    MyLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras)
                {

                }

                @Override
                public void onProviderEnabled(String provider)
                {

                }

                @Override
                public void onProviderDisabled(String provider)
                {

                }
            };
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.TIMEOUT, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.TIMEOUT, 0, listener);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        int position = getSupportActionBar().getSelectedTab().getPosition();
        if (menu != null)
            menu.clear();

        switch (position)
        {
            case 0:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;
            case 1:
                getMenuInflater().inflate(R.menu.menu_glavna__aktivnost, menu);
                break;
            case 2:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void disconnect()
    {
        if (locationManager != null)
        {
            locationManager.removeUpdates(listener);
            locationManager = null;
            listener = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Constants.jezik = -1;
            Intent i = new Intent(this, Podesavanja.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_nadji_put)
        {
            final LatLng source = mapaFragment.getMyPosition();
            // Odgovor_Servera.tipZahteva = Constants.mode;
            if (MapaFragment.cilj == null)
            {
                Toast.makeText(this, getString(R.string.izaberi_cilj), Toast.LENGTH_LONG).show();
                return true;
            }
            final LatLng destination = MapaFragment.cilj.getPosition();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    final Response odgovor = komunikacija.napredniRezim(source, destination);


                    if (odgovor != null)
                    {
                        UIHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mapaFragment.obrisiPutovanje();
                                if (odgovor.type == -1)
                                {
                                    Toast.makeText(otac, otac.getString(R.string.no_more_tokens),
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                else
                                    Toast.makeText(otac,otac.getString(R.string.number_tokens)
                                                    + ": "+
                                            Constants.numberTokens,
                                            Toast.LENGTH_SHORT).show();
                            }
                        });

                        if (odgovor.type == 4)
                        {
                            odgovor_servera.pripremiEkonomicni(odgovor, source, destination, -1);
                        } else if (odgovor.type == 6)
                        {
                            odgovor_servera.pripremiOptimalni(odgovor, source, destination);
                        } else if (odgovor.type == 7)
                        {
                            odgovor_servera.pripremiOptimalni(odgovor, source, destination);
                        } else if (odgovor.type == 5)
                        {
                            odgovor_servera.pripremiEkoOpt(odgovor, source, destination, -1);
                        }
                    } else
                    {
                        UIHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (odgovor == null)
                                {
                                    Toast.makeText(otac.getApplicationContext(), "UPS, greska", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }


                }
            }).start();
        } else if (id == R.id.action_set_start)
        {
            mapaFragment.postaviStart(mapaFragment.getMyPosition());

        } else if (id == R.id.action_clean_map)
        {
            mapaFragment.removeAllFromMap();
            mapaFragment.deleteAllFromMap();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (trenutniTab != getSupportActionBar().getSelectedTab().getPosition())
            getSupportActionBar().setSelectedNavigationItem(trenutniTab);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        trenutniTab = tab.getPosition();
        mViewPager.setCurrentItem(tab.getPosition());
        if (tab.getPosition() == 2)
            Odgovor_Servera.tipZahteva = 0;


        if (MapaFragment.searchView != null)
        {
            if (MapaFragment.searchView.isFocused())
            {
                InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.aplikacija.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(MapaFragment.searchView.getWindowToken(), 0);
            }

            EditText text = (EditText) otac.findViewById(R.id.komentar);
            if (text != null)
            {
                InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.aplikacija.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(text.getWindowToken(), 0);
            }
        }

        invalidateOptionsMenu();

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
    }

    public void prikaziNaMapi(View v)
    {
        int id = v.getId();

        prikaziNaMapi(id);

    }

    public void prikaziNaMapi(int id)
    {
        UIHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                mapaFragment.obrisiPutovanje();
            }
        });
        Linija[] linije = MainActivity.graf.getGl().linije;
        ArrayList<Cvor> cvorovi;
        String smer = linije[id].smer;
        String broj = linije[id].broj;




        int size = linije.length;

        for (int i = 0; i < size; i++)
        {
            if ((linije[i] != null) && (i != id))
            {
                if ((linije[i].smer.equals(smer)) && (broj.equals(linije[i].broj.replace("*",""))))
                {
                    Random rnd = new Random();
                    int color = Color.argb(125, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

                    cvorovi = MainActivity.graf.pratiLiniju(i, -1, -1);

                    mapaFragment.pratiLiniju(i, cvorovi, 0,color);

                }
            }
        }

        cvorovi = MainActivity.graf.pratiLiniju(id, -1, -1);
        mapaFragment.pratiLiniju(id, cvorovi, 0,Constants.GoogleBlue);

        otac.getSupportActionBar().setSelectedNavigationItem(1);
    }

    public void toogle_content(View v)
    {
        View view = v.findViewById(R.id.kontenjer);
        if (view != null)
            view.setVisibility(view.isShown() ? View.GONE : View.VISIBLE);
    }


    public void redVoznje(View v)
    {
        int id = v.getId() - 1000;
        redVoznje(id);
    }

    public void redVoznje(final int id)
    {
        final LatLng sourceLatLng = mapaFragment.getMyPosition();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final Response odgovor = komunikacija.ObicanRedVoznje(sourceLatLng, id);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ArrayList<String> vremena = komunikacija.vremenaPolaska(odgovor);
                        ArrayList<String> korekcije = komunikacija.vremenaDolaska(odgovor, vremena);

                        //Odgovor_Servera.tipZahteva = 3;
                        otac.getSupportActionBar().setSelectedNavigationItem(2);
                        odgovor_servera.popuniTabelu(odgovor, vremena, korekcije);


                    }
                });

            }
        }).start();
    }

    public void onTextViewClicked(View view)
    {
        final int id = view.getId();
        String title = otac.getString(R.string.linija) + " " + MainActivity.graf.getGl().linije[id].broj;
        title += ", " + otac.getString(R.string.smer) + " " + MainActivity.graf.getGl().linije[id].smer;
        String naziv = MainActivity.graf.getGl().linije[id].naziv;

        AlertDialog dialog = new AlertDialog.Builder(otac)
                .setTitle(title)
                .setMessage(naziv)
                .setPositiveButton(R.string.view_red_voznje, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        redVoznje(id);
                    }
                })
                .setNegativeButton(R.string.show_linija, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        prikaziNaMapi(id);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
        dialog.getButton(DialogInterface.BUTTON_NEUTRAL).
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);

        dialog.setCanceledOnTouchOutside(false);
    }

    public void prikaziCrowdSourcing(View view)
    {
        RelativeLayout crowdSourcing = (RelativeLayout) ((RelativeLayout) view.getParent()).
                findViewById(R.id.crowd_sourcing);
        if (crowdSourcing != null)
            if (!crowdSourcing.isShown())
            {
                crowdSourcing.setVisibility(View.VISIBLE);
                mapaFragment.googleMap.getUiSettings().setAllGesturesEnabled(false);

            } else
            {
                crowdSourcing.setVisibility(View.GONE);
                mapaFragment.googleMap.getUiSettings().setAllGesturesEnabled(true);
                EditText text = (EditText) otac.findViewById(R.id.komentar);
                if (text != null)
                {
                    InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.aplikacija.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(text.getWindowToken(), 0);
                }
            }
    }

    public void sendInfo(View view)
    {
        RelativeLayout crowdSourcing = (RelativeLayout) otac.
                findViewById(R.id.crowd_sourcing);
        crowdSourcing.setVisibility(View.GONE);
        mapaFragment.googleMap.getUiSettings().setAllGesturesEnabled(true);

        final int guzva, klimatizovanost;
        final String smer;
        final boolean kontrola = ((CheckBox) crowdSourcing.findViewById(R.id.kontrola)).isChecked();
        if (((RadioButton) crowdSourcing.findViewById(R.id.smerA)).isChecked())
            smer = "A";
        else
            smer = "B";

        guzva = (int) ((RatingBar) crowdSourcing.findViewById(R.id.rating_guzva)).getRating();
        klimatizovanost = (int) ((RatingBar) crowdSourcing.findViewById(R.id.rating_klimatizovanost)).getRating();

        Spinner s = (Spinner) crowdSourcing.findViewById(R.id.broj_linije);
        String naziv = (String) s.getSelectedItem();

        final String linija = MainActivity.graf.getGl().getLinija(naziv);
        final String komentar = ((EditText)crowdSourcing.findViewById(R.id.komentar)).getText().toString();

        final LatLng source = mapaFragment.getMyPosition();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                komunikacija.sendInfo(linija, smer, guzva, klimatizovanost, source, kontrola,komentar);
            }
        }).start();


    }

    public void prikaziKontrole(View view)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (!Constants.zahvetKontrola)
                {
                    Response odgovor = komunikacija.zatraziKontrole();
                    if (odgovor == null)
                        return;

                    Glavna_Aktivnost.kontrole = odgovor.kontrola;

                    UIHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mapaFragment.postaviKontrole();
                        }
                    });


                }

                Constants.zahvetKontrola = !Constants.zahvetKontrola;
            }
        }).start();

        if (Constants.zahvetKontrola)
            mapaFragment.obrisiKontrole();



    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        public static final int brojFragmenta = 3;

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            Fragment fragment = null;
            switch (position)
            {
                case 0:
                    fragment = Linije.newInstance();
                    otac.linijeFragment = (Linije) fragment;
                    break;
                case 1:
                    fragment = MapaFragment.newInstance();
                    otac.mapaFragment = (MapaFragment) fragment;
                    break;
                case 2:
                    fragment = Odgovor_Servera.newInstance();
                    otac.odgovor_servera = (Odgovor_Servera) fragment;
                    break;
            }

            return fragment;
        }

        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return brojFragmenta;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            Locale l = Locale.getDefault();
            switch (position)
            {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class Linije extends Fragment
    {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        private static LinearLayout landscapeLayout = null;

        private static LinearLayout portraitLayout = null;


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Linije newInstance()
        {
            return new Linije();
        }

        public Linije()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            final View rootView;
            rootView = inflater.inflate(R.layout.fragment_red_voznje, container, false);
            boolean b = otac.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            popuniLayout(b, rootView, inflater, container);
           // otac.linijeFragment = this;
            return rootView;
        }

        private void popuniLayout(boolean portrait, View rootView,
                                  LayoutInflater inflater, ViewGroup container)
        {

            LinearLayout linearLayout;
            if (portrait)
                linearLayout = portraitLayout;
            else
                linearLayout = landscapeLayout;

            if (linearLayout == null)
            {
                Linija[] linije = MainActivity.graf.getGl().linije;
                ArrayList<Linija> listaLinija = new ArrayList<>();

                for (int i = 1; i < linije.length; i++)
                    if (linije[i].broj.charAt(linije[i].broj.length() - 1) == '*')
                        continue;
                    else
                    {
                        listaLinija.add(linije[i]);
                        if (!Glavna_Aktivnost.linije.contains(linije[i].broj))
                            Glavna_Aktivnost.linije.add(linije[i].broj);
                    }


                linearLayout = (LinearLayout) rootView.findViewById(R.id.linije_scroll);

                int size = listaLinija.size();

                for (int i = 0; i < size; i += 2)
                {
                    View red;
                    if (portrait)
                        red = inflater.inflate(R.layout.red_voznje_portrait_layout, container, false);
                    else
                        red = inflater.inflate(R.layout.red_voznje_layout, container, false);
                    TextView textView = (TextView) red.findViewWithTag("linija");
                    //   if (!portrait)
                    //       textView.setText(otac.getString(R.string.linija) + listaLinija.get(i).broj);
                    //    else
                    textView.setText(otac.getString(R.string.linija) + " " + listaLinija.get(i).broj);
                    textView = (TextView) red.findViewWithTag("smerA");
                    textView.setText(listaLinija.get(i).naziv);
                    textView = (TextView) red.findViewWithTag("smerB");
                    textView.setText(listaLinija.get(i + 1).naziv);
                    if (!portrait)
                    {
                        red.findViewWithTag("prikaziA").setId(listaLinija.get(i).id);
                        red.findViewWithTag("prikaziB").setId(listaLinija.get(i + 1).id);
                        red.findViewWithTag("pogledajA").setId(listaLinija.get(i).id + 1000);
                        red.findViewWithTag("pogledajB").setId(listaLinija.get(i + 1).id + 1000);
                    } else
                    {
                        red.findViewWithTag("smerA").setId(listaLinija.get(i).id);
                        red.findViewWithTag("smerB").setId(listaLinija.get(i + 1).id);

                        red.findViewWithTag("smerA").setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                otac.onTextViewClicked(v);
                            }
                        });
                        red.findViewWithTag("smerB").setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                otac.onTextViewClicked(v);
                            }
                        });
                    }
                    linearLayout.addView(red, i / 2);

                }
            } else
            {
                ScrollView skrol = (ScrollView) rootView.findViewById(R.id.skrol);
                skrol.removeViewAt(0);
                ((ScrollView) linearLayout.getParent()).removeAllViews();
                skrol.addView(linearLayout);
            }
        }

    }


    public static class MapaFragment extends Fragment
    {
        public static GoogleMap googleMap = null;

        public static ArrayList<Marker> stanice = null;

        private static ArrayList<Integer> pozicije = null;

        public static Polyline ruta = null;

        private static ArrayList<Polyline> rute = null;

        public static ArrayList<Circle> pesacenja = null;

        public static Marker cilj = null;

        public static Marker start = null;

        public static AutoCompleteTextView searchView;

        private static boolean inicijalizacija = true;

        private static ArrayList<Circle> kontroleKrugovi;

        private static ArrayList<Marker> kontroleMarkeri;


        public static MapaFragment newInstance()
        {
            MapaFragment fragment = new MapaFragment();
            return fragment;
        }

        public MapaFragment()
        {

        }

        public void obrisiPutovanje()
        {
            if (rute != null)
            {
                int size = rute.size();
                for (int i = 0; i < size; i++)
                    rute.get(i).remove();
                rute.clear();
                rute = null;
            }
            if (pesacenja != null)
            {
                int size = pesacenja.size();
                for (int i = 0; i < size; i++)
                    pesacenja.get(i).remove();
                pesacenja.clear();
                pesacenja = null;
            }
            if (stanice != null)
            {
                int size = stanice.size();
                for (int i = 0; i < size; i++)
                    stanice.get(i).remove();
                stanice.clear();
                stanice = null;
            }
        }

        public void obrisiKontrole()
        {
            if (kontroleKrugovi != null)
            {
                int size = kontroleKrugovi.size();
                for (int i = 0; i < size; i++)
                    kontroleKrugovi.get(i).remove();
                kontroleKrugovi.clear();
                kontroleKrugovi = null;
            }
            if (kontroleMarkeri != null)
            {
                int size = kontroleMarkeri.size();
                for (int i = 0; i < size; i++)
                    kontroleMarkeri.get(i).remove();
                kontroleMarkeri.clear();
                kontroleMarkeri = null;
            }
        }

        public synchronized void podesiInfo()
        {
            if (inicijalizacija)
            {
                if(otac.findViewById(R.id.rating_bars) == null)
                    return;
                int maxSirina = otac.findViewById(R.id.rating_bars).getWidth();
                if (maxSirina == 0)
                    return;


                int sirina = otac.findViewById(R.id.rating_ukupno).getWidth()
                        + otac.findViewById(R.id.primer).getWidth();

                inicijalizacija = false;
                if (sirina > maxSirina)
                    otac.promena = true;
                else
                {
                    otac.promena = false;

                    return;
                }

            }

            if ((otac.landscape) &&
                    (otac.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT))
            {
                RatingBar g = (RatingBar) otac.findViewById(R.id.rating_guzva);
                RatingBar k = (RatingBar) otac.findViewById(R.id.rating_klimatizovanost);
                RatingBar u = (RatingBar) otac.findViewById(R.id.rating_ukupno);

                float gRat, kRat, uRat;
                gRat = g.getRating();
                kRat = k.getRating();

                LinearLayout ratingBars = (LinearLayout) otac.findViewById(R.id.rating_bars);
                ratingBars.removeAllViewsInLayout();

                LayoutInflater layoutInflater = (LayoutInflater) otac
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


                LinearLayout dete = (LinearLayout) layoutInflater.inflate(R.layout.rating_bars_small, null);

                g.setOnRatingBarChangeListener(null);
                k.setOnRatingBarChangeListener(null);
                u.setOnRatingBarChangeListener(null);

                g = (RatingBar) dete.findViewById(R.id.rating_guzva);
                k = (RatingBar) dete.findViewById(R.id.rating_klimatizovanost);
                u = (RatingBar) dete.findViewById(R.id.rating_ukupno);

                ratingBars.addView(dete);
                otac.landscape = false;
                postaviListenerZaRatingBar(g, k, u);

                g.setRating(gRat);
                k.setRating(kRat);
            } else if ((!otac.landscape) &&
                    (otac.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
            {
                RatingBar g = (RatingBar) otac.findViewById(R.id.rating_guzva);
                RatingBar k = (RatingBar) otac.findViewById(R.id.rating_klimatizovanost);
                RatingBar u = (RatingBar) otac.findViewById(R.id.rating_ukupno);

                float gRat, kRat;
                gRat = g.getRating();
                kRat = k.getRating();

                LinearLayout ratingBars = (LinearLayout) otac.findViewById(R.id.rating_bars);
                ratingBars.removeAllViewsInLayout();

                LayoutInflater layoutInflater = (LayoutInflater) otac
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                LinearLayout dete = (LinearLayout) layoutInflater.inflate(R.layout.rating_bars_normal, null);


                g.setOnRatingBarChangeListener(null);
                u.setOnRatingBarChangeListener(null);
                k.setOnRatingBarChangeListener(null);

                g = (RatingBar) dete.findViewById(R.id.rating_guzva);
                k = (RatingBar) dete.findViewById(R.id.rating_klimatizovanost);
                u = (RatingBar) dete.findViewById(R.id.rating_ukupno);


                ratingBars.addView(dete);
                otac.landscape = true;
                postaviListenerZaRatingBar(g, k, u);

                g.setRating(gRat);
                k.setRating(kRat);
            }


        }

        public void postaviListenerZaRatingBar(RatingBar g, RatingBar k, RatingBar u)
        {
            final RatingBar guzva = g;
            final RatingBar klima = k;
            final RatingBar ukupno = u;

            guzva.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
            {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
                {
                    otac.ukupniDojam = (klima.getRating() + rating) / 2.0;
                    ukupno.setRating((float) otac.ukupniDojam);
                }
            });

            klima.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
            {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
                {
                    otac.ukupniDojam = (guzva.getRating() + rating) / 2.0;
                    ukupno.setRating((float) otac.ukupniDojam);
                }
            });
        }

        public LatLng getMyPosition()
        {
            LatLng sourceLatLng2 = null;
            if (start != null)
                sourceLatLng2 = MapaFragment.start.getPosition();
            if (sourceLatLng2 == null)
            {
                if (googleMap != null)
                    if (googleMap.getMyLocation() != null)
                        sourceLatLng2 = new LatLng(googleMap.getMyLocation().getLatitude(),
                                googleMap.getMyLocation().getLongitude());
            }
            if (sourceLatLng2 == null)
                sourceLatLng2 = MyLocation;

            return sourceLatLng2;
        }


        public void nacrtajRutu(final ArrayList<Cvor> cvorovi,final int color)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    DirectionsHelper directionsHelper;
                    final List<LatLng> lista;

                    directionsHelper = new DirectionsHelper(cvorovi);

                    lista = directionsHelper.getDriving();

                    UIHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {

                            nacrtajPoliliniju(lista,color);
                        }
                    });
                }
            }).start();
        }

        public void nacrtajPoliliniju(List<LatLng> lista,int color)
        {
            if (lista != null)
            {
                if (rute == null)
                    rute = new ArrayList<>();
                ruta = googleMap.addPolyline(new PolylineOptions()
                                .addAll(lista)
                                .width(12)
                                .color(color)//Google maps blue color
                                .geodesic(true)
                );

                rute.add(ruta);
                ruta = null;
            }
        }

        public void nacrtajPesacenje(final LatLng source, final LatLng dest)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    DirectionsHelper directionsHelper;
                    final List<LatLng> lista;

                    directionsHelper = new DirectionsHelper(source, dest);
                    lista = directionsHelper.getTacke("walking");
                    UIHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            postaviPesacenje(lista);
                        }
                    });
                }
            }).start();

        }


        public int pratiLiniju(final int id, ArrayList<Cvor> cvors, Integer presedanjeMode,int color)
        {
            final ArrayList<Cvor> cvorovi = new ArrayList<>();

            int size = cvors.size();

            for (int i = 0; i < size; i++)
                cvorovi.add(cvors.get(i));

            final int presedanje = presedanjeMode;
            if (presedanje == 3)
                presedanjeMode = 2;
            else if (presedanje > 0)
                presedanjeMode = (presedanje + 1) % 4;

            nacrtajRutu(cvorovi, color);

            UIHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    Linija linija = MainActivity.graf.getGl().linije[id];

                    int size = cvorovi.size();
                    if ((presedanje != 2) && (presedanje != 3))
                    {
                        if (!checkMarker(cvorovi.get(0).naziv))
                            postaviStanicu(cvorovi.get(0).lat,
                                    cvorovi.get(0).lon,
                                    cvorovi.get(0).naziv, 0, linija.naziv);
                    }
                    for (int i = 1; i < size - 1; i++)
                    {
                        Cvor cvor = cvorovi.get(i);
                        if (!checkMarker(cvor.naziv))
                            postaviStanicu(cvor.lat, cvor.lon, cvor.naziv, 2, linija.naziv);
                    }
                    if ((presedanje % 2) == 0)
                    {
                        if (!checkMarker(cvorovi.get(cvorovi.size() - 1).naziv))
                            postaviStanicu(cvorovi.get(cvorovi.size() - 1).lat,
                                    cvorovi.get(cvorovi.size() - 1).lon,
                                    cvorovi.get(cvorovi.size() - 1).naziv, 1, linija.naziv);
                    } else
                    {
                        postaviStanicu(cvorovi.get(cvorovi.size() - 1).lat,
                                cvorovi.get(cvorovi.size() - 1).lon,
                                cvorovi.get(cvorovi.size() - 1).naziv, 3,
                                otac.getString(R.string.presedanje));
                    }
                }
            });

            return presedanjeMode;

        }

        private boolean checkMarker(String naziv)
        {
            if (stanice == null)
                return false;

            for(Marker m : stanice)
            {
                if (m.getTitle().equals(naziv))
                    return true;
            }
            return false;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView;

            rootView = inflater.inflate(R.layout.fragment_mapa, container, false);
            setUpMap();
            setUpSearch(rootView);
            setUpInfo(rootView);
            return rootView;
        }


        private void setUpInfo(View rootView)
        {
            Spinner spinner = (Spinner) rootView.findViewById(R.id.broj_linije);
            if (spinner == null)
                return;
            if (spinner.getAdapter() == null)
            {
                ArrayList<String> nazivi = new ArrayList<>();
                int i = 1;
                for (String linija : linije)
                {
                    for (; i < MainActivity.graf.getGl().linije.length; i++)
                    {
                        Linija l = MainActivity.graf.getGl().linije[i];
                        if ((l.broj.equals(linija)) && (l.smer.equals("A")))
                        {
                            nazivi.add(l.naziv);
                            break;
                        }
                    }
                }
                ArrayAdapter<String> karant_adapter = new ArrayAdapter<String>(otac,
                        android.R.layout.simple_spinner_item, nazivi);
                spinner.setAdapter(karant_adapter);
            }

            rootView.findViewById(R.id.rating_bars).getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
                    {
                        @Override
                        public void onGlobalLayout()
                        {
                            if (otac.promena)
                                podesiInfo();
                        }
                    });

            RatingBar g = (RatingBar) rootView.findViewById(R.id.rating_guzva);
            RatingBar k = (RatingBar) rootView.findViewById(R.id.rating_klimatizovanost);
            RatingBar u = (RatingBar) rootView.findViewById(R.id.rating_ukupno);

            postaviListenerZaRatingBar(g, k, u);

            rootView.findViewById(R.id.sendInfo).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    otac.sendInfo(v);
                }
            });


        }

        private void setUpSearch(View root)
        {
            final SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
            searchView = (AutoCompleteTextView) ((ViewGroup) root).getChildAt(0);


            searchView.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {

                    if (searchView.getAdapter() != null)
                        if (searchView.getAdapter().getCount() > 0)
                        {
                            String s = searchView.getText().toString();
                            double lat, lon;

                            try
                            {
                                List<Address> adrese = geocoder.getFromLocationName(s, 1, jugozapad.latitude, jugozapad.longitude, severoistok.latitude, severoistok.longitude);
                                s = adrese.get(0).getLocality();
                                lat = adrese.get(0).getLatitude();
                                lon = adrese.get(0).getLongitude();
                                postaviCilj(lat, lon, s);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }

                        }

                    return false;
                }

                public boolean onKey(View v, int keyCode, KeyEvent event)
                {
                    return false;

                }
            });

            searchView.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    final String polje = s.toString();
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Geocoder geocoder = new Geocoder(getContext(), Locale.US);
                            List<Address> addresses;
                            try
                            {
                                addresses = geocoder.getFromLocationName(polje, 10, jugozapad.latitude, jugozapad.longitude, severoistok.latitude, severoistok.longitude);


                                ArrayList<String> stringovi = new ArrayList<String>();
                                int size = addresses.size();
                                for (int i = 0; i < size; i++)
                                {
                                    boolean b = false;
                                    String s = addresses.get(i).getFeatureName() + " - " + addresses.get(i).getLocality();
                                    s = s.replace(" ", "");
                                    int size2 = stringovi.size();
                                    for (int j = 0; j < size2; j++)
                                    {
                                        String s2 = stringovi.get(j);
                                        s2 = s2.replace(" ", "");
                                        if (s2.toLowerCase().equals(s.toLowerCase()))
                                        {
                                            b = true;
                                            break;
                                        }
                                    }
                                    if (!b)
                                        stringovi.add(addresses.get(i).getFeatureName() + " - " + addresses.get(i).getLocality());
                                }
                                final String[] strings = stringovi.toArray(new String[stringovi.size()]);

                                otac.postaviComplete(strings, searchView);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }

                @Override
                public void afterTextChanged(Editable s)
                {

                }
            });


            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextSubmit(String query)
                {
                    Geocoder geocoder = new Geocoder(getContext());
                    List<Address> addresses;
                    try
                    {
                        addresses = geocoder.getFromLocationName(query, 5);
                        Address lokacija = addresses.get(0);
                        Toast.makeText(getContext(), lokacija.getLatitude() + "     " + lokacija.getLongitude(), Toast.LENGTH_LONG).show();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText)
                {

                    return true;
                }
            };
        }

        private void setUpMap()
        {
            if (googleMap == null)
            {

                googleMap = ((SupportMapFragment) this.getChildFragmentManager().
                        findFragmentById(R.id.mapa)).getMap();
                if (googleMap != null)
                {
                    googleMap.setMyLocationEnabled(true);

                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation, 15));


                    googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
                    {
                        @Override
                        public void onMapLongClick(LatLng latLng)
                        {
                            if (searchView.isFocused())
                            {
                                InputMethodManager inputMethodManager = (InputMethodManager)
                                        MainActivity.aplikacija.getSystemService(Context.INPUT_METHOD_SERVICE);
                                inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                            }
                            postaviCilj(latLng.latitude, latLng.longitude, "");
                        }
                    });
                    googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener()
                    {
                        @Override
                        public void onMarkerDragStart(Marker marker)
                        {

                        }

                        @Override
                        public void onMarkerDrag(Marker marker)
                        {
                           googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                        }

                        @Override
                        public void onMarkerDragEnd(Marker marker)
                        {
                            if (start != null)
                            {
                                if (start.getTitle().equals(marker.getTitle()))
                                    start = marker;
                            } else if (cilj != null)
                            {
                                if (cilj.getTitle().equals(marker.getTitle()))
                                    cilj = marker;
                            }
                        }
                    });
                    rekonstrukcijaMape();
                }
            }

        }

        @Override
        public void onDestroyView()
        {
            super.onDestroyView();

            if (googleMap != null)
            {
                removeAllFromMap();
                googleMap.clear();
                googleMap = null;

            }

            RatingBar g = (RatingBar) otac.findViewById(R.id.rating_guzva);
            RatingBar k = (RatingBar) otac.findViewById(R.id.rating_klimatizovanost);
            RatingBar u = (RatingBar) otac.findViewById(R.id.rating_ukupno);
            if (g != null)
                g.setOnRatingBarChangeListener(null);
            if (k != null)
                k.setOnRatingBarChangeListener(null);
            if (u != null)
                u.setOnRatingBarChangeListener(null);
        }

        public void removeAllFromMap()
        {
            if (stanice != null)
            {
                int size = stanice.size();
                for (int i = 0; i < size; i++)
                    stanice.get(i).remove();

            }
            if (start != null)
            {
                start.remove();
            }
            if (cilj != null)
            {
                cilj.remove();
            }
            if (rute != null)
            {
                int size = rute.size();
                for (int i = 0; i < size; i++)
                    rute.get(i).remove();
            }
            if (pesacenja != null)
            {
                int size = pesacenja.size();
                for (int i = 0; i < size; i++)
                    pesacenja.get(i).remove();
            }

            if (kontroleKrugovi != null)
            {
                int size = kontroleKrugovi.size();
                for (int i = 0; i < size; i++)
                    kontroleKrugovi.get(i).remove();
            }
            if (kontroleMarkeri != null)
            {
                int size = kontroleMarkeri.size();
                for (int i = 0; i < size; i++)
                    kontroleMarkeri.get(i).remove();
            }

        }

        public void deleteAllFromMap()
        {
            if (stanice != null)
            {
                stanice.clear();
                stanice = null;
            }
            if (start != null)
            {
                start = null;
            }
            if (cilj != null)
            {
                cilj = null;
            }
            if (rute != null)
            {
                rute.clear();
                rute = null;
            }
            if (pesacenja != null)
            {
                pesacenja.clear();
                pesacenja = null;
            }

            if (kontroleKrugovi != null)
            {
                kontroleKrugovi.clear();
                kontroleKrugovi = null;
            }

            if (kontroleMarkeri != null)
            {
                kontroleMarkeri.clear();
                kontroleMarkeri = null;
            }
        }

        public void postaviStart(LatLng latLng)
        {
            if (latLng != null)
            {
                if (start != null)
                    start.remove();

                BitmapDescriptor ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_start_position);
                start = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(otac.getString(R.string.pocetak))
                        .icon(ikonica)
                        .draggable(true));

                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }

        public void postaviCilj(double lat, double lon, String s)
        {
            if (cilj != null)
                cilj.remove();

            BitmapDescriptor ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_odrediste);
            cilj = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title(otac.getString(R.string.finish_marker))
                    .snippet(s)
                    .icon(ikonica)
                    .draggable(true));

        }

        public void postaviStanicu(double lat, double lon, String naziv, int pozicija, String snipet)
        {

            if (stanice == null)
            {
                stanice = new ArrayList<>();
                pozicije = new ArrayList<>();
            }



            BitmapDescriptor ikonica;
            if (pozicija == 0)
                ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pocetak);
            else if (pozicija == 1)
                ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_kraj);
            else if (pozicija == 3)
                ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_presedanje);
            else
                ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_stajaliste);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title(naziv)
                    .icon(ikonica)
                    .snippet(snipet)
                    .draggable(false));

            stanice.add(marker);
            pozicije.add(new Integer(pozicija));

        }

        public void postaviPesacenje(List<LatLng> lista)
        {
            if (lista != null)
            {
                if (pesacenja == null)
                    pesacenja = new ArrayList<Circle>();
                int size = lista.size();
                for (int i = 0; i < size; i++)
                {
                    Circle circle = googleMap.addCircle(new CircleOptions()
                            .center(lista.get(i))
                            .radius(4)
                            .fillColor(0xFF33E5B5)
                            .strokeColor(0xFF33E5B5));
                    pesacenja.add(circle);
                }
            }
        }

        private void rekonstrukcijaMape()
        {
            if (start != null)
                postaviStart(start.getPosition());
            if (cilj != null)
            {
                double lat = cilj.getPosition().latitude;
                double lon = cilj.getPosition().longitude;
                postaviCilj(lat, lon, cilj.getSnippet());
            }
            if (rute != null)
            {
                int size = rute.size();
                for (int i = 0; i < size; i++)
                    nacrtajPoliliniju(rute.get(i).getPoints(),rute.get(i).getColor());
            }
            if (stanice != null)
            {
                int size = stanice.size();
                for (int i = 0; i < size; i++)
                {
                    Marker marker = stanice.get(0);
                    postaviStanicu(marker.getPosition().latitude, marker.getPosition().longitude, marker.getTitle(),
                            pozicije.get(0), marker.getSnippet());
                    stanice.remove(0);
                    pozicije.remove(0);
                }
            }
            if (pesacenja != null)
            {
                ArrayList<LatLng> lista = new ArrayList<>();
                int size = pesacenja.size();
                for (int i = 0; i < size; i++)
                {
                    LatLng latLng = pesacenja.get(0).getCenter();
                    lista.add(latLng);
                    pesacenja.remove(0);
                }
                postaviPesacenje(lista);
            }

            if (kontroleMarkeri != null)
            {
                postaviKontrole();
            }
        }

        public void postaviKontrole()
        {
            if((kontrole == null) || (kontrole.size() == 0))
            {
                Toast.makeText(otac, otac.getString(R.string.info_kontrole), Toast.LENGTH_LONG).show();
                return;
            }
            obrisiKontrole();

            int size = kontrole.size();
            if (size > 0)
            {
                kontroleMarkeri = new ArrayList<>();
                kontroleKrugovi = new ArrayList<>();

                for (CoordTimestamp coordTimestamp : kontrole)
                {
                    String sati;
                    if (coordTimestamp.cas < 9)
                        sati = "0" + coordTimestamp.cas + ":";
                    else
                        sati = coordTimestamp.cas + ":";
                    if (coordTimestamp.minut < 9)
                        sati += "0" + coordTimestamp.minut;
                    else
                        sati += coordTimestamp.minut + "";

                    String title = otac.getString(R.string.last_seen) + "\n" + sati;
                    postaviKontrolu(new LatLng(coordTimestamp.lat,coordTimestamp.lon),title);
                }

            }

        }

        private void postaviKontrolu(LatLng latLng, String title)
        {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .draggable(false));

            kontroleMarkeri.add(marker);

            Circle circle = googleMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(500)
                    .strokeColor(0xF0FF0000)
                    .fillColor(0x70ba2424));
            kontroleKrugovi.add(circle);

        }
    }

    public static class Odgovor_Servera extends Fragment
    {
        private static View v = null;

        private static int tipZahteva = 0;

        private static ArrayList<RadioButton> dugmici = null;

        private Response zadnjiResponse = null;


        public static Odgovor_Servera newInstance()
        {
            Odgovor_Servera fragment = new Odgovor_Servera();
            return fragment;
        }

        public Odgovor_Servera()
        {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView;
            rootView = inflater.inflate(R.layout.fragment_glavna__aktivnost, container, false);
            //v = rootView;
            if (tipZahteva == 0)
                if (v != null)
                {
                    if (v.getParent() != null)
                        ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) rootView).removeAllViewsInLayout();
                    ((RelativeLayout) rootView).addView(v);
                }
            return rootView;
        }

        public void popuniTabelu(Response odgovor, ArrayList<String> vremenaPolaska,
                                        ArrayList<String> vremenaDolaska)
        {
            zadnjiResponse = odgovor;
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            int crowdIndeks = -1;

            /*if (odgovor.crowdInfo != null)
            {
                crowdIndeks = 0;
                for (int i = 0; i < odgovor.crowdInfo.size(); i++)
                    Toast.makeText(otac,i + 1 + ". crowd   " + odgovor.crowdInfo.get(i).toString(),Toast.LENGTH_LONG).show();
            }*/
            if (v != null)
                ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
            v = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);

            v.findViewById(R.id.zaglavlje).setVisibility(View.VISIBLE);
            v.findViewById(R.id.info_o_liniji).setVisibility(View.VISIBLE);
            LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.informacije_linije, null);
            ((LinearLayout) v.findViewById(R.id.info_o_liniji)).addView(info);
            int pocetak, kraj;
            pocetak = 0;
            kraj = 0;
            int size = vremenaDolaska.size();
            for (int i = 0; i < size; i++)
            {

                LinearLayout kontenjer;
                if (i == 0)
                    kontenjer = (LinearLayout) v.findViewById(R.id.kontenjer);
                else
                {
                    View kopija = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);
                    kontenjer = (LinearLayout) kopija.findViewById(R.id.kontenjer);
                    ((RelativeLayout) kopija).removeAllViewsInLayout();
                    ((ScrollView) kontenjer.getParent()).removeAllViewsInLayout();

                }

                StringTokenizer tokenizerDolazak = new StringTokenizer(vremenaDolaska.get(i), "\n");
                StringTokenizer tokenizerPolazak = new StringTokenizer(vremenaPolaska.get(i), "\n");

                while (tokenizerDolazak.hasMoreTokens())
                {
                    String dolazak = tokenizerDolazak.nextToken();
                    String polazak = tokenizerPolazak.nextToken();

                    LinearLayout vreme_layout = (LinearLayout) layoutInflater.inflate(R.layout.fragment_vremena, null);
                    ((TextView) vreme_layout.findViewWithTag("vreme_dolaska")).setText(dolazak);
                    TextView textView = ((TextView) vreme_layout.findViewWithTag("vreme_polaska"));
                    textView.setText(polazak);

                    ((LinearLayout) kontenjer.findViewById(R.id.vremena)).addView(vreme_layout);
                    if ((crowdIndeks != -1) && (crowdIndeks < odgovor.crowdInfo.size()))
                    {
                        CSInfo csInfo = odgovor.crowdInfo.get(crowdIndeks);
                        String sati;
                        if (csInfo.cas < 9)
                            sati = "0" + csInfo.cas + ":";
                        else
                            sati = csInfo.cas + ":";
                        if (csInfo.minut < 9)
                            sati += "0" + csInfo.minut;
                        else
                            sati += csInfo.minut + "";

                        if (sati.equals(polazak.replace("*","")))
                        {
                            textView.setTextColor(Color.RED);
                            textView.setId(10000 + crowdIndeks);
                            textView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    int id = v.getId() - 10000;
                                    prikaziCrowdInfo(zadnjiResponse.crowdInfo.get(id));
                                }
                            });
                            crowdIndeks++;

                        }
                    }

                }

                int j = pocetak;
                for (; j < odgovor.stanice.length - 1; j++)
                    if (odgovor.stanice[j].intValue() == odgovor.stanice[j + 1].intValue())
                        kraj++;
                    else
                        break;

                j = pocetak;
                pocetak = kraj + 1;

                Cvor cvor = MainActivity.graf.getStanica(odgovor.stanice[i]);

                LinearLayout stanica = (LinearLayout) layoutInflater.inflate(R.layout.informacije_linije, null);
                stanica.findViewById(R.id.linija_id).setVisibility(View.GONE);
                stanica.findViewById(R.id.smer_id).setVisibility(View.GONE);
                ((TextView) stanica.findViewById(R.id.naziv_id)).setText(R.string.stanica);
                ((LinearLayout) kontenjer.findViewById(R.id.blok_za_stanicu)).addView(stanica);

                String naziv = cvor.naziv;
                stanica = (LinearLayout) layoutInflater.inflate(R.layout.informacije_linije, null);
                stanica.findViewById(R.id.linija_id).setVisibility(View.GONE);
                stanica.findViewById(R.id.smer_id).setVisibility(View.GONE);
                ((TextView) stanica.findViewById(R.id.naziv_id)).setText(Html.fromHtml("<u>" + naziv + "</u>"));
                ((LinearLayout) kontenjer.findViewById(R.id.blok_za_stanicu)).addView(stanica);

                stanica.setId(cvor.id);
                otac.mapaFragment.postaviStanicu(cvor.lat, cvor.lon, cvor.naziv, 0,
                        MainActivity.graf.getGl().linije[odgovor.linije[0]].naziv);

                stanica.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        otac.getSupportActionBar().setSelectedNavigationItem(1);
                        Cvor cvor = MainActivity.graf.getStanica(v.getId());

                        MapaFragment.googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(cvor.lat, cvor.lon)));


                    }
                });

                for (; j <= kraj; j++)
                {
                    if (odgovor.linije[j] != -1)
                    {
                        info = (LinearLayout) layoutInflater.inflate(R.layout.informacije_linije, null);
                        ((TextView) info.findViewById(R.id.linija_id)).setText(MainActivity.graf.getGl().linije[odgovor.linije[j]].broj);
                        ((TextView) info.findViewById(R.id.smer_id)).setText(MainActivity.graf.getGl().linije[odgovor.linije[j]].smer);
                        ((TextView) info.findViewById(R.id.naziv_id)).setText(MainActivity.graf.getGl().linije[odgovor.linije[j]].naziv);
                        ((LinearLayout) kontenjer.findViewById(R.id.info_o_liniji)).addView(info);
                    }
                }

                if (i > 0)
                    ((LinearLayout) v.findViewById(R.id.kontenjer)).addView(kontenjer);
            }

            tipZahteva = 0;

            if (otac.findViewById(R.id.wraper) != null)
            {
                if (v.getParent() != null)
                {
                    ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) otac.findViewById(R.id.wraper)).removeAllViewsInLayout();
                }

                ((RelativeLayout) otac.findViewById(R.id.wraper)).addView(v);
            }
        }

        private void prikaziCrowdInfo(CSInfo csInfo)
        {

            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.fragment_crowd_sourcing,null);

            ((RatingBar) view.findViewById(R.id.rating_guzva)).setRating(csInfo.crowded.floatValue());
            ((RatingBar) view.findViewById(R.id.rating_klimatizovanost)).setRating(csInfo.stuffy.floatValue());
            float ukupno = csInfo.crowded.floatValue() + csInfo.stuffy.floatValue();
            ukupno /= 2.0;

            ((RatingBar) view.findViewById(R.id.rating_ukupno)).setRating(ukupno);



            ((TextView) view.findViewById(R.id.komentar)).setText(csInfo.message.replace("+busSEPARATOR+","\n"));

            AlertDialog dialog = new AlertDialog.Builder(otac)
                    .setTitle(otac.getString(R.string.bus_informations))
                    //.setMessage(naziv)
                    .setView(view)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();

        }

        public void prikazekonomicnog(final Response odgovor, ArrayList<String> vremenaPolaska,
                                      ArrayList<String> vremenaDolaska, final LatLng source, final LatLng destination)
        {
            if (v != null)
                ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) otac.findViewById(R.id.pager).getRootView();
            Cvor polazna = MainActivity.graf.getStanica(odgovor.stanice[0]);
            Cvor dolazna = MainActivity.graf.getStanica(odgovor.stanice[odgovor.stanice.length - 1]);
            View view = layoutInflater.inflate(R.layout.fragment_ekonomicni, container, false);

            int pocetak = 0;
            int kraj = 0;
            int size = vremenaDolaska.size();
            for (int i = 0; i < size; i++)
            {
                LinearLayout kontenjer;
                if (i == 0)
                    kontenjer = (LinearLayout) view.findViewById(R.id.kontenjer);
                else
                {
                    View kopija = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);
                    kontenjer = (LinearLayout) kopija.findViewById(R.id.kontenjer);
                    ((ScrollView) kontenjer.getParent()).removeAllViewsInLayout();
                }


                int j = pocetak;
                for (; j < odgovor.linije.length - 1; j++)
                    if ((odgovor.linije[j] > -1) && (odgovor.linije[j + 1] > -1))
                        if (GradskeLinije.istaOsnovna(odgovor.linije[j], odgovor.linije[j + 1]))
                            kraj++;
                        else
                            break;

                j = pocetak;
                pocetak = kraj + 1;
                int l = j;

                String dolazak = vremenaDolaska.get(i);
                String polazak = vremenaPolaska.get(i);
                boolean []izostavi = new boolean[odgovor.linije.length];
                for(int ii = 0; ii < izostavi.length; ii++)
                    izostavi[i] = false;
                for (j = l + 1; j <= kraj; j++)
                {
                    int brojZvezdica = j - l;
                    String zvezdice = "";
                    for (int z = 0; z < brojZvezdica; z++)
                        zvezdice += "*";
                    String proba = dolazak.replace(zvezdice, "");
                    if (proba.length() == dolazak.length())
                        izostavi[j] = true;
                }
                j = l;
                StringTokenizer tokenizerDolazak = new StringTokenizer(dolazak, "\n");
                StringTokenizer tokenizerPolazak = new StringTokenizer(polazak, "\n");

                while (tokenizerDolazak.hasMoreTokens())
                {
                    dolazak = tokenizerDolazak.nextToken();
                    polazak = tokenizerPolazak.nextToken();
                    int pomak = dolazak.length() - dolazak.replace("*", "").length();


                    if (!izostavi[j + pomak])
                    {
                        LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.slicke, null);
                        LinearLayout ikone = (LinearLayout) info.findViewById(R.id.slicice);
                        ImageView imageView = new ImageView(otac);
                        imageView.setImageResource(R.mipmap.ic_walking);


                        dodajDeonicu(R.mipmap.ic_walking, otac.getString(R.string.start), polazna.naziv, "",
                                "", otac.getString(R.string.pesacenje),
                                (LinearLayout) info.findViewById(R.id.kontenjer));

                        ikone.addView(imageView);
                        int resurs = MainActivity.ikonice[odgovor.linije[j + pomak]];
                        imageView = new ImageView(otac);
                        imageView.setImageResource(resurs);

                        dodajDeonicu(resurs, polazna.naziv, dolazna.naziv, dolazak,
                                "", MainActivity.graf.getGl().linije[odgovor.linije[j + pomak]].naziv,
                                (LinearLayout) info.findViewById(R.id.kontenjer));

                        ikone.addView(imageView);
                        imageView = new ImageView(otac);
                        imageView.setImageResource(R.mipmap.ic_walking);

                        dodajDeonicu(R.mipmap.ic_walking, dolazna.naziv, otac.getString(R.string.cilj), "",
                                "", otac.getString(R.string.pesacenje),
                                (LinearLayout) info.findViewById(R.id.kontenjer));

                        ikone.addView(imageView);

                        ((TextView) info.findViewById(R.id.vremena)).setText(dolazak);


                        kontenjer.addView(info);

                        RadioButton button = (RadioButton) layoutInflater
                                .inflate(R.layout.show_on_map, null).findViewById(R.id.dugme);
                        ((LinearLayout) button.getParent()).removeAllViewsInLayout();
                        if (i == 0)
                            button.setChecked(true);
                        dugmici.add(button);

                        button.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                int size = dugmici.size();
                                for (int i = 0; i < size; i++)
                                {
                                    RadioButton radio = dugmici.get(i);
                                    if (radio.isChecked())
                                        radio.setChecked(false);
                                    RadioButton dugme = (RadioButton) v;
                                    if (dugme == radio)
                                    {
                                        dugme.setChecked(true);
                                        pripremiEkonomicni(odgovor, source, destination, i);
                                        otac.getSupportActionBar().setSelectedNavigationItem(1);
                                    }
                                }
                            }
                        });

                        ((LinearLayout) info.findViewById(R.id.kontenjer)).addView(button);

                    }
                }

                if (i > 0)
                    ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(kontenjer);

            }
            v = view;
            if (otac.findViewById(R.id.wraper) != null)
            {
                if (v.getParent() != null)
                {
                    ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) otac.findViewById(R.id.wraper)).removeAllViewsInLayout();
                }

                ((RelativeLayout) otac.findViewById(R.id.wraper)).addView(v);
            }
        }

        private void dodajDeonicu(int resurs, String pocetak, String kraj, String vremePocetak,
                                         String vremeKraj, String linija, LinearLayout view)
        {
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout deonica = (LinearLayout) layoutInflater.inflate(R.layout.fragment_deonica, null);

            ((ImageView) deonica.findViewById(R.id.slicica)).setImageResource(resurs);
            ((TextView) deonica.findViewById(R.id.linija)).setText(linija);
            ((TextView) deonica.findViewById(R.id.vremena)).setText(vremePocetak + "\n" + vremeKraj);
            ((TextView) deonica.findViewById(R.id.stanice)).setText(pocetak + "\n" + kraj);

            view.addView(deonica);

        }
        private void dodajDeonicuCrowd(int resurs, String pocetak, String kraj, String vremePocetak,
                                       String vremeKraj, String linija, LinearLayout view,int pomak)
        {
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout deonica = (LinearLayout) layoutInflater.inflate(R.layout.fragment_deonica_crowd, null);

            ((ImageView) deonica.findViewById(R.id.slicica)).setImageResource(resurs);
            ((TextView) deonica.findViewById(R.id.linija)).setText(linija);
            TextView textView = ((TextView) deonica.findViewById(R.id.vremena));
            textView.setText(vremePocetak + "\n" + vremeKraj);
            ImageView info = ((ImageView) deonica.findViewById(R.id.slicica_info));

            String tag = pomak + "";
            info.setTag(tag);
            info.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    String tag = (String) v.getTag();
                    int id = Integer.parseInt(tag);
                    prikaziCrowdInfo(zadnjiResponse.vremenaDolaska.get(id).crowdInfo);
                }
            });
            ((TextView) deonica.findViewById(R.id.stanice)).setText(pocetak + "\n" + kraj);

            view.addView(deonica);
        }

        public void pripremiEkonomicni(final Response odgovor,
                                              final LatLng source, final LatLng destination,int linija)
        {
            if (odgovor != null)
            {
                final ArrayList<ArrayList<Cvor>> listaCvorova = new ArrayList<>();
                final ArrayList<Integer> linije_id = new ArrayList<>();
                if (odgovor != null)
                {
                    listaCvorova.add(MainActivity.graf.pratiLiniju(odgovor.linije[0], odgovor.stanice[0], odgovor.stanice[1]));
                    linije_id.add(Integer.valueOf(odgovor.linije[0]));
                    for (int i = 1; i < odgovor.linije.length; i++)
                        if (!GradskeLinije.istaOsnovna(odgovor.linije[i],odgovor.linije[i-1]))
                        {
                            listaCvorova.add(MainActivity.graf.pratiLiniju(odgovor.linije[i], odgovor.stanice[0], odgovor.stanice[1]));
                            linije_id.add(Integer.valueOf(odgovor.linije[i]));
                        }


                    Cvor pocetna = MainActivity.graf.getStanica(odgovor.stanice[0]);
                    otac.mapaFragment.nacrtajPesacenje(source, new LatLng(pocetna.lat, pocetna.lon));
                    int size = listaCvorova.size();
                    Cvor krajnja = MainActivity.graf.getStanica(odgovor.stanice[1]);
                    otac.mapaFragment.nacrtajPesacenje(new LatLng(krajnja.lat, krajnja.lon), destination);


                    if (linija == -1)
                    {
                        otac.mapaFragment.pratiLiniju(linije_id.get(0),
                                listaCvorova.get(0), 0, Constants.GoogleBlue);
                        if (dugmici != null)
                            dugmici.clear();
                        dugmici = new ArrayList<>();
                    }
                    else
                    {
                        otac.mapaFragment.obrisiPutovanje();
                        for (int i = 0; i < size; i++)
                            if (linija == i)
                            {
                                otac.mapaFragment.pratiLiniju(linije_id.get(i),
                                        listaCvorova.get(i), 0, Constants.GoogleBlue);
                                break;
                            }
                    }
                }
                if (linija == -1)
                {

                    UIHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {

                            prikazEkonomicnog(odgovor, source, destination, linije_id);


                        }
                    });
                }
            }
        }

        private void prikazEkonomicnog(final Response odgovor, final LatLng source, final LatLng destination, ArrayList<Integer> linijeId)
        {
            if (v != null)
                ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) otac.findViewById(R.id.pager).getRootView();
            Cvor polazna = MainActivity.graf.getStanica(odgovor.stanice[0]);
            Cvor dolazna = MainActivity.graf.getStanica(odgovor.stanice[odgovor.stanice.length - 1]);
            View view = layoutInflater.inflate(R.layout.fragment_ekonomicni, container, false);

            int size = linijeId.size();
            for (int i = 0; i < size; i++)
            {
                int LINIJAID = linijeId.get(i);
                LinearLayout kontenjer;
                if (i == 0)
                    kontenjer = (LinearLayout) view.findViewById(R.id.kontenjer);
                else
                {
                    View kopija = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);
                    kontenjer = (LinearLayout) kopija.findViewById(R.id.kontenjer);
                    ((ScrollView) kontenjer.getParent()).removeAllViewsInLayout();
                }

                LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.slicke, null);
                LinearLayout ikone = (LinearLayout) info.findViewById(R.id.slicice);
                ImageView imageView = new ImageView(otac);
                imageView.setImageResource(R.mipmap.ic_walking);


                dodajDeonicu(R.mipmap.ic_walking, otac.getString(R.string.start), polazna.naziv, "",
                        "", otac.getString(R.string.pesacenje),
                        (LinearLayout) info.findViewById(R.id.kontenjer));

                ikone.addView(imageView);
                int resurs = MainActivity.ikonice[LINIJAID];
                imageView = new ImageView(otac);
                imageView.setImageResource(resurs);

                dodajDeonicu(resurs, polazna.naziv, dolazna.naziv, "",
                        "", MainActivity.graf.getGl().linije[LINIJAID].naziv,
                        (LinearLayout) info.findViewById(R.id.kontenjer));

                ikone.addView(imageView);
                imageView = new ImageView(otac);
                imageView.setImageResource(R.mipmap.ic_walking);

                dodajDeonicu(R.mipmap.ic_walking, dolazna.naziv, otac.getString(R.string.cilj), "",
                        "", otac.getString(R.string.pesacenje),
                        (LinearLayout) info.findViewById(R.id.kontenjer));

                ikone.addView(imageView);

                ((TextView) info.findViewById(R.id.vremena)).setText("");


                kontenjer.addView(info);

                RadioButton button = (RadioButton) layoutInflater
                        .inflate(R.layout.show_on_map, null).findViewById(R.id.dugme);
                ((LinearLayout) button.getParent()).removeAllViewsInLayout();
                if (size > 1)
                {
                    if (i == 0)
                        button.setChecked(true);
                    dugmici.add(button);

                    button.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            int size = dugmici.size();
                            for (int i = 0; i < size; i++)
                            {
                                RadioButton radio = dugmici.get(i);
                                if (radio.isChecked())
                                    radio.setChecked(false);
                                RadioButton dugme = (RadioButton) v;
                                if (dugme == radio)
                                {
                                    dugme.setChecked(true);
                                    pripremiEkonomicni(odgovor, source, destination, i);
                                    otac.getSupportActionBar().setSelectedNavigationItem(1);
                                }
                            }
                        }
                    });
                }

                ((LinearLayout) info.findViewById(R.id.kontenjer)).addView(button);


                if (i > 0)
                    ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(kontenjer);

            }
            v = view;
            if (otac.findViewById(R.id.wraper) != null)
            {
                if (v.getParent() != null)
                {
                    ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) otac.findViewById(R.id.wraper)).removeAllViewsInLayout();
                }

                ((RelativeLayout) otac.findViewById(R.id.wraper)).addView(v);
            }
        }

        public void pripremiOptimalni(final Response odgovor,
                                             final LatLng source, final LatLng destination)
        {
            Integer presedanjeMode = 0;
            final ArrayList<Cvor> cvorovi = new ArrayList<>();
            for (int i = 1; i < odgovor.stanice.length; i++)
                if (odgovor.linije[i] == null)
                {
                    LatLng pocetak, kraj;
                    if (odgovor.stanice[i - 1] == -1)
                        pocetak = source;
                    else
                    {
                        Cvor stanica = MainActivity.graf.getStanica(odgovor.stanice[i - 1]);
                        pocetak = new LatLng(stanica.lat, stanica.lon);
                    }
                    if (odgovor.stanice[i] == -2)
                        kraj = destination;
                    else
                    {
                        Cvor stanica = MainActivity.graf.getStanica(odgovor.stanice[i]);
                        kraj = new LatLng(stanica.lat, stanica.lon);
                        cvorovi.add(stanica);
                    }
                    otac.mapaFragment.nacrtajPesacenje(pocetak, kraj);
                    presedanjeMode = 0;

                } else
                {
                    if (odgovor.linije[i] == odgovor.linije[i + 1])
                    {
                        cvorovi.add(MainActivity.graf.getStanica(odgovor.stanice[i]));
                        continue;
                    }
                    cvorovi.add(MainActivity.graf.getStanica(odgovor.stanice[i]));

                    if (odgovor.linije[i + 1] != null)
                        presedanjeMode++;

                    presedanjeMode = otac.mapaFragment.pratiLiniju(odgovor.linije[i],
                            cvorovi, presedanjeMode,Constants.GoogleBlue);
                    cvorovi.clear();
                    if (odgovor.linije[i + 1] != null)
                        cvorovi.add(MainActivity.graf.getStanica(odgovor.stanice[i]));

                }

            UIHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (odgovor.type == 6)
                        prikaziOptimalni(odgovor, source, destination);
                    else
                        prikaziMinWalk(odgovor, source, destination);
                }
            });
        }

        private void prikaziMinWalk(Response odgovor, LatLng source, LatLng destination)
        {
            if (v != null)
                ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) otac.findViewById(R.id.pager).getRootView();

            View view = layoutInflater.inflate(R.layout.fragment_ekonomicni, container, false);

            LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.slicke, null);
            LinearLayout ikone = (LinearLayout) info.findViewById(R.id.slicice);
            ImageView imageView = new ImageView(otac);
            imageView.setImageResource(R.mipmap.ic_walking);

            ikone.addView(imageView);
            boolean bb = false;
            for (int i = 2; i < odgovor.linije.length - 1; i++)
            {
                bb = true;
                if (odgovor.linije[i] == odgovor.linije[i + 1])
                    continue;
                if (odgovor.linije[i] == null)
                {
                    imageView = new ImageView(otac);
                    imageView.setImageResource(R.mipmap.ic_walking);
                    ikone.addView(imageView);
                } else
                {
                    int resurs = MainActivity.ikonice[odgovor.linije[i]];
                    imageView = new ImageView(otac);
                    imageView.setImageResource(resurs);
                    ikone.addView(imageView);
                }
            }

            if (!bb)
            {
                ((TextView) info.findViewById(R.id.vremena)).setText(otac.getString(R.string.pesacenje));
            }else
                ((TextView) info.findViewById(R.id.vremena)).setText("");

            imageView = new ImageView(otac);
            imageView.setImageResource(R.mipmap.ic_walking);

            if (bb)
                ikone.addView(imageView);


            ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(info);
            Cvor polazna = new Cvor();
            polazna.naziv = otac.getString(R.string.cilj);
            if (odgovor.stanice.length > 3)
            {
                polazna = MainActivity.graf.getStanica(odgovor.stanice[1]);
            }
            dodajDeonicu(R.mipmap.ic_walking, otac.getString(R.string.start), polazna.naziv, "",
                    "", otac.getString(R.string.pesacenje),
                    (LinearLayout) info.findViewById(R.id.kontenjer));


            LinearLayout kontenjer = null;
            boolean b = false;

            Cvor dolazna = null;
            for (int i = 0; i < odgovor.linije.length - 1; i++)
            {
                if (odgovor.linije[i] == null)
                    continue;
                if (odgovor.linije[i] == odgovor.linije[i + 1])
                    continue;
                if (kontenjer == null)
                    kontenjer = (LinearLayout) view.findViewById(R.id.kontenjer);
                else
                {
                    View kopija = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);
                    kontenjer = (LinearLayout) kopija.findViewById(R.id.kontenjer);
                    ((ScrollView) kontenjer.getParent()).removeAllViewsInLayout();
                }

                int resurs = MainActivity.ikonice[odgovor.linije[i]];
                dolazna = MainActivity.graf.getStanica(odgovor.stanice[i]);


                dodajDeonicu(resurs, polazna.naziv, dolazna.naziv, "",
                        "", MainActivity.graf.getGl().linije[odgovor.linije[i]].naziv,
                        (LinearLayout) info.findViewById(R.id.kontenjer));

                if ((odgovor.stanice[i + 1] > 0) && (odgovor.linije[i + 1] == null))
                {
                    polazna = MainActivity.graf.getStanica(odgovor.stanice[i + 1]);
                    dodajDeonicu(R.mipmap.ic_walking, dolazna.naziv, polazna.naziv, "",
                            "", otac.getString(R.string.pesacenje),
                            (LinearLayout) info.findViewById(R.id.kontenjer));
                } else
                {
                    polazna = dolazna;
                }

                if (b)
                    ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(kontenjer);
                else
                    b = true;
            }
            if (bb)
                dodajDeonicu(R.mipmap.ic_walking, polazna.naziv, otac.getString(R.string.cilj)
                        , "", "", otac.getString(R.string.pesacenje),
                        (LinearLayout) info.findViewById(R.id.kontenjer));

            v = view;
            if (otac.findViewById(R.id.wraper) != null)
            {
                if (v.getParent() != null)
                {
                    ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) otac.findViewById(R.id.wraper)).removeAllViewsInLayout();
                }

                ((RelativeLayout) otac.findViewById(R.id.wraper)).addView(v);
            }
        }

        public void prikaziOptimalni(Response odgovor, LatLng source, LatLng destination)
        {
            zadnjiResponse = odgovor;
            if (v != null)
                ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) otac.findViewById(R.id.pager).getRootView();

            View view = layoutInflater.inflate(R.layout.fragment_ekonomicni, container, false);

            LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.slicke, null);
            LinearLayout ikone = (LinearLayout) info.findViewById(R.id.slicice);
            ImageView imageView = new ImageView(otac);
            imageView.setImageResource(R.mipmap.ic_walking);

            String sati, minuti;
            if (hour >= 10)
                sati = hour + "";
            else
                sati = "0" + hour;
            if (minute >= 10)
                minuti = "" + minute;
            else
                minuti = "0" + minute;

            ikone.addView(imageView);
            int pomak = 0;
            for (int i = 2; i < odgovor.linije.length - 1; i++)
            {
                if (odgovor.linije[i] == odgovor.linije[i + 1])
                    continue;
                if (odgovor.linije[i] == null)
                {
                    imageView = new ImageView(otac);
                    imageView.setImageResource(R.mipmap.ic_walking);
                    ikone.addView(imageView);
                } else
                {
                    int resurs = MainActivity.ikonice[odgovor.vremenaDolaska.get(pomak++).linija];
                    imageView = new ImageView(otac);
                    imageView.setImageResource(resurs);
                    ikone.addView(imageView);
                }
            }

            pomak = 0;
            imageView = new ImageView(otac);
            imageView.setImageResource(R.mipmap.ic_walking);


            ikone.addView(imageView);
            String polazak = sati + ":" + minuti;
            int korekcija = odgovor.size.intValue();

            hour += korekcija / 3600;
            korekcija = korekcija % 3600;
            minute += korekcija / 60;

            while (minute > 59)
            {
                hour = (hour + 1) % 24;
                minute -= 60;
            }

            if (hour >= 10)
                sati = hour + "";
            else
                sati = "0" + hour;
            if (minute >= 10)
                minuti = "" + minute;
            else
                minuti = "0" + minute;
            String dolazak = sati + ":" + minuti;

            ((TextView) info.findViewById(R.id.vremena)).setText(polazak + " - " + dolazak);


            ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(info);
            Cvor polazna = new Cvor();
            polazna.naziv = otac.getString(R.string.cilj);
            if ((odgovor.vremenaDolaska != null) && (odgovor.vremenaDolaska.size() > 0))
            {
                polazna = MainActivity.graf.getStanica(odgovor.vremenaDolaska.get(0).stanica.intValue());
                dolazak = odgovor.vremenaDolaska.get(0).sat + ":" + odgovor.vremenaDolaska.get(0).minut;
            }
            dodajDeonicu(R.mipmap.ic_walking, otac.getString(R.string.start), polazna.naziv, polazak,
                    "", otac.getString(R.string.pesacenje),
                    (LinearLayout) info.findViewById(R.id.kontenjer));


            LinearLayout kontenjer = null;
            boolean b = false;

            Cvor dolazna = null;
            for (int i = 0; i < odgovor.linije.length - 1; i++)
            {
                if (odgovor.linije[i] == null)
                    continue;
                if (odgovor.linije[i] == odgovor.linije[i + 1])
                    continue;
                if (kontenjer == null)
                    kontenjer = (LinearLayout) view.findViewById(R.id.kontenjer);
                else
                {
                    View kopija = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);
                    kontenjer = (LinearLayout) kopija.findViewById(R.id.kontenjer);
                    ((ScrollView) kontenjer.getParent()).removeAllViewsInLayout();
                }

                int resurs = MainActivity.ikonice[odgovor.linije[i]];
                dolazna = MainActivity.graf.getStanica(odgovor.stanice[i]);

                if (odgovor.vremenaDolaska.get(pomak).sat > 9)
                    sati = odgovor.vremenaDolaska.get(pomak).sat + "";
                else
                    sati = "0" + odgovor.vremenaDolaska.get(pomak).sat;

                if (odgovor.vremenaDolaska.get(pomak).minut > 9)
                    minuti = odgovor.vremenaDolaska.get(pomak).minut + "";
                else
                    minuti = "0" + odgovor.vremenaDolaska.get(pomak).minut;


                dolazak = sati + ":" + minuti;

                if (odgovor.vremenaDolaska.get(pomak).crowdInfo == null)
                    dodajDeonicu(resurs, polazna.naziv, dolazna.naziv, dolazak,
                        "", MainActivity.graf.getGl().linije[odgovor.linije[i]].naziv,
                        (LinearLayout) info.findViewById(R.id.kontenjer));
                else
                    dodajDeonicuCrowd(resurs, polazna.naziv, dolazna.naziv, dolazak,
                            "", MainActivity.graf.getGl().linije[odgovor.linije[i]].naziv,
                            (LinearLayout) info.findViewById(R.id.kontenjer),
                            pomak );

                if ((odgovor.stanice[i + 1] > 0) && (odgovor.linije[i + 1] == null))
                {
                    polazna = MainActivity.graf.getStanica(odgovor.stanice[i + 1]);
                    dodajDeonicu(R.mipmap.ic_walking, dolazna.naziv, polazna.naziv, "",
                            "", otac.getString(R.string.pesacenje),
                            (LinearLayout) info.findViewById(R.id.kontenjer));
                }

                if (odgovor.vremenaDolaska.size() > ++pomak)
                    polazna = MainActivity.graf.getStanica(odgovor.vremenaDolaska.get(pomak).stanica);

                if (b)
                    ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(kontenjer);
                else
                    b = true;
            }

            if ((odgovor.vremenaDolaska != null) && (odgovor.vremenaDolaska.size() > 0))
            {
                dodajDeonicu(R.mipmap.ic_walking, dolazna.naziv, otac.getString(R.string.cilj)
                        , "", "", otac.getString(R.string.pesacenje),
                        (LinearLayout) info.findViewById(R.id.kontenjer));
            }
            v = view;
            if (otac.findViewById(R.id.wraper) != null)
            {
                if (v.getParent() != null)
                {
                    ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) otac.findViewById(R.id.wraper)).removeAllViewsInLayout();
                }

                ((RelativeLayout) otac.findViewById(R.id.wraper)).addView(v);
            }
        }



        public void pripremiEkoOpt(final Response odgovor, final LatLng source, final LatLng destination, int linija)
        {

            int i;
            if (linija == -1)
            {
                i = 0;
                if (dugmici != null)
                    dugmici.clear();
                dugmici = new ArrayList<>();
            } else
            {
                i = linija;
                otac.mapaFragment.obrisiPutovanje();
            }

            if (odgovor.linije[i] == null)
            {
                otac.mapaFragment.nacrtajPesacenje(source, destination);
            } else
            {
                Cvor pocetna = MainActivity.graf.getStanica(odgovor.stanice[2 * i]);
                Cvor krajnja = MainActivity.graf.getStanica(odgovor.stanice[2 * i + 1]);
                ArrayList<Cvor> cvorovi =
                        MainActivity.graf.pratiLiniju(odgovor.linije[i], pocetna.id, krajnja.id);
                otac.mapaFragment.nacrtajPesacenje(source, (new LatLng(pocetna.lat, pocetna.lon)));
                otac.mapaFragment.pratiLiniju(odgovor.linije[i], cvorovi, 0,Constants.GoogleBlue);

                otac.mapaFragment.nacrtajPesacenje((new LatLng(krajnja.lat, krajnja.lon)),
                        destination);
            }


            if (linija == -1)
            {
                UIHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        prikaziEkoOpt(odgovor, source, destination);
                    }

                });
            }
        }

        private void prikaziEkoOpt(final Response odgovor, final LatLng source, final LatLng destination)
        {

            zadnjiResponse = odgovor;
            if (v != null)
                ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) otac.findViewById(R.id.pager).getRootView();

            View view = layoutInflater.inflate(R.layout.fragment_ekonomicni, container, false);

            String sati, minuti;
            if (hour >= 10)
                sati = hour + "";
            else
                sati = "0" + hour;
            if (minute >= 10)
                minuti = "" + minute;
            else
                minuti = "0" + minute;

            for (int i = 0; i < odgovor.linije.length; i++)
            {
                LinearLayout kontenjer;
                if (i == 0)
                    kontenjer = (LinearLayout) view.findViewById(R.id.kontenjer);
                else
                {
                    View kopija = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);
                    kontenjer = (LinearLayout) kopija.findViewById(R.id.kontenjer);
                    ((ScrollView) kontenjer.getParent()).removeAllViewsInLayout();
                }

                int dolazakSati, dolazakMinuti;


                dolazakSati = hour;
                dolazakMinuti = minute + odgovor.korekcije[i] / 60;
                while (dolazakMinuti > 59)
                {
                    dolazakSati = (dolazakSati + 1) % 24;
                    dolazakMinuti -= 60;
                }

                String krajDolazak;

                if (dolazakSati > 9)
                    krajDolazak = dolazakSati + ":";
                else
                    krajDolazak = "0" + dolazakSati + ":";

                if (dolazakMinuti > 9)
                    krajDolazak += dolazakMinuti + "";
                else
                    krajDolazak += "0" + dolazakMinuti;


                LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.slicke, null);
                LinearLayout ikone = (LinearLayout) info.findViewById(R.id.slicice);
                ((TextView) info.findViewById(R.id.vremena)).setText(sati + ":" + minuti + "-" + krajDolazak);
                if (odgovor.linije[i] == null)
                {
                    ImageView imageView = new ImageView(otac);
                    imageView.setImageResource(R.mipmap.ic_walking);
                    ikone.addView(imageView);

                    dodajDeonicu(R.mipmap.ic_walking, otac.getString(R.string.start), otac.getString(R.string.cilj), sati + ":" + minuti,
                            krajDolazak, otac.getString(R.string.pesacenje),
                            (LinearLayout) info.findViewById(R.id.kontenjer));


                } else
                {
                    Cvor polazna = MainActivity.graf.getStanica(odgovor.stanice[2 * i]);
                    Cvor dolazna = MainActivity.graf.getStanica(odgovor.stanice[2 * i + 1]);
                    ImageView imageView = new ImageView(otac);
                    imageView.setImageResource(R.mipmap.ic_walking);
                    ikone.addView(imageView);

                    String dolazak;
                    if (odgovor.vremenaDolaska.get(i).sat > 9)
                        dolazak = odgovor.vremenaDolaska.get(i).sat + ":";
                    else
                        dolazak = "0" + odgovor.vremenaDolaska.get(i).sat + ":";

                    if (odgovor.vremenaDolaska.get(i).minut > 9)
                        dolazak += odgovor.vremenaDolaska.get(i).minut + "";
                    else
                        dolazak += "0" + odgovor.vremenaDolaska.get(i).minut;

                    dodajDeonicu(R.mipmap.ic_walking, otac.getString(R.string.start), polazna.naziv, sati + ":" + minuti,
                            "", otac.getString(R.string.pesacenje),
                            (LinearLayout) info.findViewById(R.id.kontenjer));

                    int resurs = MainActivity.ikonice[odgovor.linije[i]];
                    imageView = new ImageView(otac);
                    imageView.setImageResource(resurs);

                    ikone.addView(imageView);

                    if (odgovor.vremenaDolaska.get(i).crowdInfo == null)
                        dodajDeonicu(resurs, polazna.naziv, dolazna.naziv, dolazak,
                                "", MainActivity.graf.getGl().linije[odgovor.linije[i]].naziv,
                                (LinearLayout) info.findViewById(R.id.kontenjer));
                    else
                        dodajDeonicuCrowd(resurs, polazna.naziv, dolazna.naziv, dolazak,
                                "", MainActivity.graf.getGl().linije[odgovor.linije[i]].naziv,
                                (LinearLayout) info.findViewById(R.id.kontenjer),
                                i);




                    imageView = new ImageView(otac);
                    imageView.setImageResource(R.mipmap.ic_walking);

                    dodajDeonicu(R.mipmap.ic_walking, dolazna.naziv, otac.getString(R.string.cilj), "",
                            krajDolazak, otac.getString(R.string.pesacenje),
                            (LinearLayout) info.findViewById(R.id.kontenjer));

                    ikone.addView(imageView);
                    RadioButton button = (RadioButton) layoutInflater
                            .inflate(R.layout.show_on_map, null).findViewById(R.id.dugme);
                    ((LinearLayout) button.getParent()).removeAllViewsInLayout();
                    if (i == 0)
                        button.setChecked(true);
                    dugmici.add(button);

                    button.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            int size = dugmici.size();
                            for (int i = 0; i < size; i++)
                            {
                                if (dugmici.get(i).isChecked())
                                    dugmici.get(i).setChecked(false);
                                RadioButton dugme = (RadioButton) v;
                                if (dugme == dugmici.get(i))
                                {
                                    dugme.setChecked(true);
                                    pripremiEkoOpt(odgovor, source, destination, i);
                                    otac.getSupportActionBar().setSelectedNavigationItem(1);
                                }
                            }
                        }
                    });

                    ((LinearLayout) info.findViewById(R.id.kontenjer)).addView(button);
                }

                kontenjer.addView(info);

                if (i > 0)
                    ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(kontenjer);
            }

            v = view;
            if (otac.findViewById(R.id.wraper) != null)
            {
                if (v.getParent() != null)
                {
                    ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) otac.findViewById(R.id.wraper)).removeAllViewsInLayout();
                }

                ((RelativeLayout) otac.findViewById(R.id.wraper)).addView(v);
            }
        }
    }
}