package pt.ulisboa.tecnico.cmu.ubibike.domain;

/**
 * This class represents a User of this application aka a biker
 */
public class User {
    private String username;
    private int score;
    private int bikeId;

    public User(String username) {
        this.username = username;
    }

    public int getBikeId() {
        return bikeId;
    }

    public int getScore() {
        return score;
    }

    public String getUsername() {
        return username;
    }

    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
