package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Application;


public class UbiBikeApplication extends Application {

    private String _username;
    private int _score;
    private boolean _status;

    //Getters

    public String get_username() {
        return _username;
    }

    public boolean is_status() {
        return _status;
    }

    public int get_score() {
        return _score;
    }

    //Setters

    public void set_username(String username) {
        this._username = username;
    }

    public void set_status(boolean status) {
        this._status = status;
    }

    public void set_score(int score) {
        this._score = _score;
    }





}
