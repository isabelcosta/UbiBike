package pt.ulisboa.tecnico.cmu.ubibike.common;

/**
 * Created by vicente on 08-Apr-16.
 */public class Constants {

    /**
     *
     *  SERVER connection
     *
     */

    public static final int SERVER_PORT = 4432;
                                // genymotion
    public static final String SERVER_IP = "10.0.3.2";
                                // AVD
//    public static final String SERVER_IP = "10.0.2.2";


    /**
     *
     *  SERVER JSON variables
     *
     */

    //  request type
    public static final String REQUEST_TYPE = "type";

    public static final String GET_POINTS = "get points";
    public static final String ADD_POINTS = "add points";
    public static final String GET_POINTS_HISTORY = "get points history";
    public static final String REGISTER_CLIENT = "register client";
    public static final String LOGIN_CLIENT = "login client";
    public static final String GET_CLIENTS = "get clients";
    public static final String GREATING = "greating";
    public static final String IS_RIDING = "is riding";
    public static final String GET_BIKE_STATIONS = "get bike stations";


    // maps

    public static final int MAPS_ZOOM_LEVEL_STATION = 16;

        // maps intents
        public static final String INTENT_LOCATION_MESSAGE = "locationMessage";
        public static final String INTENT_LATITUDE = "latitude";
        public static final String INTENT_LONGITUDE = "longitude";




    // info for the request
    public static final String CLIENT_NAME = "client name";
    public static final String CLIENT_PASSWORD = "client password";
    public static final String POINTS = "points";
    public static final String CLIENT_POINTS = "client points";
    public static final String POINTS_ORIGIN = "points origin";
    public static final String POINTS_HISTORY = "points history";

    // server responses
    public static final String IS_RIDING_YES = "yes";
    public static final String POINTS_ADDED = "Points added";
    public static final String BIKE_STATIONS_LIST = "bike stations";


    // tests
    public static final String ADD_POINTS_TEST_125_ORIGIN = "Gained 125 points during 11/02/16 ride #1";
    public static final String ADD_POINTS_TEST_125 = "125";

    public static final boolean BYPASS_CREDENTIAL_CHECK = true;

    public static final String TEST_CLIENT_USERNAME = "v";
    public static final String TEST_CLIENT_PASSWORD = "123123";

    /**
     *
     *  ACTIVITY STATE
     *
     */

    public static final String MY_PREFS = "UbibikeSP";

    public static final String PREF_BIKER_SCORE = "biker_score";
    public static final String PREF_BIKER_SCORE_DEFAULT = "0";

    public static final String PREF_SCORE_HISTORY = "score_history";
    public static final String PREF_SCORE_HISTORY_DEFAULT = "empty";


}
