package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.view.View;
import android.widget.Button;


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
