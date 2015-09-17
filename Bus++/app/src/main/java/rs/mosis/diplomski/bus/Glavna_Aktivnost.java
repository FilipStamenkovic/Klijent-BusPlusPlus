package rs.mosis.diplomski.bus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    public static final LatLng jugozapad = new LatLng(43.2659128,21.7123964);
    public static final LatLng severoistok = new LatLng(43.4381218,22.1044385);
    LocationManager locationManager;
    LocationListener listener;

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

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


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment fragment;
            if(position == 1)
                fragment = MapaFragment.newInstance(position + 1);
            else
                fragment = PlaceholderFragment.newInstance(position + 1);


            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {

        }

        @SuppressLint("NewApi")
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




        int a =3;
    }

    public static class MapaFragment extends Fragment
    {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static GoogleMap googleMap = null;

        public static Marker cilj = null;

        public static Marker start = null;


        public static MapaFragment newInstance(int sectionNumber) {
            MapaFragment fragment = new MapaFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public MapaFragment() {

        }

        private void addMarker(double lat,double lon, String s)
        {

            if(cilj != null)
            {
                cilj.remove();
            }
            cilj = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title("Odrediste")
                    .snippet(s)
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
            final AutoCompleteTextView searchView = (AutoCompleteTextView) ((ViewGroup) root).getChildAt(0);
            //SearchView.SearchAutoComplete

            searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                    if (searchView.getAdapter() != null)
                        if (searchView.getAdapter().getCount() > 0) {
                            // searchView.setText((String) searchView.getAdapter().getItem(0));
                            // searchView.setAdapter(null);

                            Geocoder geocoder = new Geocoder(getContext(), Locale.US);
                            String s = searchView.getText().toString();
                            double lat, lon;

                            try {
                                List<Address> adrese = geocoder.getFromLocationName(s, 1, jugozapad.latitude, jugozapad.longitude, severoistok.latitude, severoistok.longitude);
                                s = adrese.get(0).getLocality();
                                lat = adrese.get(0).getLatitude();
                                lon = adrese.get(0).getLongitude();
                                addMarker(lat, lon, s);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    return false;
                }

                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    return false;

                }
            });

            searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    final String polje = s.toString();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Geocoder geocoder = new Geocoder(getContext(), Locale.US);
                            List<Address> addresses;
                            try {
                                addresses = geocoder.getFromLocationName(polje, 10, jugozapad.latitude, jugozapad.longitude, severoistok.latitude, severoistok.longitude);


                                ArrayList<String> stringovi = new ArrayList<String>();
                                for (int i = 0; i < addresses.size(); i++) {
                                    boolean b = false;
                                    String s = addresses.get(i).getFeatureName() + " - " + addresses.get(i).getLocality();
                                    s = s.replace(" ", "");
                                    for (int j = 0; j < stringovi.size(); j++) {
                                        String s2 = stringovi.get(j);
                                        s2 = s2.replace(" ", "");
                                        if (s2.toLowerCase().equals(s.toLowerCase())) {
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
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                            //searchView.setAdapter(adapter);


                            //adapter.notifyDataSetChanged();
                        }
                    }).start();


                    // Toast.makeText(getContext(),lokacija.getLatitude()+ "     " + lokacija.getLongitude(),Toast.LENGTH_LONG).show();

                }

                @Override
                public void afterTextChanged(Editable s) {

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

                View view = root.findViewById(R.id.mapa);
                googleMap = ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mapa)).getMap();
                if (googleMap != null)
                    googleMap.setMyLocationEnabled(true);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation,15));
            }



               // searchView.setOnQueryTextListener(queryTextListener);
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

}
