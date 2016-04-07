package pt.ulisboa.tecnico.cmu.ubibike.Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonParser;

/**
 * This is a simple server application. This server receive a string message
 * from the Android mobile phone and show it on the console.
 * Author by Lak J Comspace
 */
public class SimpleTextServer {

	private static ServerSocket serverSocket;
	private static Socket clientSocket;
	private static InputStreamReader inputStreamReader;
	private static BufferedReader bufferedReader;
	private static String message;

	private static HashMap<String, UbiClient> clientsList = new HashMap<>();
	
	
	
	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(4444); // Server socket

		} catch (IOException e) {
			System.out.println("Could not listen on port: 4444");
		}

		System.out.println("Server started. Listening to the port 4444");
		UbiClient client1 = new UbiClient("v");
		client1.setPoints(100);
		ArrayList<String> pointsHistory = new ArrayList<>();
		if (client1.getPointsHistory() == null) {
			client1.setPointsHistory(pointsHistory);
		} else {
			pointsHistory = client1.getPointsHistory();
		}
		
		pointsHistory.add("received 50 points from michael");
		pointsHistory.add("received 50 points from john");
		
		client1.setPointsHistory(pointsHistory);
		
		clientsList.put("v", client1);
		
		while (true) {
			try {
				/*
				 * client Socket and DataStreams
				 * */
				
                clientSocket = serverSocket.accept();
                
				DataInputStream dataInputStream = new DataInputStream(
                        clientSocket.getInputStream()); // used to get messages FROM the CLIENT

                DataOutputStream dataOutputStream = new DataOutputStream(
                        clientSocket.getOutputStream()); // use to send messages TO the CLIENT
                /*
                 *  -----------------------------------
                 * */
                
                // message from the client
				message = dataInputStream.readUTF();
				
				// create the json object from the String
				final JSONObject jsondata;
                jsondata = new JSONObject(message);
                
                // get the type of message to know what the client wants
                String type = jsondata.getString("type");
				System.out.println("Type: " + type);
				
				
				if (type.equals
						("register client")) 
				{
						registerClient(jsondata);
						dataOutputStream.writeUTF("Cliente adicionado");
				}
				else if (type.equals
							("get clients")) 
				{
					String clients = getClients();
	                dataOutputStream.writeUTF(clients);	
				}
				else if (type.equals
							("get points history")) 
				{
					JSONObject json = getPointsHistory(jsondata);
	                dataOutputStream.writeUTF(json.toString());	
				}
				else if (type.equals
						("greating")) 
				{
					String greating = jsondata.getString("greating");
					System.out.println("Greating " + greating);
					String messageToClient = "Greetings from the Server";
					dataOutputStream.writeUTF(messageToClient);
				}
				else if (type.equals
						("add points")) 
				{
					// get client points
					String points = jsondata.getString("client points");
					System.out.println("Points " + points);
					
					//get client name
					String clientName = jsondata.getString("client name");
					
					// get clients current real points
					int clientPoints = clientsList.get(clientName).getPoints();
					
					// update clients current real points added with the received points
					clientsList.get(clientName).setPoints(clientPoints + Integer.parseInt(jsondata.getString("client points")));
					
					// get client points history
					ArrayList<String> pointsOrigin = clientsList.get(clientName).getPointsHistory();
					
					// add new points to the history and update on server
					pointsOrigin.add(jsondata.getString("points origin"));
					clientsList.get(clientName).setPointsHistory(pointsHistory);
					
					// send message to clients confirming that the points were added
					String messageToClient = "Points Added";
					dataOutputStream.writeUTF(messageToClient);
				}
				else if (type.equals
						("get points")) 
				{
					// get points 
					String clientName = jsondata.getString("client name");
					int clientPoints = clientsList.get(clientName).getPoints();
					
					
					dataOutputStream.writeUTF(clientPoints+"");
				}
                
				clientSocket.close();

			} catch (IOException ex) {
				System.out.println("Problem in message reading");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private static void registerClient(JSONObject jsondata) throws JSONException {
		// get client name from jsondata - "client name"
		String clientName = jsondata.getString("client name");
		
		// create a new UbiClient with clientName
		UbiClient client = new UbiClient(clientName);
		
		// set starting point to 100
		// TODO - FIXME - just for testing purposes
		client.setPoints(100);
		
		// add the client to the HashMap<String,UbiClient> clientsList
		clientsList.put(clientName,client);
		
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
		
		String clientName = jsondata.getString("client name");
		int clientPoints = clientsList.get(clientName).getPoints();
		ArrayList<String> pointsOrigin = clientsList.get(clientName).getPointsHistory();
		
		
		System.out.println("name " + clientName);
		System.out.println("points " + clientPoints);
		
		JSONObject json = new JSONObject();
		
        json.put("type", "give points");
        json.put("points", clientPoints);

        
        StringBuilder sb = new StringBuilder();
		for (String s : pointsOrigin)
		{
			sb.append(s);
			System.out.println(s);
		    sb.append("\t");
		}
		
		json.put("points history", sb.toString());
        
		
		
        return json;
		
	}
	
}