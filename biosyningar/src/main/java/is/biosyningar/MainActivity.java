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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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
import retrofit.mime.TypedInput;

public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener
{
    @InjectView(R.id.slidingLayer) SlidingLayer mSlidingLayer;
    @InjectView(R.id.cinemaSchedules) ListView mListView;
    @InjectView(R.id.progressIndicator) ProgressBar mProgressBar;
    @InjectView(R.id.error) TextView mErrorText;

    private CinemaAdapter mAdapter;
    private ApisService mService;

    private Context getContext() { return this; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cinema, menu);

        int state = getPreferences(MODE_PRIVATE).getInt("selector", 0);

        if (state != 0)
            menu.findItem(state).setChecked(true);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        int state = getPreferences(MODE_PRIVATE).getInt("selector", 0);

        MenuItem filter = menu.findItem(R.id.action_new_post);

        if (state == 0)
           filter.setIcon(R.drawable.ic_action_filter);
        else
            filter.setIcon(R.drawable.ic_action_filter_selected);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.menu_filter_16_before:
                UpdateAdapterData("16:00", false, false);
                break;

            case R.id.menu_filter_18_before:
                UpdateAdapterData("18:00", false, false);
                break;

            case R.id.menu_filter_20_before:
                UpdateAdapterData("20:00", false, false);
                break;

            case R.id.menu_filter_20_after:
                UpdateAdapterData("20:00", false, true);
                break;

            case R.id.menu_filter_22_after:
                UpdateAdapterData("22:00", false, true);
                break;

            case R.id.menu_no_filter:
                UpdateAdapterData("no_filter", true, true);
                break;
        }

        if (itemId == R.id.menu_no_filter)
        {
            getPreferences(MODE_PRIVATE).edit().remove("selector").commit();
            supportInvalidateOptionsMenu();
            return true;
        }

        if (itemId != R.id.action_new_post)
            getPreferences(MODE_PRIVATE).edit().putInt("selector", itemId).commit();

        supportInvalidateOptionsMenu();

        return true;
    }

    /**
     * Updates the movies in the adapter
     *
     * @param filterDate Filter date to compare to decide which movies will be discarded
     * @param getAllMovies Boolean flag to decide if we want to show all movies
     * @param afterParseDate Filter Dates based on after||before method on Date.class
     */
    private void UpdateAdapterData(String filterDate, boolean getAllMovies, boolean afterParseDate)
    {
        List<CinemaMovie> movies = GsonUtil.GetAllMovies(this);

        try
        {
            if (getAllMovies)
            {
                mAdapter.setMovies(movies);
                mAdapter.notifyDataSetChanged();
            }
            else
            {
                CinemaParser util = new CinemaParser(filterDate, afterParseDate);
                List<CinemaMovie> filteredMovies = util.FilterMoviesByTime(movies);
                mAdapter.setMovies(filteredMovies);
                mAdapter.notifyDataSetChanged();
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void Initialize()
    {
        getPreferences(MODE_PRIVATE).edit().clear().commit();

        ButterKnife.inject(this);
        mListView.setOnItemClickListener(this);

        RestAdapter restAdapter = RetrofitUtil.RestAdapterInstance();
        mService = restAdapter.create(ApisService.class);

        registerReceiver(new ConnectionChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    Callback<CinemaResults> callback = new Callback<CinemaResults>()
    {
        @Override
        public void success(CinemaResults cinemaResults, Response response)
        {
            getPreferences(MODE_PRIVATE).edit().clear().commit();

            supportInvalidateOptionsMenu();

            mErrorText.setVisibility(View.GONE);
            EnforceViewBehaviorOnNetworkCall(View.GONE);

            mAdapter = new CinemaAdapter(getContext(), R.layout.listview_cinema, cinemaResults.getMovies());

            String responseBody = GetJsonResponseString(response.getBody());
            getPreferences(MODE_PRIVATE).edit().putString(GsonUtil.EXTRA_MOVIECACHE, responseBody).commit();

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

    private String ConvertStreamToString(InputStream stream) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line;
        while ((line = reader.readLine()) != null)
        {
            sb.append(line);
        }

        return sb.toString();
    }

    private String GetJsonResponseString(TypedInput body)
    {
        String response = null;
        try
        {
            InputStream fu = body.in();
            response = ConvertStreamToString(fu);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return response;
    }

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
