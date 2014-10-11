package is.biosyningar;

import android.app.Activity;

import com.google.gson.Gson;
import java.util.List;

import is.biosyningar.datacontracts.CinemaMovie;
import is.biosyningar.datacontracts.CinemaResults;

public class GsonUtil
{
    public static String EXTRA_MOVIECACHE = "is.biosyningar.MOVIECACHE";

    public static List<CinemaMovie> GetAllMovies(Activity ctx)
    {
        try
        {
            String jsonMovies = Prefs.with(ctx).GetString(EXTRA_MOVIECACHE, null);

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
