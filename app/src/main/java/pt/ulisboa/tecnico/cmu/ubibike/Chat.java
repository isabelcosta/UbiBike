package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    private EditText editTxt;
    private Button btn;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    private String bikerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        // HEADER
                // biker name
        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        bikerName = getIntent().getStringExtra("bikerName");
        manufacturerTextView.setText(bikerName);



        btn = (Button) findViewById(R.id.button);
        list = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this line adds the data of your EditText and puts in your array
                arrayList.add("test");
                // next thing you have to do is check if your adapter has changed
                adapter.notifyDataSetChanged();
            }
        });

//      Change color to current menu
        Button messengerBtn = (Button) findViewById(R.id.menu_bottom_messenger);
        messengerBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        messengerBtn.setTextColor(getResources().getColor(R.color.white));


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(Chat.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;

            case R.id.menu_bottom_messenger:
//                intent = new Intent(Chat.this, Chat.class);
                execute = false;
                break;

            case R.id.menu_bottom_options:
                intent = new Intent(Chat.this, OptionsMenu.class);
                intent.putExtra("bikerName",bikerName);
                break;

//            Points History

            case R.id.biker_score:
                intent = new Intent(Chat.this, ScoreHistory.class);
                intent.putExtra("bikerName",bikerName);
                break;
//          Ubibike Logo
            case R.id.ubibikeLogo:
                intent = new Intent(Chat.this, UserDashboard.class);
                intent.putExtra("bikerName",bikerName);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    }

}
