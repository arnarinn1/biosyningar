package is.biosyningar;

import android.app.Activity;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import is.biosyningar.datacontracts.CinemaMovie;
import is.biosyningar.datacontracts.CinemaResults;

/**
 * Created by Arnar on 18.5.2014.
 */
public class GsonUtil
{
    public static String EXTRA_MOVIECACHE = "is.biosyningar.MOVIECACHE";

    public static List<CinemaMovie> GetAllMovies(Activity ctx)
    {
        try
        {
            String jsonMovies = ctx.getPreferences(Context.MODE_PRIVATE).getString(EXTRA_MOVIECACHE, null);

            return new Gson().fromJson(jsonMovies, CinemaResults.class)
                             .getMovies();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return null;
    }
}
