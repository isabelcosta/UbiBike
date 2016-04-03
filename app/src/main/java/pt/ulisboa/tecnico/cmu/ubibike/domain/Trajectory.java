package pt.ulisboa.tecnico.cmu.ubibike.domain;

/**
 * This class represents a trajectory made by a user
 */
public class Trajectory {

    private int id;
    private long duration;
    private long distance;
    private String beginingTime;
    private String endTime;
    private String date;

    public Trajectory(long duration, long distance, String beginingTime, String endTime, String date) {
        this.duration = duration;
        this.distance = distance;
        this.beginingTime = beginingTime;
        this.endTime = endTime;
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getDuration() {
        //@TODO beginnig - end time
        return duration;
    }

    public int getId() {
        return id;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setBeginingTime(String beginingTime) {
        this.beginingTime = beginingTime;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }
}
