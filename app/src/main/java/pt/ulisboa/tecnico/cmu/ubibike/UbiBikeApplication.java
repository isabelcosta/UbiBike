package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class UbiBikeApplication extends Application {

    private String _username;
    private int _score;
    private boolean _status;
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private ArrayList<String> DUMMY_CREDENTIALS;

    // Shared preferences file name
    public static final String SHARED_PREFERENCE_FILENAME = "UbibikeSP";

    public UbiBikeApplication() {
        String[] credentialsArray = {
                "foo@example.com:hello", "bar@example.com:world",
                "isabel:costa", "pedro:dias", "vicente:rocha"
        };
        DUMMY_CREDENTIALS = new ArrayList<>(Arrays.asList(credentialsArray));
    }

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

    public List<String> getDummyCredentials() {
        return DUMMY_CREDENTIALS;
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

    public void addCredentials(String credentials) {
        this.DUMMY_CREDENTIALS.add(credentials);
    }
    public List<String> getDummyCredentials(String credentials) {
        return this.DUMMY_CREDENTIALS;
    }

/*
    public void bottomMenuClickAction(Activity activity){
        if(!(activity.getClass().isInstance(UserDashboard.class))){
            Button homeBtn = (Button) activity.findViewById(R.id.menu_bottom_home);
            homeBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), UserDashboard.class);
                    startActivity(intent);
                }
            });
        }

        if(!(activity.getClass().isInstance(Chat.class))){
            Button chatBtn = (Button) activity.findViewById(R.id.menu_bottom_messenger);
            chatBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Chat.class);
                    startActivity(intent);
                }
            });
        }

        if(!(activity.getClass().isInstance(OptionsMenu.class))){
            Button optionsBtn = (Button) activity.findViewById(R.id.menu_bottom_options);
            optionsBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), OptionsMenu.class);
                    startActivity(intent);
                }
            });
        }
    }*/


}
