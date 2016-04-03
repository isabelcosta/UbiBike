package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;




public class AboutMenu extends AppCompatActivity {

    private String bikerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_menu);


        // HEADER
        // biker name
        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        bikerName = getIntent().getStringExtra("bikerName");
        manufacturerTextView.setText(bikerName);


    }
    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(AboutMenu.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_ubiconnect:
                intent = new Intent(AboutMenu.this, FindPeersActivity.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_options:
                intent = new Intent(AboutMenu.this, OptionsMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

//            Points History

            case R.id.biker_score:
                intent = new Intent(AboutMenu.this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;

//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(AboutMenu.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }

}
