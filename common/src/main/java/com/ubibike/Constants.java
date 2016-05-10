package com.ubibike;


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
    public static final String REMOVE_POINTS = "remove points";
    public static final String GET_POINTS_HISTORY = "get points history";
    public static final String REGISTER_CLIENT = "register client";
    public static final String LOGIN_CLIENT = "login client";
    public static final String GET_CLIENTS = "get clients";
    public static final String GREATING = "greating";
    public static final String IS_RIDING = "is riding";
    public static final String GET_BIKE_STATIONS = "get bike stations";
    public static final String GET_RIDES_HISTORY = "get rides history";
    public static final String ADD_RIDE = "add ride";
    public static final String RESERVE_BIKE = "reserve bike";


    // maps

    public static final int MAPS_ZOOM_LEVEL_STATION = 16;

        // maps intents
        public static final String INTENT_LOCATION_MESSAGE = "locationMessage";
        public static final String INTENT_LATITUDE = "latitude";
        public static final String INTENT_LONGITUDE = "longitude";
        public static final String INTENT_RIDE_NUMBER = "longitude";




    // info for the request
    public static final String CLIENT_NAME = "client name";
    public static final String CLIENT_PASSWORD = "client password";
    public static final String POINTS = "points";
    public static final String CLIENT_POINTS = "client points";
    public static final String POINTS_TO_DECREASE= "points to decrease";
    public static final String POINTS_TO_ADD = "points to add";
    public static final String POINTS_ORIGIN = "points origin";
    public static final String POINTS_ORIGIN_TO_ME = "points origin to me";
    public static final String POINTS_HISTORY = "points history";
    public static final String STATION_NAME = "station name";
    public static final String RIDE_INFO = "ride info";



    // server responses
    public static final String IS_RIDING_YES = "yes";
    public static final String POINTS_ADDED = "Points added";
    public static final String POINTS_NOT_ADDED = "Points not added";
    public static final String POINTS_REMOVED = "Points removed";
    public static final String POINTS_NOT_REMOVED = "Points not removed";
    public static final String BIKE_STATIONS_LIST = "bike stations";
    public static final String RIDES_HISTORY_LIST = "rides history list";


    // reserve bike
    public static final int NO_BIKE_ID = 999;
    public static final String BIKE_ID = "bike id";
    public static final String BIKE_STATUS = "bike status";
    public static final String BIKE_RESERVED = "1";
    public static final String BIKE_NOT_RESERVED_HAS_RESERVE = "2";
    public static final String BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE = "3";
    public static final String BIKE_RESERVED_TOAST_MSG = "Bike successfully reserved!";
    public static final String BIKE_NOT_RESERVED_HAS_RESERVE_TOAST_MSG = "You already have a bike reserved!";
    public static final String BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE_TOAST_MSG = "There are currently no bikes available at this station";


    // tests
    public static final String ADD_POINTS_TEST_10 = "10";
    public static final String ADD_POINTS_TEST_10_ORIGIN = "Received " + ADD_POINTS_TEST_10 + " points from joao";

    public static final boolean BYPASS_CREDENTIAL_CHECK = false;
    public static final boolean ACCEPT_BIKE_RESERVE_PICOAS = false;

    public static final String TEST_CLIENT_USERNAME = "joao";
    public static final String TEST_CLIENT_PASSWORD = "123123";

    public static final String TEST_CLIENT_USERNAME_2 = "joana";
    public static final String TEST_CLIENT_PASSWORD_2 = "123123";


    // WIFI DIRECT

    public static final String COMMUNICATION_TYPE_WIFI = "communication";

    public static final String GIVE_POINTS_WIFI = "give points";
    public static final String SEND_MESSAGE_WIFI = "send message";
    public static final String SEND_INFO_WIFI = "send info";


    public static final String MESSAGE_WIFI = "wifi message";
    public static final String POINTS_WIFI = "wifi points";
    public static final String USER_WIFI = "user wifi";
    public static final String NAME_WIFI = "name wifi";


    // Location Listener
    public static final int GPS_CHECKING_PERIOD = 2000;
    public static final int GPS_CHECKING_DISTANCE = 10;

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
