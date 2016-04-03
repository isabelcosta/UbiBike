package pt.ulisboa.tecnico.cmu.ubibike.domain;

/**
 * This class represents all the data related to points lost and earned
 */
public class PointsTranfer {

    private int mode;
    private int points;
    private String peerUsername;
    private String date;

    //Constants
    private static final int EARNED_FROM_A_PEER = 1;
    private static final int EARNED_FROM_A_RIDE = 2;
    private static final int SENT_TO_A_PEER = 3;

    public PointsTranfer (int mode, int points, String peerUsername, String date) {
        this.mode = mode;
        this.points = points;
        this.peerUsername = peerUsername;
        this.date = date;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPeerUsername() {
        return peerUsername;
    }

    public void setPeerUsername(String peerUsername) {
        this.peerUsername = peerUsername;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
