package pt.ulisboa.tecnico.cmu.ubibike.Server;


import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static com.ubibike.Constants.*;



/**
 * This is a simple server application. This server receive a string message
 * from the Android mobile phone and show it on the console.
 * Author by Lak J Comspace
 */
public class UbiServer {

    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static String message;

    private static HashMap<String, UbiClient> clientsList = new HashMap<>();
    private static HashMap<String, MapsCoordinates> bikeStations = new HashMap<>();
    private static HashMap<String, HashMap<Integer, Boolean>> bikesPerStation = new HashMap<>();
    private static HashMap<String, ArrayList<MapsCoordinates>> ridesHistory = new HashMap<>();


    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(SERVER_PORT); // Server socket

        } catch (IOException e) {
            System.out.println("Could not listen on port: " + SERVER_PORT);
        }

        System.out.println("Server started. Listening to the port " + SERVER_PORT);

        /**
         *  TESTING
         */

        // create client "v" for testing purposes
        createTestClient();

        // define bike stations
        createBikeStations();

        /**
         *  -------------------------
         */



        while (true) {
            try {
				/**
				 * client Socket and DataStreams
				 * */

                clientSocket = serverSocket.accept();

                DataInputStream dataInputStream = new DataInputStream(
                        clientSocket.getInputStream()); // used to get messages FROM the CLIENT

                DataOutputStream dataOutputStream = new DataOutputStream(
                        clientSocket.getOutputStream()); // use to send messages TO the CLIENT
                /**
                 *  -----------------------------------
                 * */



                // message from the client
                message = dataInputStream.readUTF();

                // create the json object from the String
                final JSONObject jsondata;
                jsondata = new JSONObject(message);

                // get the type of message to know what the client wants
                String type = jsondata.getString(REQUEST_TYPE);
                System.out.println("Type: " + type);


                if (type.equals
                        (REGISTER_CLIENT))
                {
                    boolean canRegister = registerClient(jsondata);

                    if (canRegister) {
                        dataOutputStream.writeUTF("ok");
                    } else {
                        dataOutputStream.writeUTF("username already exists..");
                    }
                }
                else if (type.equals
                        (LOGIN_CLIENT))
                {
                    boolean canLogin = loginClient(jsondata);

                    if (canLogin) {
                        dataOutputStream.writeUTF("ok");
                    } else {
                        dataOutputStream.writeUTF("Pair username/password doesn't match..");
                    }
                }
                else if (type.equals
                        (GET_CLIENTS))
                {
                    String clients = getClients();
                    dataOutputStream.writeUTF(clients);
                }
                else if (type.equals
                        (GET_POINTS_HISTORY)) {
                    JSONObject json = getPointsHistory(jsondata);
                    dataOutputStream.writeUTF(json.toString());
                }
                else if (type.equals
                        (GREATING))
                {

                    String greating = jsondata.getString(GREATING);
                    System.out.println("Greating " + greating);

                    String messageToClient = "Greetings from the Server";
                    dataOutputStream.writeUTF(messageToClient);
                }
                else if (type.equals
                        (ADD_POINTS))
                {
                    // call function that adds points to the client
                    if(!addPoints(jsondata)) {
                            // send message to clients confirming that the points were NOT added
                        String messageToClient = POINTS_NOT_ADDED;
                        dataOutputStream.writeUTF(messageToClient);

                    } else {
                        // send message to clients confirming that the points were added
                        String messageToClient = POINTS_ADDED;
                        dataOutputStream.writeUTF(messageToClient);
                    }

                }
                else if (type.equals
                        (REMOVE_POINTS))
                {
                    String messageToClient;

                        // call function that decreases points to the client
                    if (!decreasePoints(jsondata)) {
                            // send message to clients confirming that the points were NOT removed
                        messageToClient = POINTS_NOT_REMOVED;
                        dataOutputStream.writeUTF(messageToClient);
                    } else {
                            // send message to clients confirming that the points were removed
                        messageToClient = POINTS_REMOVED;
                        dataOutputStream.writeUTF(messageToClient);
                    }

                }
                else if (type.equals
                        (GET_POINTS))
                {
                    // invoke getPoints to get the client points
                    JSONObject json = getPoints(jsondata);

                    dataOutputStream.writeUTF(json.toString());
                }
                else if (type.equals
                        (IS_RIDING))
                {

                    // todo is always true
                    JSONObject json = new JSONObject();
                    json.put(IS_RIDING, IS_RIDING_YES);
                    dataOutputStream.writeUTF(json.toString());

                }
                else if (type.equals
                        (GET_BIKE_STATIONS))
                {
                    // invoke getStations() to get the list of stations in XML string on a JSONObject
                    JSONObject json = getStations();

                    dataOutputStream.writeUTF(json.toString());

                }
                else if (type.equals
                        (RESERVE_BIKE))
                {
                    // invoke reserveBike() to get try to reserve a bike
                    int reserve = reserveBike(jsondata);

                    // send reservation result to server
                    dataOutputStream.writeUTF(String.valueOf(reserve));

                }
                else if (type.equals
                        (GET_RIDES_HISTORY))
                {
                    // invoke getStations() to get the list of stations in XML string on a JSONObject
                    JSONObject json = getRidesHistory(jsondata);

                    dataOutputStream.writeUTF(json.toString());

                }
                else if (type.equals
                        (ADD_RIDE))
                {
                    // invoke getStations() to get the list of stations in XML string on a JSONObject
                    addRide(jsondata);

                    dataOutputStream.writeUTF("ride added");

                }
                clientSocket.close();

            } catch (IOException ex) {
                System.out.println("Problem in message reading");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JDOMException e) {
                e.printStackTrace();
            }
        }

    }



    private static boolean registerClient(JSONObject jsondata) throws JSONException {

        // get client name from jsondata - "client name"
        String clientName = jsondata.getString(CLIENT_NAME);
        String clientPassword = jsondata.getString(CLIENT_PASSWORD);

        if (clientsList.containsKey(clientName)) {
            return false;
        }

        // create a new UbiClient with clientName
        UbiClient client = new UbiClient(clientName);

        // set user password
        client.setPassword(clientPassword);

        // set starting point to 100
        // TODO - FIXME - just for testing purposes
        client.setPoints(100);

        // add the client to the HashMap<String,UbiClient> clientsList
        clientsList.put(clientName,client);

        return true;


    }

    private static boolean loginClient(JSONObject jsondata) throws JSONException {

        // get client name from jsondata - "client name"
        String clientName = jsondata.getString(CLIENT_NAME);
        String clientPassword = jsondata.getString(CLIENT_PASSWORD);

        if (!clientsList.containsKey(clientName)) {
            return false;
        }

        if (clientsList.get(clientName).getPassword() == null) {
            return false;
        }

        return clientsList.get(clientName).getPassword()
                    .equals(clientPassword);


    }

    private static String getClients () {
        StringBuilder sb = new StringBuilder();
        for (UbiClient s : clientsList.values())
        {
            sb.append(s.getName());
            sb.append("\t");
        }
        return sb.toString();
    }

    private static JSONObject getPointsHistory(JSONObject jsondata) throws JSONException {

        String clientName = jsondata.getString(CLIENT_NAME);
        int clientPoints = clientsList.get(clientName).getPoints();
        ArrayList<String> pointsOrigin = clientsList.get(clientName).getPointsHistory();


        System.out.println("name " + clientName);
        System.out.println("points " + clientPoints);

        JSONObject json = new JSONObject();

        json.put(POINTS, clientPoints);


        StringBuilder sb = new StringBuilder();
        for (String s : pointsOrigin)
        {
            sb.append(s);
            System.out.println(s);
            sb.append("\t");
        }

        json.put(POINTS_HISTORY, sb.toString());



        return json;

    }


    private static boolean addPoints(JSONObject jsondata) throws JSONException {


            // get client points from jsondata
        String toAddPoints = jsondata.getString(POINTS_TO_ADD);
        System.out.println("Points to add " + toAddPoints);

            //get sender name from jsondata
        String senderName = jsondata.getString(USER_WIFI);
            // get client points from server data
        int senderPoints = clientsList.get(senderName).getPoints();
            // check if the sender has enough points to send
        System.out.println(senderPoints + " send points");
        System.out.println(toAddPoints + " toAddPoints");



        if (senderPoints - Integer.parseInt(toAddPoints) < 0) {return false;}

        clientsList.get(senderName).setPoints(senderPoints - Integer.parseInt(toAddPoints));


        //get client name from jsondata
        String clientName = jsondata.getString(CLIENT_NAME);

            // get client points from server data
        int currentPoints = clientsList.get(clientName).getPoints();

            // update client points with the received points
        clientsList.get(clientName).setPoints(currentPoints + Integer.parseInt(toAddPoints));

            // get client points history
        ArrayList<String> pointsOrigin = clientsList.get(clientName).getPointsHistory();

            // add new points to the history and update on server
        pointsOrigin.add(jsondata.getString(POINTS_ORIGIN));
        clientsList.get(clientName).setPointsHistory(pointsOrigin);

        return true;
    }
    private static boolean decreasePoints(JSONObject jsondata) throws JSONException {

        // get client points from jsondata
        String toRemovePoints = jsondata.getString(POINTS_TO_DECREASE);
        System.out.println("Points to decrease " + toRemovePoints);

        //get client name form jsondata
        String clientName = jsondata.getString(CLIENT_NAME);

        // get client points from server data
        int currentPoints = clientsList.get(clientName).getPoints();

            // if the user doesn't have enough points it wont update
        if (currentPoints - Integer.parseInt(toRemovePoints) < 0) {return false;}

        // update client points with the decreased points
        clientsList.get(clientName).setPoints(currentPoints - Integer.parseInt(toRemovePoints));

        // get client points history
        ArrayList<String> pointsOrigin = clientsList.get(clientName).getPointsHistory();

        // add new points to the history and update on server
        pointsOrigin.add(jsondata.getString(POINTS_ORIGIN));
        clientsList.get(clientName).setPointsHistory(pointsOrigin);

        return true;
    }

    private static JSONObject getPoints(JSONObject jsondata) throws JSONException {

        // get client name
        String clientName = jsondata.getString(CLIENT_NAME);

        int clientPoints = clientsList.get(clientName).getPoints();

        JSONObject json = new JSONObject();
        json.put(POINTS, clientPoints);

        return json;
    }

    private static JSONObject getStations() throws JSONException {
        JSONObject json = new JSONObject();


        // create XML representing the bike stations
        Element bikeStationsXML = new Element("bikeStations");
        Document doc = new Document(bikeStationsXML);
//        doc.setRootElement(bikeStationsXML);


        int i = 0;
        // for each station, add latitude, longitude and marker text
        for (String markerID:
                bikeStations.keySet()) {

            MapsCoordinates coor = bikeStations.get(markerID);

            Element coordinates = new Element("stationsCoordinates");
            coordinates.setAttribute(new Attribute("id", String.valueOf(i)));

            coordinates.addContent(new Element("latitude").
                    setText(String.valueOf(coor.getLatitude())));
            coordinates.addContent(new Element("longitude")
                    .setText(String.valueOf(coor.getLongitude())));
            coordinates.addContent(new Element("marker")
                    .setText(markerID));

            doc.getRootElement().addContent(coordinates);

            i++;

        }

        // create a string from the xml
        XMLOutputter xmlOutput = new XMLOutputter();
        String bikeStationsString = xmlOutput.outputString(doc);
        System.out.println(bikeStationsString);

        // put the xml with the bike stations on the json object
        json.put(BIKE_STATIONS_LIST,bikeStationsString);

        return json;
    }


 private static JSONObject getRidesHistory(JSONObject jsondata) throws JSONException {
        JSONObject json = new JSONObject();

        String clientName = jsondata.getString(CLIENT_NAME);


        // create XML representing the bike stations
        Element ridesHistoryXML = new Element("ridesHistory");
        Document doc = new Document(ridesHistoryXML);
//        doc.setRootElement(bikeStationsXML);

        HashMap<String, ArrayList<MapsCoordinates>> clientTrajectories = clientsList.get(clientName).getTrajectories();


        int i = 0;
        // for each station, add latitude, longitude and marker text
        for (String ride:
                clientTrajectories.keySet()) {

            ArrayList<MapsCoordinates> coords = clientTrajectories.get(ride);

            Element rides = new Element("rides");
            rides.setAttribute(new Attribute("id", String.valueOf(i)));

//            Element coordinates = new Element("rides");
//            coordinates.setAttribute(new Attribute("id", String.valueOf(i)));
//
//
//            coordinates.addContent(new Element("latitude").
//                    setText(String.valueOf(coor.getLatitude())));
//            coordinates.addContent(new Element("longitude")
//                    .setText(String.valueOf(coor.getLongitude())));
//            coordinates.addContent(new Element("marker")
//                    .setText(markerID));
//
//            doc.getRootElement().addContent(coordinates);

            int a = 0;
            for (MapsCoordinates coor:
                    coords) {
                Element coordinates = new Element("coordinates");
                coordinates.setAttribute(new Attribute("id", String.valueOf(a)));

                coordinates.addContent(new Element("latitude")
                        .setText(String.valueOf(coor.getLatitude())));
                coordinates.addContent(new Element("longitude")
                        .setText(String.valueOf(coor.getLongitude())));

                rides.addContent(coordinates);
                a++;
            }

            doc.getRootElement().addContent(rides);

            i++;

        }

        // create a string from the xml
        XMLOutputter xmlOutput = new XMLOutputter();
        String ridesHistoryString = xmlOutput.outputString(doc);
        System.out.println("rides history of " + clientName);
        System.out.println(ridesHistoryString);
        // put the xml with the bike stations on the json object
        json.put(RIDES_HISTORY_LIST, ridesHistoryString);

        return json;
    }



    private static void addRide(JSONObject jsondata) throws JSONException, IOException, JDOMException {

        String clientName = jsondata.getString(CLIENT_NAME);

        String rideString = jsondata.getString(RIDE_INFO);

        // instanciate SAXBuilder to parse the String to XML
        SAXBuilder builder = new SAXBuilder();
        // create a stream to be converted to a JDOM document
        InputStream stream = new ByteArrayInputStream(rideString.getBytes("UTF-8"));
        // create JDOM document from stream
        Document stationsList = builder.build(stream);

        // get root node to travel the XML file and get each station attributes
        Element rootNode = stationsList.getRootElement();
        List list = rootNode.getChildren("coordinate");

        ArrayList<MapsCoordinates> trajectoryPoints = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {

            Element node = (Element) list.get(i);

            double latitude = Double.parseDouble(node.getChildText("latitude"));
            double longitude = Double.parseDouble(node.getChildText("longitude"));

            trajectoryPoints.add(new MapsCoordinates(latitude,longitude));

        }

//        ArrayList<MapsCoordinates> trajectoryOneCoordinates = new ArrayList<>();
//
//        trajectoryOneCoordinates.add(new MapsCoordinates(38.737651, -9.140756));
//        trajectoryOneCoordinates.add(new MapsCoordinates(38.737480, -9.140690));
//        trajectoryOneCoordinates.add(new MapsCoordinates(38.737288, -9.140613));
//        trajectoryOneCoordinates.add(new MapsCoordinates(38.737104, -9.140613));
//        trajectoryOneCoordinates.add(new MapsCoordinates(38.736940, -9.140506));


        UbiClient ubiClient = clientsList.get(clientName);

        int numberOfRides = ubiClient.getTrajectories().size();

        ubiClient.getTrajectories().put(String.valueOf(numberOfRides+1), trajectoryPoints);


    }


    private static int reserveBike(JSONObject jsondata) throws JSONException {

        // get station name
        String station = jsondata.getString(STATION_NAME);
        // get client name
        String clientName = jsondata.getString(CLIENT_NAME);

        HashMap<Integer, Boolean> clientPoints = bikesPerStation.get(station);

        for (Integer bikeID :
                clientPoints.keySet()) {
            // check if bike is not reserved
            if (!clientPoints.get(bikeID)) {
                // check if user does not hold a bike reserve
                if(clientsList.get(clientName).getHoldingBikeID() == NO_BIKE_ID) {
                    // give bike to client
                    clientsList.get(clientName).setHoldingBikeID(bikeID);
                    // mark bike as reserved
                    clientPoints.put(bikeID, true);
                    return BIKE_RESERVED;
                }
                return BIKE_NOT_RESERVED_HAS_RESERVE;
            }
        }


        return BIKE_NOT_RESERVED_NO_BIKES_AVAILABLE;
    }


    /**
     *  Populate FUNCTIONS
     */

    public static void createTestClient () {

        /**
         *  Client Joao
         */
        UbiClient clientJoao = new UbiClient(TEST_CLIENT_USERNAME);
        clientJoao.setPoints(100);
        ArrayList<String> pointsHistory = new ArrayList<>();
        if (clientJoao.getPointsHistory() == null) {
            clientJoao.setPointsHistory(pointsHistory);
        } else {
            pointsHistory = clientJoao.getPointsHistory();
        }

        pointsHistory.add("received 50 points from joana");
        pointsHistory.add("received 50 points from joana");

        clientJoao.setPointsHistory(pointsHistory);
        clientJoao.setPassword(TEST_CLIENT_PASSWORD);
        clientsList.put(TEST_CLIENT_USERNAME, clientJoao);


        // client joana
        UbiClient clientJoana= new UbiClient(TEST_CLIENT_USERNAME_2);
        clientJoana.setPoints(100);
        pointsHistory = new ArrayList<>();
        if (clientJoana.getPointsHistory() == null) {
            clientJoana.setPointsHistory(pointsHistory);
        } else {
            pointsHistory = clientJoana.getPointsHistory();
        }

        pointsHistory.add("received 50 points from joao");
        pointsHistory.add("received 50 points from joao");

        clientJoana.setPointsHistory(pointsHistory);
        clientJoana.setPassword(TEST_CLIENT_PASSWORD_2);
        clientsList.put(TEST_CLIENT_USERNAME_2, clientJoana);



        HashMap<String, ArrayList<MapsCoordinates>> joanaTrajectories = new HashMap<>();
        ArrayList<MapsCoordinates> trajectoryOneCoordinates = new ArrayList<>();

        trajectoryOneCoordinates.add(new MapsCoordinates(38.737651, -9.140756));
        trajectoryOneCoordinates.add(new MapsCoordinates(38.737480, -9.140690));
        trajectoryOneCoordinates.add(new MapsCoordinates(38.737288, -9.140613));
        trajectoryOneCoordinates.add(new MapsCoordinates(38.737104, -9.140613));
        trajectoryOneCoordinates.add(new MapsCoordinates(38.736940, -9.140506));

        joanaTrajectories.put("1", trajectoryOneCoordinates);

        clientJoana.setTrajectories(joanaTrajectories);


    }


    private static void createBikeStations() {
        // LOCATION -   Lat : Lng

        // Alameda
                // rua alves redol

        HashMap<Integer, Boolean> stationBikes = new HashMap<>();
        String alameda = "Alameda Station";

        double latitudeAlameda = 38.737104;
        double longitudeAlameda= -9.140560;

        bikeStations.put(alameda, new MapsCoordinates(latitudeAlameda,longitudeAlameda));

        // place 10 bikes at the Alameda Station
        for(int i = 1; i <= 10; i++) {
            stationBikes.put(i, false);
        }

        bikesPerStation.put(alameda, stationBikes);



        // Campo Pequeno
                    // avenida antonio serpa

        stationBikes = new HashMap<>();
        String campoPequeno = "Campo Pequeno Station";

        double latitudeCampPeq = 38.743096;
        double longitudeCampPeq = -9.148070;

        bikeStations.put(campoPequeno, new MapsCoordinates(latitudeCampPeq, longitudeCampPeq));


        // place 10 bikes at the Campo Pequeno Station
        for(int i = 11; i <= 20; i++) {
            // todo change to false
            stationBikes.put(i, false);
        }

        bikesPerStation.put(campoPequeno, stationBikes);



        // Picoas
                // rua Tomas Ribeiro

        stationBikes = new HashMap<>();
        String picoas = "Picoas Station";

        double latitudePicoas = 38.731033;
        double longitudePicoas = -9.147309;

        bikeStations.put(picoas, new MapsCoordinates(latitudePicoas, longitudePicoas));


        // place 10 bikes at the Picoas Station
        // todo change to true
        if (ACCEPT_BIKE_RESERVE_PICOAS) {
            for(int i = 21; i <= 30; i++) {
                stationBikes.put(i, false);
            }
        }

        bikesPerStation.put(picoas, stationBikes);


    }



}