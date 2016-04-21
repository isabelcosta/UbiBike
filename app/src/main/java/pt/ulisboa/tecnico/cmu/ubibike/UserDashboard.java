package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class UserDashboard extends CommonWithButtons {

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

    @Override
    public void launchClick(View v) {

        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {

            case R.id.dashboard_menu_rides_history:
                intent = new Intent(UserDashboard.this, UbiRidesHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.dashboard_menu_find_bike:
                intent = new Intent(UserDashboard.this, StationsList.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.dashboard_menu_biker_score:
                intent = new Intent(UserDashboard.this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                execute = true;
                break;

            case R.id.dashboard_menu_riding:
                //intent = new Intent(UserDashboard.this, UbiRideMapsActivity.class);
                intent = new Intent(UserDashboard.this, MsgSenderActivity.class);
                intent.putExtra("bikerName",bikerName);
                execute = true;
                break;
            case R.id.ubibikeLogo:
                execute = false;
                break;

            default:
                super.launchClick(v);
                return;

        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }
}
