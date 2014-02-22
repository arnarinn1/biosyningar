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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.slidinglayer.SlidingLayer;

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
    public static final String ApisUrl = "http://apis.is";

    @InjectView(R.id.slidingLayer) SlidingLayer mSlidingLayer;
    @InjectView(R.id.cinemaSchedules) ListView mListView;
    @InjectView(R.id.progressIndicator) ProgressBar mProgressBar;
    @InjectView(R.id.error) TextView mErrorText;

    private CinemaAdapter mAdapter;
    private Apis service;
    private Context getContext() { return this; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

        mListView.setOnItemClickListener(this);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ApisUrl)
                .build();

        service = restAdapter.create(Apis.class);

        registerReceiver(new ConnectionChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    Callback<CinemaResults> callback = new Callback<CinemaResults>()
    {
        @Override
        public void success(CinemaResults cinemaResults, Response response)
        {
            mErrorText.setVisibility(View.GONE);
            EnforceViewBehaviorOnNetworkCall(View.GONE);

            mAdapter = new CinemaAdapter(getContext(), R.layout.listview_cinema, cinemaResults.getMovies());
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
                service.getMovies("cinema", callback);
                Toast.makeText(context, "Connected to network", Toast.LENGTH_SHORT).show();

            }
            else
            {
                EnforceViewBehaviorOnNetworkCall(View.GONE);

                if (mAdapter == null)
                {
                    mErrorText.setVisibility(View.VISIBLE);
                    mErrorText.setText(R.string.not_connected_network);
                }

                Toast.makeText( context, "Not connected to network", Toast.LENGTH_SHORT ).show();
            }
        }
    }
}
