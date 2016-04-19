package pt.ulisboa.tecnico.cmu.ubibike;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmu.ubibike.common.CommonWithButtons;

public class Chat extends CommonWithButtons {

    private EditText editTxt;
    private Button btn;
    private ListView list;
    private EditText textMsg;
    private ChatListAdapter mAdapter;
    private ArrayList<Message> mMessages;
    private String bikerName;
    private String person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        this.bikerName = ((UbiBikeApplication) getApplication()).getUsername();


        // HEADER
                // biker name
        TextView manufacturerTextView = (TextView)findViewById(R.id.biker_name);
        manufacturerTextView.setText(bikerName);

        // person name
        person = getIntent().getStringExtra("person");

        TextView chatPerson = (TextView)findViewById(R.id.chat_person);
        chatPerson.setText(person);


        // Views
        btn = (Button) findViewById(R.id.btSend1);
        list = (ListView) findViewById(R.id.lvChat);
        textMsg = (EditText) findViewById(R.id.etMessage1);


        mMessages = new ArrayList<>();
        mAdapter = new ChatListAdapter(Chat.this, "Vicente", mMessages);

        list.setAdapter(mAdapter);
        list.setBackgroundColor(getResources().getColor(R.color.grey));


        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Message m = new Message();
                m.setBody(textMsg.getText().toString());
                m.setUserId("Antonio");

                // this line adds the data of your EditText and puts in your array
                mMessages.add(m);
                // next thing you have to do is check if your adapter has changed

                m = new Message();
                m.setBody("auto response");
                        m.setUserId("Joao");
                // this line adds the data of your EditText and puts in your array
                mMessages.add(m);

                mAdapter.notifyDataSetChanged();
                textMsg.setText(null);
            }
        });

    }



//    public void launchClick(View v) {
//        Intent intent = null;
//        Boolean execute = true;
//
//        switch(v.getId()) {
//            case R.id.menu_bottom_home:
//                intent = new Intent(Chat.this, UserDashboard.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//
//            case R.id.menu_bottom_ubiconnect:
//                intent = new Intent(Chat.this, UbiconnectActivity.class);
//                break;
//
//            case R.id.menu_bottom_options:
//                intent = new Intent(Chat.this, OptionsMenu.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//
////            Points History
//
//            case R.id.biker_score:
//                intent = new Intent(Chat.this, ScoreHistory.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
////          Ubibike Logo
//            case R.id.ubibikeLogo:
//                intent = new Intent(Chat.this, UserDashboard.class);
//                intent.putExtra("bikerName",bikerName);
//                break;
//        }
//        if (execute){
//            startActivityForResult(intent, 0);
//        }
//    }

}
