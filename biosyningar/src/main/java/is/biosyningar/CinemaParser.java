package is.biosyningar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import is.biosyningar.datacontracts.CinemaMovie;
import is.biosyningar.datacontracts.CinemaShowtimes;

/**
 * Created by Arnar on 18.5.2014.
 */
public class CinemaParser
{
    DateFormat filterDateFormat = new SimpleDateFormat("hh:mm");
    boolean afterParseDate;
    Date filterDate;

    public CinemaParser(String time, boolean afterParseDate)
        throws ParseException
    {
        this.afterParseDate = afterParseDate;
        this.filterDate =  filterDateFormat.parse(time);
    }

    public List<CinemaMovie> FilterMoviesByTime(List<CinemaMovie> movies)
            throws ParseException
    {
        ListIterator<CinemaMovie> iteratorMovies = movies.listIterator();

        ParseMovies(iteratorMovies );

        return movies;
    }

    private void ParseMovies(ListIterator<CinemaMovie> iteratorMovies)
            throws ParseException
    {
        while(iteratorMovies.hasNext())
        {
            List<CinemaShowtimes> theaters = iteratorMovies.next().getShowtimes();

            ListIterator<CinemaShowtimes> iteratorTheatres = theaters.listIterator();

            while(iteratorTheatres.hasNext())
            {
                List<String> showTimes = iteratorTheatres.next().getSchedule();

                ListIterator<String> iterator = showTimes.listIterator();

                ProcessShowTimes(iterator, afterParseDate);

                if(showTimes.size() == 0)
                    iteratorTheatres.remove();
            }

            if(theaters.size() == 0)
                iteratorMovies.remove();
        }
    }

    private void ProcessShowTimes(ListIterator<String> iterator, boolean afterParseDate)
            throws ParseException
    {
        while(iterator.hasNext())
        {
            String value = iterator.next();

            String showTimeString = value.substring(0, 4);
            Date showTimeDate = filterDateFormat.parse(showTimeString);

            if (afterParseDate)
            {
                if (filterDate.after(showTimeDate))
                {
                    iterator.remove();
                }
            }
            else
            {
                if (filterDate.before(showTimeDate))
                {
                    iterator.remove();
                }
            }
        }
    }
}
