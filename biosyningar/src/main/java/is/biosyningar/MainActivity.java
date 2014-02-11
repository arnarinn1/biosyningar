package is.biosyningar;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.slidinglayer.SlidingLayer;
import com.squareup.picasso.Picasso;

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

    private SlidingLayer mSlidingLayer;
    private ListView mListView;
    private CinemaAdapter mAdapter;
    private ProgressBar mProgressBar;
    private Button mTryAgainButton;
    private RelativeLayout mHiddenLayout;

    private Apis apisClient;

    private Context getContext() { return this; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AttachViews();
        AttachEventListeners();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ApisUrl)
                .build();

        apisClient = restAdapter.create(Apis.class);
        apisClient.getMovies("cinema", callback);
    }

    Callback<CinemaResults> callback = new Callback<CinemaResults>()
    {
        @Override
        public void success(CinemaResults cinemaResults, Response response)
        {
            mHiddenLayout.setVisibility(View.GONE);
            EnforceViewBehaviorOnNetworkCall(View.GONE, true);

            mAdapter = new CinemaAdapter(getContext(), R.layout.listview_cinema, cinemaResults.getMovies());
            mListView.setAdapter(mAdapter);
        }

        @Override
        public void failure(RetrofitError retrofitError)
        {
            EnforceViewBehaviorOnNetworkCall(View.GONE, true);
            mHiddenLayout.setVisibility(View.VISIBLE);
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

    private void EnforceViewBehaviorOnNetworkCall(int progressBarVisible, boolean buttonEnabled)
    {
        mProgressBar.setVisibility(progressBarVisible);
        mTryAgainButton.setEnabled(buttonEnabled);
    }

    private void AttachViews()
    {
        mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
        mListView = (ListView) findViewById(R.id.cinemaSchedules);
        mProgressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        mHiddenLayout = (RelativeLayout) findViewById(R.id.error_layout);
        mTryAgainButton = (Button) findViewById(R.id.try_again_button);
    }

    private void AttachEventListeners()
    {
        mListView.setOnItemClickListener(this);

        mTryAgainButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                apisClient.getMovies("cinema", callback);
                EnforceViewBehaviorOnNetworkCall(View.VISIBLE, false);
            }
        });
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
}
