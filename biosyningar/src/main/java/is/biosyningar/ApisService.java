package is.biosyningar;

import is.biosyningar.datacontracts.CinemaResults;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface ApisService
{
    @GET("/{endpoint}")
    void getMovies
    (
        @Path("endpoint") String endpoint,
                          Callback<CinemaResults> movies
    );
}
