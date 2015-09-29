package rs.mosis.diplomski.bus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.drive.internal.AddEventListenerRequest;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import strukture.Cvor;
import strukture.GradskeLinije;
import strukture.Graf;
import strukture.Linija;

public class Glavna_Aktivnost extends AppCompatActivity implements ActionBar.TabListener {

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
    public static final LatLng Nis = new LatLng(43.319425, 21.899487);
    public static final LatLng jugozapad = new LatLng(43.2659128,21.7123964);
    public static final LatLng severoistok = new LatLng(43.4381218,22.1044385);
    public static final LatLng proba = new LatLng(43.338626, 21.875792);
    public static final LatLng proba_kraj= new LatLng(43.312385, 21.923335);
    public static Handler UIHandler = new Handler(Looper.getMainLooper());
    LocationManager locationManager;
    LocationListener listener;
    public static Geocoder geocoder;

    public static Glavna_Aktivnost otac;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glavna__aktivnost);

        this.setTitle("Bus++");
        geocoder = new Geocoder(this, Locale.US);

        fragmentManager = getSupportFragmentManager();

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        Intent ii = getIntent();
        int a = ii.getIntExtra("jblg", -1);

        Toast.makeText(this, a + "  ", Toast.LENGTH_LONG).show();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());



        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.https://github.com/FilipStamenkovic/Klijent-BusPlusPlus
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        otac = this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        disconnect();
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnect();
    }

    public void postaviComplete(final String [] strings,final AutoCompleteTextView textView)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<?> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_suggest, strings);


                textView.setAdapter(adapter);
                //textView.setDropDownHeight(textView.getHeight());



                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    MyLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_glavna__aktivnost, menu);
        return true;
    }

    private void disconnect()
    {
        if(locationManager != null)
        {
            locationManager.removeUpdates(listener);
            locationManager = null;
            listener = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            //MapaFragment.pocetak = !MapaFragment.pocetak;
            return true;
        } else if (id == R.id.action_ekonomicni)
        {
            final LatLng source = MapaFragment.getMyPosition();
            final LatLng destination = MapaFragment.cilj.getPosition();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    final Response odgovor = Komunikacija_Server.ekonomicniRezim(source,destination);

                    final ArrayList<ArrayList<Cvor>> listaCvorova = new ArrayList<ArrayList<Cvor>>();
                    if(odgovor != null)
                        for(int i = 0; i < odgovor.linije.length; i++)
                            listaCvorova.add(MainActivity.graf.pratiLiniju(odgovor.linije[i],odgovor.stanice[0],odgovor.stanice[1]));
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(odgovor != null)
                            {
                                int index = 0;
                                ArrayList<Integer> linije_id = new ArrayList<Integer>();
                                Cvor pocetna = MainActivity.graf.getStanica(odgovor.stanice[0]);
                                MapaFragment.nacrtajPesacenje(source,new LatLng(pocetna.lat,pocetna.lon));
                                for(int i = 0; i < odgovor.linije.length; i++)
                                    linije_id.add(new Integer(odgovor.linije[i]));

                                for(int i = 0; i < odgovor.linije.length - 1; i++)
                                {
                                    if ((GradskeLinije.istaOsnovna(odgovor.linije[index],odgovor.linije[index + 1]))
                                            && (listaCvorova.get(index).size() == listaCvorova.get(index + 1).size()))
                                    {
                                        listaCvorova.remove(index + 1);
                                        linije_id.remove(index + 1);
                                    }
                                    else
                                        index++;

                                }

                                for(int i = 0; i < listaCvorova.size(); i++)
                                {
                                    MapaFragment.pratiLiniju(linije_id.get(i).intValue(),
                                            otac.getSupportActionBar(),listaCvorova.get(i));
                                }
                                Cvor krajnja = MainActivity.graf.getStanica(odgovor.stanice[1]);
                                MapaFragment.nacrtajPesacenje(new LatLng(krajnja.lat,krajnja.lon),destination);
                            }else
                            {
                                Toast.makeText(otac.getApplicationContext(),"UPS, greska",Toast.LENGTH_LONG).show();
                            }

                            ArrayList<String> vremena = Komunikacija_Server.vremenaPolaska(odgovor, 5);
                            ArrayList<String> korekcije = Komunikacija_Server.vremenaDolaska(odgovor, vremena);
                            // otac.getSupportActionBar().setSelectedNavigationItem(2);
                            Odgovor_Servera.prikazEkonomicnog(odgovor, vremena, korekcije);
                        }
                    });

                }
            }).start();
        }else if(id == R.id.action_set_start)
        {
            if (MapaFragment.start != null)
                MapaFragment.start.remove();
            BitmapDescriptor ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_start_position);
            MapaFragment.start = MapaFragment.googleMap.addMarker(new MarkerOptions()
                    .position(Nis)
                    .title(getString(R.string.pocetak))
                    .icon(ikonica)
                    .draggable(true));
        }else if (id == R.id.action_clean_map)
        {
            MapaFragment.clearStanice();
            if (MapaFragment.start != null)
            {
                MapaFragment.start.remove();
                MapaFragment.start = null;
            }
            if (MapaFragment.cilj != null)
            {
                MapaFragment.cilj.remove();
                MapaFragment.cilj = null;
            }
            if (MapaFragment.ruta != null)
            {
                MapaFragment.ruta.remove();
                MapaFragment.ruta = null;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        if(tab.getPosition() == 2)
            Odgovor_Servera.zahtev.putInt("zahtev",0);

        if (MapaFragment.searchView != null)
            if (MapaFragment.searchView.isFocused())
            {
                InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.aplikacija.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(MapaFragment.searchView.getWindowToken(), 0);
            }

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public static final int brojFragmenta = 3;

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        private void createFragment(int position)
        {
            /*switch (position)
            {
                case 0 :
                    fragmenti[position] = PlaceholderFragment.newInstance(position + 1);
                    break;
                case 1 :
                    fragmenti[position] = MapaFragment.newInstance(position + 1);
                    break;
                case 2:
                    fragmenti[position] = Odgovor_Servera.newInstance(position + 1);
                    break;
            }*/
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            Fragment fragment = null;
            switch (position)
            {
                case 0 :
                    fragment = PlaceholderFragment.newInstance();
                    break;
                case 1 :
                    fragment = MapaFragment.newInstance();
                    break;
                case 2:
                    fragment = Odgovor_Servera.newInstance();
                    break;
            }

            return  fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return brojFragmenta;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
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
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private static LinearLayout linearLayout = null;


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance() {
            PlaceholderFragment fragment = new PlaceholderFragment();
            return fragment;
        }

        public PlaceholderFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            final View rootView;
            rootView = inflater.inflate(R.layout.fragment_red_voznje, container, false);




            Linija[] linije = MainActivity.graf.getGl().linije;
            ArrayList<Linija> listaLinija = new ArrayList<>();

            for(int i = 1; i < linije.length; i++)
                if(linije[i].broj.charAt(linije[i].broj.length() - 1) == '*')
                    continue;
                else
                    listaLinija.add(linije[i]);

            if(linearLayout == null)
            {

                linearLayout = (LinearLayout) rootView.findViewById(R.id.linije_scroll);

                for (int i = 0; i < listaLinija.size(); i += 2) {
                    View red = inflater.inflate(R.layout.red_voznje_layout, container, false);
                    TextView textView = (TextView) red.findViewWithTag("linija");
                    textView.setText("Linija " + listaLinija.get(i).broj);
                    textView = (TextView) red.findViewWithTag("smerA");
                    textView.setText(listaLinija.get(i).naziv);
                    textView = (TextView) red.findViewWithTag("smerB");
                    textView.setText(listaLinija.get(i + 1).naziv);
                    red.findViewWithTag("prikaziA").setId(listaLinija.get(i).id);
                    red.findViewWithTag("prikaziB").setId(listaLinija.get(i + 1).id);
                    red.findViewWithTag("pogledajA").setId(listaLinija.get(i).id + 1000);
                    red.findViewWithTag("pogledajB").setId(listaLinija.get(i + 1).id  + 1000);
                    // container.addView(red);
                    linearLayout.addView(red, i / 2);

                }
            }
            else
            {
                ScrollView skrol = (ScrollView) rootView.findViewById(R.id.skrol);
                //lin = linearLayout;

                //skrol.removeAllViewsInLayout();
                //skrol.removeAllViews();
                skrol.removeViewAt(0);
                //rootView.findViewById(R.id.linije_scroll).remove
                ((ScrollView)linearLayout.getParent()).removeAllViews();
                skrol.addView(linearLayout);
            }


            return rootView;
        }

    }

    public void prikaziNaMapi(View v)
    {
        int id = v.getId();

        ArrayList<Cvor> cvorovi = MainActivity.graf.pratiLiniju(id, -1, -1);

        MapaFragment.pratiLiniju(id, getSupportActionBar(),cvorovi);


        int a = 3;
    }

    public void redVoznje(View v)
    {
        final int id = v.getId() - 1000;



        final LatLng sourceLatLng = MapaFragment.getMyPosition();

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                final Response odgovor = Komunikacija_Server.ObicanRedVoznje(sourceLatLng,id);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ArrayList<String> vremena = Komunikacija_Server.vremenaPolaska(odgovor);
                        ArrayList<String> korekcije = Komunikacija_Server.vremenaDolaska(odgovor, vremena);

                        Odgovor_Servera.popuniTabelu(odgovor, vremena, korekcije);
                        otac.getSupportActionBar().setSelectedNavigationItem(2);
                    }
                });

            }
        }).start();

    }

    public static class MapaFragment extends Fragment
    {
        public static GoogleMap googleMap = null;

        public static ArrayList<Marker> stanice = null;

        public static Polyline ruta = null;

        public static ArrayList<Circle> pesacenja = null;

        public static Marker cilj = null;

        public static Marker start = null;

        public static AutoCompleteTextView searchView;


        public static MapaFragment newInstance() {
            MapaFragment fragment = new MapaFragment();
            return fragment;
        }

        public MapaFragment() {

        }


        public static LatLng getMyPosition()
        {
            LatLng sourceLatLng2 = null;
            if(start != null)
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


        public static void nacrtajRutu(final ArrayList<Cvor> cvorovi)
        {
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    DirectionsHelper directionsHelper;
                    final List<LatLng> lista;

                    directionsHelper  = new DirectionsHelper(cvorovi);
                    lista= directionsHelper.getTacke("driving");

                    UIHandler.post(new Runnable() {
                        @Override
                        public void run()
                        {
                            if (lista != null)
                            {
                                if (ruta != null)
                                    ruta.remove();

                                ruta = googleMap.addPolyline(new PolylineOptions()
                                                .addAll(lista)
                                                .width(12)
                                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                                .geodesic(true)
                                );
                            }
                        }
                    });
                }
            }).start();



        }

        public static void nacrtajPesacenje(final LatLng source, final LatLng dest)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    DirectionsHelper directionsHelper;
                    final List<LatLng> lista;

                    directionsHelper  = new DirectionsHelper(source,dest);
                    lista = directionsHelper.getTacke("walking");
                    UIHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (lista != null)
                            {
                                if (pesacenja != null)
                                {
                                       /* int size = pesacenja.size();
                                        for(int i = 0; i < size; i++)
                                        {
                                            pesacenja.get(0).remove();
                                            pesacenja.remove(0);
                                        }*/
                                }

                                pesacenja = new ArrayList<Circle>();
                                for (int i = 0; i < lista.size(); i++)
                                {
                                    Circle circle = googleMap.addCircle(new CircleOptions()
                                            .center(lista.get(i))
                                            .radius(4)
                                            .strokeColor(Color.BLUE)
                                            .fillColor(Color.GREEN));
                                    pesacenja.add(circle);
                                }
                            }
                        }
                    });
                }
            }).start();

        }

        public static void clearStanice()
        {
            if(stanice != null)
            {
                int size = stanice.size();
                for(int i = 0; i < size; i++) {
                    stanice.get(0).remove();
                    stanice.remove(0);
                }
                stanice = null;
            }
        }

        public static void clearPesacenje()
        {
            if (pesacenja != null)
            {
                int size = pesacenja.size();
                for (int i = 0; i < size; i++)
                {
                    pesacenja.get(0).remove();
                    pesacenja.remove(0);
                }
                pesacenja = null;
            }
        }

        public static void pratiLiniju(int id,ActionBar actionBar, ArrayList<Cvor> cvorovi)
        {
            Linija linija = MainActivity.graf.getGl().linije[id];

           // ArrayList<Cvor> cvorovi = MainActivity.graf.pratiLiniju(id,-1,-1);

           // clearStanice();

            if(stanice == null)
                stanice = new ArrayList<>();
            int brojac = 0;
            for(int i = 0; i < cvorovi.size(); i++)
            {
                brojac++;
                BitmapDescriptor ikonica;
                if(i == 0)
                    ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pocetak);
                else if (i == cvorovi.size() - 1)
                    ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_kraj);
                else
                     ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_stajaliste);
                Cvor cvor = cvorovi.get(i);
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(cvor.lat, cvor.lon))
                        .title(cvor.naziv)
                        .icon(ikonica)
                        .snippet(linija.naziv)
                        .draggable(false));

                stanice.add(marker);
            }

            Linija nextLine = MainActivity.graf.getGl().linije[linija.id];
            nacrtajRutu(cvorovi);
/*            while(nextLine.broj.equals(linija.broj + "*"))
            {
                linija = nextLine;

                ArrayList<Cvor> novi = MainActivity.graf.pratiLiniju(linija.id);
                int size = novi.size();

                for(int i = 0; i < novi.size(); i++)
                    if(cvorovi.contains(novi.get(i)))
                    {
                        novi.remove(i);
                        i--;
                    }else
                        cvorovi.add(novi.get(i));

                for(int i = 0; i < novi.size(); i++)
                {
                    brojac++;
                    Cvor cvor = cvorovi.get(i);
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(cvor.lat,cvor.lon))
                            .title(cvor.naziv)
                            .snippet(linija.naziv)
                            .draggable(false));

                    stanice.add(marker);
                }
            }*/

           // getContext();
            actionBar.setSelectedNavigationItem(1);

        }

        private void addMarker(double lat,double lon, String s)
        {

            if(cilj != null)
            {
                cilj.remove();
            }

            BitmapDescriptor ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_odrediste);
            cilj = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title("Odrediste")
                    .snippet(s)
                    .icon(ikonica)
                    .draggable(true));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            View rootView;

            rootView = inflater.inflate(R.layout.fragment_mapa, container, false);
            setUpMap(rootView);
            setUpSearch(rootView);
            return rootView;
        }

        private void setUpSearch(View root)
        {
            final SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
            //SearchView searchView = (SearchView) root.findViewById(R.id.search_adresa);
            searchView = (AutoCompleteTextView) ((ViewGroup) root).getChildAt(0);
            //SearchView.SearchAutoComplete


            searchView.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {

                    if (searchView.getAdapter() != null)
                        if (searchView.getAdapter().getCount() > 0)
                        {
                            // searchView.setText((String) searchView.getAdapter().getItem(0));
                            // searchView.setAdapter(null);


                            String s = searchView.getText().toString();
                            double lat, lon;

                            try
                            {
                                List<Address> adrese = geocoder.getFromLocationName(s, 1, jugozapad.latitude, jugozapad.longitude, severoistok.latitude, severoistok.longitude);
                                s = adrese.get(0).getLocality();
                                lat = adrese.get(0).getLatitude();
                                lon = adrese.get(0).getLongitude();
                                addMarker(lat, lon, s);
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
                                for (int i = 0; i < addresses.size(); i++)
                                {
                                    boolean b = false;
                                    String s = addresses.get(i).getFeatureName() + " - " + addresses.get(i).getLocality();
                                    s = s.replace(" ", "");
                                    for (int j = 0; j < stringovi.size(); j++)
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

                                // ArrayAdapter<?> adapter = new ArrayAdapter<String>(getActivity(),R.layout.simple_suggest,strings);


                                ((Glavna_Aktivnost) getActivity()).postaviComplete(strings, searchView);
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }


                            //searchView.setAdapter(adapter);


                            //adapter.notifyDataSetChanged();
                        }
                    }).start();


                    // Toast.makeText(getContext(),lokacija.getLatitude()+ "     " + lokacija.getLongitude(),Toast.LENGTH_LONG).show();

                }

                @Override
                public void afterTextChanged(Editable s)
                {

                }
            });


            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Geocoder geocoder = new Geocoder(getContext());
                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocationName(query,5);
                        Address lokacija = addresses.get(0);
                        Toast.makeText(getContext(),lokacija.getLatitude()+ "     " + lokacija.getLongitude(),Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    return true;
                }
            };
        }
        private void setUpMap(View root)
        {
            if(googleMap == null) {

                googleMap = ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapa)).getMap();
                if (googleMap != null)
                    googleMap.setMyLocationEnabled(true);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation, 15));


                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
                {
                    @Override
                    public void onMapLongClick(LatLng latLng)
                    {
                        if (searchView.isFocused())
                        {
                            InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.aplikacija.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                        }
                        if (cilj != null)
                            cilj.remove();
                        BitmapDescriptor ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_odrediste);
                        cilj = googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(getString(R.string.finish_marker))
                                .icon(ikonica)
                                .draggable(true));
                        if (ruta != null)
                        {
                            ruta.remove();
                            ruta = null;
                        }
                        // clearStanice();
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

            }

        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();

            if(googleMap != null)
            {
                if(cilj != null)
                {
                    cilj.remove();
                    cilj = null;
                }
                if(start != null)
                {
                    start.remove();
                    start = null;
                }
                googleMap.clear();
                googleMap = null;

            }
        }
    }

    public static class Odgovor_Servera extends Fragment
    {
        private static View v = null;

       // private static LayoutInflater layoutInflater;

        public static Bundle zahtev;

        private static int tipZahteva = -1;


        public static Odgovor_Servera newInstance()
        {
            Odgovor_Servera fragment = new Odgovor_Servera();
            return fragment;
        }

        public Odgovor_Servera()
        {

            if(zahtev == null)
                zahtev = new Bundle();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView;
            int noviZahtev = zahtev.getInt("zahtev");
/*            if ((noviZahtev == 0) && (tipZahteva != -1))
                return v;
            else
                tipZahteva = 0;*/
            rootView = inflater.inflate(R.layout.fragment_glavna__aktivnost, container, false);
            //v = rootView;
            if(v != null)
            {
                if(v.getParent() != null)
                    ((RelativeLayout)v.getParent()).removeAllViewsInLayout();
                ((RelativeLayout) rootView).addView(v);
            }
           // layoutInflater = inflater;
            return rootView;
        }

        public static void popuniTabelu(Response odgovor, ArrayList<String> vremenaPolaska, ArrayList<String> vremenaDolaska)
        {
            if(zahtev == null)
                zahtev = new Bundle();
            zahtev.putInt("zahtev", 3);
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

            v = layoutInflater.inflate(R.layout.fragment_odgovor_servera,null);

            v.findViewById(R.id.zaglavlje).setVisibility(View.VISIBLE);
            v.findViewById(R.id.info_o_liniji).setVisibility(View.VISIBLE);
            LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.informacije_linije, null);
            ((LinearLayout) v.findViewById(R.id.info_o_liniji)).addView(info);
            int pocetak, kraj;
            pocetak = 0;
            kraj = 0;
            for (int i = 0; i < vremenaDolaska.size(); i++)
            {
                LinearLayout vreme_layout = (LinearLayout) layoutInflater.inflate(R.layout.fragment_vremena, null);
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
                ((TextView) vreme_layout.findViewWithTag("vreme_dolaska")).setText(vremenaDolaska.get(i));
                ((TextView) vreme_layout.findViewWithTag("vreme_polaska")).setText(vremenaPolaska.get(i));

                ((LinearLayout) kontenjer.findViewById(R.id.vremena)).addView(vreme_layout);


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
                ((TextView) stanica.findViewById(R.id.linija_id)).setVisibility(View.GONE);
                ((TextView) stanica.findViewById(R.id.smer_id)).setVisibility(View.GONE);
                ((TextView) stanica.findViewById(R.id.naziv_id)).setText(R.string.stanica);
                ((LinearLayout) kontenjer.findViewById(R.id.blok_za_stanicu)).addView(stanica);

                String naziv = cvor.naziv;
                stanica = (LinearLayout) layoutInflater.inflate(R.layout.informacije_linije, null);
                ((TextView) stanica.findViewById(R.id.linija_id)).setVisibility(View.GONE);
                ((TextView) stanica.findViewById(R.id.smer_id)).setVisibility(View.GONE);
                ((TextView) stanica.findViewById(R.id.naziv_id)).setText(Html.fromHtml("<u>" + naziv + "</u>"));
                ((LinearLayout) kontenjer.findViewById(R.id.blok_za_stanicu)).addView(stanica);

                stanica.setId(cvor.id);

                MapaFragment.clearStanice();

                ArrayList<Marker> markers = MapaFragment.stanice;
                markers = new ArrayList<>();

                BitmapDescriptor ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pocetak);
                Marker marker = MapaFragment.googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(cvor.lat, cvor.lon))
                        .title(cvor.naziv)
                        .icon(ikonica)
                        .snippet(MainActivity.graf.getGl().linije[odgovor.linije[j]].naziv)
                        .draggable(false));

                markers.add(marker);

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


        }

        public static void prikazEkonomicnog(Response odgovor, ArrayList<String> vremenaPolaska, ArrayList<String> vremenaDolaska)
        {
            zahtev.putInt("zahtev",4);
            LayoutInflater layoutInflater = (LayoutInflater) otac.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ViewGroup container = (ViewGroup) otac.findViewById(R.id.pager).getRootView();
            Cvor polazna = MainActivity.graf.getStanica(odgovor.stanice[0]);
            Cvor dolazna = MainActivity.graf.getStanica(odgovor.stanice[1]);
            View view = layoutInflater.inflate(R.layout.fragment_ekonomicni, container, false);
            ((TextView)view.findViewById(R.id.polazna_stanica)).setText(Html.fromHtml("<u>" + polazna.naziv + "</u>"));
            ((TextView)view.findViewById(R.id.zavrsna_stanica)).setText(Html.fromHtml("<u>" + dolazna.naziv + "</u>"));

            view.findViewById(R.id.polazna_stanica).setId(polazna.id);
            view.findViewById(R.id.zavrsna_stanica).setId(dolazna.id);

            //MapaFragment.clearStanice();
            ArrayList<Marker> markers = new ArrayList<>();
            /*markers = new ArrayList<>();

            BitmapDescriptor ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_pocetak);
            Marker marker = MapaFragment.googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(polazna.lat, polazna.lon))
                    .title(polazna.naziv)
                    .icon(ikonica)
                            //.snippet(MainActivity.graf.getGl().linije[odgovor.linije[j]].naziv)
                    .draggable(false));

            markers.add(marker);
            ikonica = BitmapDescriptorFactory.fromResource(R.mipmap.ic_kraj);
            marker = MapaFragment.googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(dolazna.lat, dolazna.lon))
                    .title(dolazna.naziv)
                    .icon(ikonica)
                            //.snippet(MainActivity.graf.getGl().linije[odgovor.linije[j]].naziv)
                    .draggable(false));

            markers.add(marker);

            MapaFragment.stanice = markers;*/
            int pocetak = 0;
            int kraj = 0;
            for(int i = 0; i < vremenaDolaska.size(); i++)
            {
                LinearLayout vreme_layout = (LinearLayout) layoutInflater.inflate(R.layout.fragment_vremena, null);
                LinearLayout kontenjer;
                if (i == 0)
                    kontenjer = (LinearLayout) view.findViewById(R.id.kontenjer);
                else
                {
                    View kopija = layoutInflater.inflate(R.layout.fragment_odgovor_servera, null);
                    kontenjer = (LinearLayout) kopija.findViewById(R.id.kontenjer);
                    ((ScrollView) kontenjer.getParent()).removeAllViewsInLayout();
                }

                    ((TextView) vreme_layout.findViewWithTag("vreme_dolaska")).setText(vremenaDolaska.get(i));
                    ((TextView) vreme_layout.findViewWithTag("vreme_polaska")).setText(vremenaPolaska.get(i));
                    ((LinearLayout) kontenjer.findViewById(R.id.vremena)).addView(vreme_layout);


                    int j = pocetak;
                    for (; j < odgovor.linije.length - 1; j++)
                        if (GradskeLinije.istaOsnovna(odgovor.linije[j], odgovor.linije[j + 1]))
                            kraj++;
                        else
                            break;

                    j = pocetak;
                    pocetak = kraj + 1;

                    for (; j <= kraj; j++)
                    {
                        if (odgovor.linije[j] != -1)
                        {
                            LinearLayout info = (LinearLayout) layoutInflater.inflate(R.layout.informacije_linije, null);
                            ((TextView) info.findViewById(R.id.linija_id)).setText(MainActivity.graf.getGl().linije[odgovor.linije[j]].broj);
                            ((TextView) info.findViewById(R.id.smer_id)).setText(MainActivity.graf.getGl().linije[odgovor.linije[j]].smer);
                            ((TextView) info.findViewById(R.id.naziv_id)).setText(MainActivity.graf.getGl().linije[odgovor.linije[j]].naziv);
                            ((LinearLayout) kontenjer.findViewById(R.id.info_o_liniji)).addView(info);
                        }
                    }

                    if (i > 0)
                        ((LinearLayout) view.findViewById(R.id.kontenjer)).addView(kontenjer);

                }
            v = view;
            if(otac.findViewById(R.id.wraper) != null)
            {
                if(v.getParent() != null)
                {
                    ((RelativeLayout) v.getParent()).removeAllViewsInLayout();
                    ((RelativeLayout) otac.findViewById(R.id.wraper)).removeAllViewsInLayout();
                }

                ((RelativeLayout) otac.findViewById(R.id.wraper)).addView(v);
            }
            tipZahteva = 0;
        }
    }

}
