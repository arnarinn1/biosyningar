package is.biosyningar.datacontracts;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CinemaResults
{
    @SerializedName("results")
    public List<CinemaMovie> movies;

    public List<CinemaMovie> getMovies() { return this.movies; }
}
