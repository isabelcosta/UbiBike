package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class GivePointsOrChat extends CommonWithButtons {

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

    @Override
    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.ubiconnect_ubichat:
//                intent = new Intent(GivePointsOrChat.this, Chat.class);
                intent = new Intent(GivePointsOrChat.this, MsgSenderActivity.class);

                intent.putExtra("person",person);
                break;

            case R.id.ubiconnect_give_points:
//                intent = new Intent(GivePointsOrChat.this, Chat.class);
                intent = new Intent(GivePointsOrChat.this, Chat.class);
                intent.putExtra("person",person);
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
