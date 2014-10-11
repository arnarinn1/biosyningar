package is.biosyningar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import is.biosyningar.datacontracts.CinemaMovie;
import is.biosyningar.datacontracts.CinemaShowtimes;

public class CinemaParser
{
    private DateFormat filterDateFormat = new SimpleDateFormat("hh:mm");
    private boolean afterParseDate;
    private Date filterDate;
    private Set<String> mCinemaSet;

    public CinemaParser(String time, boolean afterParseDate, Set<String> cinemaSet)
        throws ParseException
    {
        this.afterParseDate = afterParseDate;
        this.filterDate =  filterDateFormat.parse(time);
        this.mCinemaSet = cinemaSet;
    }

    public List<CinemaMovie> FilterMoviesByTime(List<CinemaMovie> movies)
            throws ParseException
    {
        FilterByTime(movies.listIterator());

        FilterByCinema(movies.listIterator());

        return movies;
    }

    private void FilterByTime(ListIterator<CinemaMovie> iteratorMovies)
            throws ParseException
    {
        while(iteratorMovies.hasNext())
        {
            List<CinemaShowtimes> theaters = iteratorMovies.next().getShowtimes();

            ListIterator<CinemaShowtimes> iteratorTheatres = theaters.listIterator();

            while(iteratorTheatres.hasNext())
            {
                List<String> showTimes = iteratorTheatres.next().getSchedule();

                ProcessShowTimes(showTimes.listIterator(), afterParseDate);

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

    private void FilterByCinema(ListIterator<CinemaMovie> cinemaMovieListIterator)
    {
        while(cinemaMovieListIterator.hasNext())
        {
            List<CinemaShowtimes> theaters = cinemaMovieListIterator.next().getShowtimes();

            FilterTheaters(theaters.listIterator());

            if(theaters.size() == 0)
                cinemaMovieListIterator.remove();
        }
    }

    private void FilterTheaters(ListIterator<CinemaShowtimes> iterator)
    {
        if (mCinemaSet.isEmpty()) return;

        while(iterator.hasNext())
        {
            if (!mCinemaSet.contains(iterator.next().getTheater().toLowerCase()))
                iterator.remove();
        }
    }
}
