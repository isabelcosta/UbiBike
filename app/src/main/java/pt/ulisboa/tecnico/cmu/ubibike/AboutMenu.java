package pt.ulisboa.tecnico.cmu.ubibike;

import android.os.Bundle;
import android.widget.TextView;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;


public class AboutMenu extends CommonWithButtons {

    protected String bikerName;

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


}
