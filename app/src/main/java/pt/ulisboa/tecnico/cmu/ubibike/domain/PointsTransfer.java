package pt.ulisboa.tecnico.cmu.ubibike.domain;

import org.json.JSONObject;

/**
 * This class represents all the data related to points lost and earned
 */
public class PointsTransfer {

    private int mode;
    private int points;
    private String peerUsername;


    private JSONObject json;

    //Constants
    public static final int EARNED_FROM_A_PEER = 1;
    public static final int EARNED_FROM_A_RIDE = 2;
    public static final int SENT_TO_A_PEER = 3;

    public PointsTransfer(int mode, int points, String peerUsername, JSONObject json) {
        this.mode = mode;
        this.points = points;
        this.peerUsername = peerUsername;
        this.json = json;
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


    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

}
