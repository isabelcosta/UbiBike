package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Application;


public class UbiBikeApplication extends Application {

    private String _username;
    private int _score;
    private boolean _status;

    //Getters

    public String getUsername() {
        return _username;
    }

    public boolean isStatus() {
        return _status;
    }

    public int getScore() {
        return _score;
    }

    //Setters

    public void setUsername(String username) {
        this._username = username;
    }

    public void setStatus(boolean status) {
        this._status = status;
    }

    public void setScore(int score) {
        this._score = _score;
    }





}
