package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Application;

/**
 * Created by Isabel on 01/04/2016.
 */
public class UbiBikeApplication extends Application {

    private String _username;
    private int _score;
    private boolean _status;

    public String get_username() {
        return _username;
    }

    public void set_username(String username) {
        this._username = username;
    }

    public int get_score() {
        return _score;
    }

    public void set_score(int score) {
        this._score = _score;
    }





}
