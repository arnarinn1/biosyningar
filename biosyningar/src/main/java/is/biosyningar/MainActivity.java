package is.biosyningar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.slidinglayer.SlidingLayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import is.biosyningar.adapters.CinemaAdapter;
import is.biosyningar.adapters.ShowTimesAdapter;
import is.biosyningar.datacontracts.CinemaMovie;
import is.biosyningar.datacontracts.CinemaResults;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener
{
    @InjectView(R.id.slidingLayer) SlidingLayer mSlidingLayer;
    @InjectView(R.id.cinemaSchedules) ListView mListView;
    @InjectView(R.id.progressIndicator) ProgressBar mProgressBar;
    @InjectView(R.id.error) TextView mErrorText;

    private CinemaAdapter mAdapter;
    private ApisService mService;
    private Map<String, Integer> mCinemaMappings;

    private static String mCinemaCacheFilter = "filterCinemas";
    private static String mCinemaDateCacheFilter = "filterDateCinemas";

    private Context getContext() { return this; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
    }

    private void Initialize()
    {
        InitializeCinemaMappings();

        Prefs.with(this).Clear();

        ButterKnife.inject(this);
        mListView.setOnItemClickListener(this);

        RestAdapter restAdapter = RetrofitUtil.RestAdapterInstance();
        mService = restAdapter.create(ApisService.class);

        registerReceiver(new ConnectionChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void InitializeCinemaMappings()
    {
        mCinemaMappings = new HashMap<String, Integer>();
        mCinemaMappings.put("sambíóin akureyri", R.id.menu_akureyri);
        mCinemaMappings.put("borgarbíó", R.id.menu_borgarbio);
        mCinemaMappings.put("sambíóin keflavík", R.id.menu_keflavik);
        mCinemaMappings.put("bíó paradís", R.id.menu_paradis);
        mCinemaMappings.put("háskólabíó", R.id.menu_haskolabio);
        mCinemaMappings.put("smárabíó", R.id.menu_smarabio);
        mCinemaMappings.put("laugarásbíó", R.id.menu_laugarasbio);
        mCinemaMappings.put("sambíóin kringlunni", R.id.menu_kringlan);
        mCinemaMappings.put("sambíóin álfabakka", R.id.menu_alfabakki);
        mCinemaMappings.put("sambíóin egilshöll", R.id.menu_egilsholl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cinema, menu);

        SetCinemaDateMenuCheckStates(menu);
        SetCinemaMenuCheckStates(menu);

        return true;
    }

    private void SetCinemaMenuCheckStates(Menu menu)
    {
        Set<String> cachedCinemas = Prefs.with(this).GetStringSet(mCinemaCacheFilter, new HashSet<String>());

        for (String cinema : cachedCinemas)
        {
            if (mCinemaMappings.containsKey(cinema))
            {
                menu.findItem(mCinemaMappings.get(cinema)).setChecked(true);
            }
        }
    }

    private void SetCinemaDateMenuCheckStates(Menu menu)
    {
        int state = Prefs.with(this).GetInt(mCinemaDateCacheFilter, 0);
        if (state != 0)
            menu.findItem(state).setChecked(true);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        int state = Prefs.with(this).GetInt(mCinemaDateCacheFilter, 0);

        MenuItem filter = menu.findItem(R.id.action_filter_by_time);
        MenuItem cinemaFilter = menu.findItem(R.id.action_cinemas);

        if (state == 0)
           filter.setIcon(R.drawable.ic_action_filter);
        else
            filter.setIcon(R.drawable.ic_action_filter_selected);

        Set<String> cinemaSet = Prefs.with(this).GetStringSet(mCinemaCacheFilter, new HashSet<String>());
        if (cinemaSet.isEmpty())
            cinemaFilter.setIcon(R.drawable.ic_action_filter);
        else
            cinemaFilter.setIcon(R.drawable.ic_action_filter_selected);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_filter_16_before:
                AddDateToCache(itemId, "16:00", false);
                break;

            case R.id.menu_filter_18_before:
                AddDateToCache(itemId, "18:00", false);
                break;

            case R.id.menu_filter_20_before:
                AddDateToCache(itemId, "20:00", false);
                break;

            case R.id.menu_filter_20_after:
                AddDateToCache(itemId, "20:00", true);
                break;

            case R.id.menu_filter_22_after:
                AddDateToCache(itemId, "22:00", true);
                break;

            case R.id.menu_no_filter:
                AddDateToCache(itemId, "23:30", false);
                break;

            case R.id.menu_akureyri:
            case R.id.menu_borgarbio:
            case R.id.menu_keflavik:
            case R.id.menu_paradis:
            case R.id.menu_smarabio:
            case R.id.menu_haskolabio:
            case R.id.menu_laugarasbio:
            case R.id.menu_alfabakki:
            case R.id.menu_egilsholl:
            case R.id.menu_kringlan:
                AddCinemaToCache(item);
                UpdateAdapterData("23:30", false);
                break;

            case R.id.menu_showAllCinemas:
                Prefs.with(this).Remove(mCinemaCacheFilter);
                UpdateAdapterData("23:30", false);
        }

        if (itemId == R.id.menu_no_filter)
        {
            Prefs.with(this).Remove(mCinemaDateCacheFilter);
            supportInvalidateOptionsMenu();
            return true;
        }

        supportInvalidateOptionsMenu();

        return true;
    }

    Callback<CinemaResults> callback = new Callback<CinemaResults>()
    {
        @Override
        public void success(CinemaResults cinemaResults, Response response)
        {
            Prefs.with(getContext()).Clear();

            supportInvalidateOptionsMenu();

            mErrorText.setVisibility(View.GONE);
            EnforceViewBehaviorOnNetworkCall(View.GONE);

            mAdapter = new CinemaAdapter(getContext(), R.layout.listview_cinema, cinemaResults.getMovies());

            String responseBody = StreamUtil.GetJsonResponseString(response.getBody());
            Prefs.with(getContext()).Save(GsonUtil.EXTRA_MOVIECACHE, responseBody);

            mListView.setAdapter(mAdapter);
        }

        @Override
        public void failure(RetrofitError retrofitError)
        {
            EnforceViewBehaviorOnNetworkCall(View.GONE);
            mErrorText.setVisibility(View.VISIBLE);
            mErrorText.setText(R.string.no_schedules);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        CinemaMovie movie = mAdapter.getItem(position);

        if (!mSlidingLayer.isOpened())
        {
            mSlidingLayer.openLayer(true);

            ExpandableListView expandMovie = (ExpandableListView) findViewById(R.id.expand_showtimes);
            expandMovie.setAdapter(new ShowTimesAdapter(this, movie.getShowtimes()));
        }
    }

    private void EnforceViewBehaviorOnNetworkCall(int progressBarVisible)
    {
        mProgressBar.setVisibility(progressBarVisible);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
                if (mSlidingLayer.isOpened())
                {
                    mSlidingLayer.closeLayer(true);
                    return true;
                }
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    private void AddDateToCache(int itemId, String dateTime, boolean afterParseDate)
    {
        Prefs.with(this).Save(mCinemaDateCacheFilter, itemId);
        UpdateAdapterData(dateTime, afterParseDate);
    }

    private void AddCinemaToCache(MenuItem item)
    {
        String value = item.getTitle().toString().toLowerCase();
        Set<String> stringSet = Prefs.with(this).GetStringSet(mCinemaCacheFilter, new HashSet<String>());

        if (stringSet.contains(value))
            stringSet.remove(value);
        else
            stringSet.add(value);

        Prefs.with(this).Save(mCinemaCacheFilter, stringSet);
    }

    /**
     * Updates the movies in the adapter
     *
     * @param filterDate Filter date to compare to decide which movies will be discarded
     * @param afterParseDate Filter Dates based on after||before method on Date.class
     */
    private void UpdateAdapterData(String filterDate, boolean afterParseDate)
    {
        List<CinemaMovie> movies = GsonUtil.GetAllMovies(this);

        try
        {
            CinemaParser util = new CinemaParser(filterDate, afterParseDate, Prefs.with(this).GetStringSet(mCinemaCacheFilter, new HashSet<String>()));
            List<CinemaMovie> filteredMovies = util.FilterMoviesByTime(movies);
            mAdapter.setMovies(filteredMovies);
            mAdapter.notifyDataSetChanged();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private class ConnectionChangeReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo wifiStatus   = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobileStatus = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            boolean wifiNetworkStatus = (wifiStatus != null && wifiStatus.isConnected());
            boolean mobileNetworkStatus = (mobileStatus != null && mobileStatus.isConnected());

            if(wifiNetworkStatus || mobileNetworkStatus)
            {
                if (mAdapter != null) return;

                EnforceViewBehaviorOnNetworkCall(View.VISIBLE);
                mService.getMovies("cinema", callback);
            }
            else
            {
                EnforceViewBehaviorOnNetworkCall(View.GONE);

                if (mAdapter == null)
                {
                    mErrorText.setVisibility(View.VISIBLE);
                    mErrorText.setText(R.string.not_connected_network);
                }
            }
        }
    }
}
