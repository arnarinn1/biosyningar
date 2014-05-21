package is.biosyningar;

import retrofit.RestAdapter;
import retrofit.client.ApacheClient;

/**
 * Created by Arnar on 22.3.2014.
 */
public class RetrofitUtil
{
    static final String ApisUrl = "http://apis.is";

    public static RestAdapter RestAdapterInstance()
    {
        return new RestAdapter.Builder()
                              .setEndpoint(ApisUrl)
                              .setClient(new ApacheClient())
                              .build();
    }
}
