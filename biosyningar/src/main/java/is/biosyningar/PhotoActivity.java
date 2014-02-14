package is.biosyningar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoActivity extends Activity
{
    public static final String EXTRA_IMAGE_URL = "is.biosyningar.IMAGEURL";
    public static final String EXTRA_TITLE = "is.biosyningar.TITLE";

    private PhotoViewAttacher mAttacher;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_photo);

        ImageView mImageView = (ImageView) findViewById(R.id.iv_photo);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
        String title = intent.getStringExtra(EXTRA_TITLE);

        setTitle(title);

        Picasso.with(this).load(imageUrl).into(mImageView);

        mAttacher = new PhotoViewAttacher(mImageView);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mAttacher.cleanup();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
