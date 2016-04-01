package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ScoreHistory extends AppCompatActivity {

    private String bikerName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);
        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();

        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);;
        bikerName = getIntent().getStringExtra("bikerName");
        manufacturerTextView.setText(bikerName);
    }

    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(ScoreHistory.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_messenger:
                intent = new Intent(ScoreHistory.this, Chat.class);

                break;

            case R.id.menu_bottom_options:
                intent = new Intent(ScoreHistory.this, OptionsMenu.class);
//                execute = false;
                break;

            //            Points History

            case R.id.biker_score:
                execute = false;
                break;
//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(ScoreHistory.this, UserDashboard.class);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }
}
