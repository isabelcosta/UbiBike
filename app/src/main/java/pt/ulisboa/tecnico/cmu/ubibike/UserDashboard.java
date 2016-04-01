package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UserDashboard extends AppCompatActivity {

    private String bikerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // HEADER
            // biker name
        TextView bikersName = (TextView)findViewById(R.id.biker_name);
        bikersName.setText(bikerName);



        //      Change color to current menu
        Button messengerBtn = (Button) findViewById(R.id.menu_bottom_home);
        messengerBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        messengerBtn.setTextColor(getResources().getColor(R.color.white));

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {

            // footer buttons
            case R.id.menu_bottom_home:
                execute = false;
                break;

            case R.id.menu_bottom_messenger:
                intent = new Intent(UserDashboard.this, Chat.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_options:
                intent = new Intent(UserDashboard.this, OptionsMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

            // dashboard menu buttons
            case R.id.dashboard_menu_find_me_a_bike:
                intent = new Intent(UserDashboard.this, TrajectoryMapsActivity.class);
                intent.putExtra("bikerName",bikerName);
                break;

//            todo criar meu my rides
            case R.id.dashboard_menu_my_rides:
//                                                     FIXME: 26-Mar-16 change Trajectory
                intent = new Intent(UserDashboard.this, TrajectoryList.class);
                intent.putExtra("bikerName",bikerName);
//                execute = false;
                break;

//            todo criar menu give points
            case R.id.dashboard_menu_give_points:
//                                                         FIXME: 26-Mar-16 change Trajectory
                intent = new Intent(UserDashboard.this, TrajectoryMapsActivity.class);
                intent.putExtra("bikerName",bikerName);
                execute = false;

                break;

//            Points History
            case R.id.biker_score:
                intent = new Intent(UserDashboard.this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;

//          Ubibike Logo
            case R.id.ubibikeLogo:
                execute = false;
                break;

        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }
}
