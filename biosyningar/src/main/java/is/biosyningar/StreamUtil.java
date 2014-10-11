package is.biosyningar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import retrofit.mime.TypedInput;

public class StreamUtil
{
    public static String ConvertStreamToString(InputStream stream) throws Exception
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

    public static String GetJsonResponseString(TypedInput body)
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
}
