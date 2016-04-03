package pt.ulisboa.tecnico.cmu.ubibike;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.hide();
        }

    /* New Handler to start the Menu-Activity
     * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                UbiBikeApplication app = (UbiBikeApplication) getApplication();
                app.setUsername(null, true);
                Intent mainIntent;
                if(app.isUserLoggedIn()) {
                    mainIntent = new Intent(SplashScreen.this, UserDashboard.class);
                } else {
                    mainIntent = new Intent(SplashScreen.this, LoginActivity.class);
                }

                /* Create an Intent that will start the Menu-Activity. */
                SplashScreen.this.startActivity(mainIntent);
                SplashScreen.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
