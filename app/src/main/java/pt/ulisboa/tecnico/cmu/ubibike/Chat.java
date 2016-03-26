package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Chat extends AppCompatActivity {

    private EditText editTxt;
    private Button btn;
    private Button btn2;
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        TextView manufacturerTextView = (TextView)findViewById(R.id.textView);;
// Now you can set TextView's text using setText() method:
        String name = getIntent().getStringExtra("username");

        manufacturerTextView.setText(name);


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
                arrayList.add("Mega LOL");
                // next thing you have to do is check if your adapter has changed
                adapter.notifyDataSetChanged();
            }
        });





        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void launchClick(View v) {
        Intent intent = null;
        Boolean execute = true;

        switch(v.getId()) {
            case R.id.menu_bottom_home:
                intent = new Intent(Chat.this, LoginActivity.class);
                break;

            case R.id.menu_bottom_messenger:
//                intent = new Intent(Chat.this, Chat.class);
                execute = false;
                break;

            case R.id.menu_bottom_options:
                intent = new Intent(Chat.this, TrajectoryMapsActivity.class);
                break;
        }
        if (execute){
            startActivityForResult(intent, 0);
        }
    };

}
