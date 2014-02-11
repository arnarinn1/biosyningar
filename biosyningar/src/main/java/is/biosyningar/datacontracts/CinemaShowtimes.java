package is.biosyningar.datacontracts;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CinemaShowtimes
{
    @SerializedName("theater")
    public String theater;

    @SerializedName("schedule")
    public List<String> schedule;

    public String getTheater() { return this.theater; }
    public List<String> getSchedule () { return this.schedule; }
}
