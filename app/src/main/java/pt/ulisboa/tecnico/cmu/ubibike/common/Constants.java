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
//    public static final String SERVER_IP = "10.0.3.2";
                                // AVD
    public static final String SERVER_IP = "10.0.2.2";


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
    public static final String RESERVE_BIKE = "reserve bike";


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
    public static final String STATION_NAME = "station name";


    // server responses
    public static final String IS_RIDING_YES = "yes";
    public static final String POINTS_ADDED = "Points added";
    public static final String BIKE_STATIONS_LIST = "bike stations";


    // reserve bike
    public static final int NO_BIKE_ID = 999;
    public static final int BIKE_RESERVED = 1;
    public static final int BIKE_NOT_RESERVED_HAS_RESERVE = 2;
    public static final int BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE = 3;
    public static final String BIKE_RESERVED_TOAST_MSG = "Bike successfully reserved!";
    public static final String BIKE_NOT_RESERVED_HAS_RESERVE_TOAST_MSG = "You already have a bike reserved!";
    public static final String BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE_TOAST_MSG = "There are currently no bikes available at this station";


    // tests
    public static final String ADD_POINTS_TEST_125_ORIGIN = "Gained 125 points during 11/02/16 ride #1";
    public static final String ADD_POINTS_TEST_125 = "125";

    public static final boolean BYPASS_CREDENTIAL_CHECK = true;
    public static final boolean ACCEPT_BIKE_RESERVE_PICOAS = true;

    public static final String TEST_CLIENT_USERNAME = "v";
    public static final String TEST_CLIENT_PASSWORD = "123123";

    // WIFI DIRECT

    public static final String COMMUNICATION_TYPE_WIFI = "communication";
    public static final String GIVE_POINTS_WIFI = "give points";
    public static final String SEND_MESSAGE_WIFI = "send message";
    public static final String MESSAGE_WIFI = "wifi message";
    public static final String POINTS_WIFI = "wifi points";
    public static final String USER_WIFI = "user wifi";



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
