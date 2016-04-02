package pt.ulisboa.tecnico.cmu.ubibike.domain;

/**
 * This class representes a trajectory made by a user
 */
public class Trajectory {

    private long duration;
    private long distance;
    private String time;
    private String date;

    public Trajectory(long duration, long distance, String time, String date) {
        this.duration = duration;
        this.distance = distance;
        this.time = time;
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
