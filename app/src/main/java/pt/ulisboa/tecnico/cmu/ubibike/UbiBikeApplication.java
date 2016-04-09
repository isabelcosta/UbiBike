package pt.ulisboa.tecnico.cmu.ubibike;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.PREF_BIKER_SCORE;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.PREF_BIKER_SCORE_DEFAULT;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.PREF_SCORE_HISTORY;
import static pt.ulisboa.tecnico.cmu.ubibike.common.Constants.PREF_SCORE_HISTORY_DEFAULT;


public class UbiBikeApplication extends Application {

    private String _username;
    private int _score;
    private boolean _status;
    private SharedPreferences prefs;
    private Editor editor;
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private ArrayList<String> DUMMY_CREDENTIALS;

    /**
     * Constants
     */
    // Shared preferences file name
    public static final String SHARED_PREFERENCE_FILENAME = "UbibikeSP";
    public static final String SP_USERNAME = "Username";
    public static final String SP_IS_USER_LOGGED = "IsLogged";

    /**
     * Constructor
     */
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

    public void setUsername(String username, boolean fromSharedPreferences) {
        if (fromSharedPreferences){
            // creating an shared Preference file for the information to be stored
            prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
            this._username = prefs.getString(SP_USERNAME, null);
        } else {
            this._username = username;
        }
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

    public void sendTrajectory(long distance, String date, long duration) {

    }

    public void logout() {
        SharedPreferences pref = getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        Intent intent = new Intent(this, LoginActivity.class);

        // Closing all the Activities
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    public void saveCredentials(String username) {

        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        // get editor to edit in file
        editor = prefs.edit();

        // as now we have information in string. Lets stored them with the help of editor
        editor.putString(SP_USERNAME, username);
        editor.putBoolean(SP_IS_USER_LOGGED, true);
        editor.commit();
    }

    // Check for login
    public boolean isUserLoggedIn(){
        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
        Boolean isLogged = prefs.getBoolean(SP_IS_USER_LOGGED, false);
        return isLogged;
    }

    public void saveBikerScore(String bikerScore) {

        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        // get editor to edit in file
        editor = prefs.edit();

        // as now we have information in string. Lets stored them with the help of editor
        editor.putString(PREF_BIKER_SCORE, bikerScore);

        //todo consider using apply()
        editor.commit();
    }


    public String getBikerScore() {

        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        String bikerScore = prefs.getString(PREF_BIKER_SCORE, PREF_BIKER_SCORE_DEFAULT);

        return bikerScore;
    }

    public void saveBikerScoreHistory(String bikerScoreHistory) {

        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        // get editor to edit in file
        editor = prefs.edit();

        // as now we have information in string. Lets stored them with the help of editor
        editor.putString(PREF_SCORE_HISTORY, bikerScoreHistory);

        //todo consider using apply()
        editor.commit();
    }


    public String getBikerScoreHistory() {

        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);

        String bikerScoreHistory = prefs.getString(PREF_SCORE_HISTORY, PREF_SCORE_HISTORY_DEFAULT);

        return bikerScoreHistory;
    }


/*
    /**
     * Gets username of logged user from Shared Preferences
     * @return username
     */
/*
    public String getUsernameFromSP(){
        // creating an shared Preference file for the information to be stored
        prefs = getApplicationContext().getSharedPreferences(SHARED_PREFERENCE_FILENAME, MODE_PRIVATE);
        String username = prefs.getString(SP_USERNAME, null);
        return username;
    }
*/
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
