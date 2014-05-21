package is.biosyningar.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import is.biosyningar.PhotoActivity;
import is.biosyningar.R;
import is.biosyningar.datacontracts.CinemaMovie;

public class CinemaAdapter extends BaseAdapter
{
    private Context mContext;
    private int layoutResourceId;
    private List<CinemaMovie> mMovies;
    private Picasso mPicassoInstance;

    public CinemaAdapter(Context context, int layoutResourceId, List<CinemaMovie> movies)
    {
        this.mContext = context;
        this.layoutResourceId = layoutResourceId;
        this.mMovies = movies;
        this.mPicassoInstance = new Picasso.Builder(context).memoryCache(new LruCache(10000000)).build();
    }

    static class CinemaHolder
    {
        public CinemaHolder(View view)
        {
            ButterKnife.inject(this, view);
        }

        @InjectView(R.id.movie_title) TextView title;
        @InjectView(R.id.movie_poster) ImageView image;
        @InjectView(R.id.movie_score) TextView imdb;
        @InjectView(R.id.movie_restrictions) TextView restricted;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        final CinemaHolder holder;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new CinemaHolder(row);
            row.setTag(holder);
        }
        else
        {
            holder = (CinemaHolder)row.getTag();
        }

        final CinemaMovie movie = getItem(position);

        holder.title.setText(movie.getTitle());
        holder.imdb.setText(movie.getImdb());
        holder.restricted.setText(movie.getRestricted());

        holder.image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LaunchPhotoActivity(movie.getImageUrl(), movie.getTitle());
            }
        });

        mPicassoInstance
                .load(movie.getImageUrl())
                .resizeDimen(R.dimen.poster_width, R.dimen.poster_height)
                .into(holder.image);

        return row;
    }

    private void LaunchPhotoActivity(String imageUrl, String title)
    {
        Activity activity = (Activity) mContext;
        Intent intent = new Intent(mContext, PhotoActivity.class);
        intent.putExtra(PhotoActivity.EXTRA_IMAGE_URL, imageUrl);
        intent.putExtra(PhotoActivity.EXTRA_TITLE, title);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public int getCount()
    {
        return (mMovies == null) ? 0 : mMovies.size();
    }

    @Override
    public CinemaMovie getItem(int position)
    {
        return mMovies.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return mMovies.indexOf(getItem(position));
    }

    public void setMovies(List<CinemaMovie> movies) { this.mMovies = movies; }
}