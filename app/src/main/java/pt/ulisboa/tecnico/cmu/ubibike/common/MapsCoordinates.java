package pt.ulisboa.tecnico.cmu.ubibike.common;

/**
 * Created by vicente on 18-Apr-16.
 */
public class MapsCoordinates {

    private double longitude;
    private double latitude;

    public MapsCoordinates(double latitude, double longitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
