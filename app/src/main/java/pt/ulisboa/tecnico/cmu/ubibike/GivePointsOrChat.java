package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmu.ubibike.common.Common;

public class GivePointsOrChat extends Common {

    private String bikerName;
    private String person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_give_points_or_chat);

        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        // HEADER
        // biker name
        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        manufacturerTextView.setText(bikerName);

        // person name
        person = getIntent().getStringExtra("person");

        TextView personView = (TextView)findViewById(R.id.person_name);
        personView.setText(person);
    }
    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(GivePointsOrChat.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_ubiconnect:
                intent = new Intent(GivePointsOrChat.this, FindPeersActivity.class);
                break;

            case R.id.menu_bottom_options:
                intent = new Intent(GivePointsOrChat.this, OptionsMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

//            Points History

            case R.id.biker_score:
                intent = new Intent(GivePointsOrChat.this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;
//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(GivePointsOrChat.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.ubiconnect_ubichat:
                intent = new Intent(GivePointsOrChat.this, Chat.class);
                intent.putExtra("bikerName",bikerName);
                intent.putExtra("person",person);
                break;

            case R.id.ubiconnect_give_points:
                intent = new Intent(GivePointsOrChat.this, Chat.class);
                intent.putExtra("bikerName",bikerName);
                intent.putExtra("person",person);
                execute = false;
                break;


        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }
}
