package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class OptionsMenu extends CommonWithButtons {

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

    @Override
    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {

            // dashboard menu buttons

            case R.id.options_report_bug:
//                 FIXME: 26-Mar-16 change Trajectory
//                intent = new Intent(OptionsMenu.this, TrajectoryMapsActivity.class);
//                intent.putExtra("bikerName",bikerName);
//
                execute = false;
                break;

            case R.id.options_about:
                intent = new Intent(OptionsMenu.this, AboutMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.options_logout:
                execute = false;
                ((UbiBikeApplication) getApplication()).logout();
                break;

            case R.id.menu_bottom_options:
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
