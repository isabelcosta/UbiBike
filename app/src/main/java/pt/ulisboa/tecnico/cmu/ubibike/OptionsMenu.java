package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OptionsMenu extends AppCompatActivity {

    private String bikerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_menu);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();




        // HEADER
        // biker name
        TextView bikersName = (TextView)findViewById(R.id.biker_name);
        bikersName.setText(bikerName);




        //      Change color to current menu
        Button optionsBtn = (Button) findViewById(R.id.menu_bottom_options);
        optionsBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        optionsBtn.setTextColor(getResources().getColor(R.color.white));
    }


    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {

            // footer buttons
            case R.id.menu_bottom_home:
                intent = new Intent(OptionsMenu.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_ubiconnect:
                intent = new Intent(OptionsMenu.this, FindPeersActivity.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_options:
                execute = false;
                break;

            // dashboard menu buttons

            case R.id.options_report_bug:
//                 FIXME: 26-Mar-16 change Trajectory
//                intent = new Intent(OptionsMenu.this, TrajectoryMapsActivity.class);
//                intent.putExtra("bikerName",bikerName);
//
                execute = false;
                break;

//            todo criar menu about
            case R.id.options_about:
//                 FIXME: 26-Mar-16 change Trajectory
                intent = new Intent(OptionsMenu.this, AboutMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.options_logout:
                execute = false;
                logout();
                break;

//            Points History

            case R.id.biker_score:
                intent = new Intent(OptionsMenu.this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;
//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(OptionsMenu.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }

    private void logout() {
        String spFilename = UbiBikeApplication.SHARED_PREFERENCE_FILENAME;
        SharedPreferences pref = getApplication().getSharedPreferences(spFilename, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        Intent intent = new Intent(OptionsMenu.this, LoginActivity.class);
        startActivity(intent);
    }
}
