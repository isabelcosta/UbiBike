package pt.ulisboa.tecnico.cmu.ubibike.Server;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import static com.ubibike.Constants.*;


public class UbiClient {

	/**
	*
	* 		Atributes
 	*
 	*
	* */

	private String _name;
	private String _password;

	private int _points;
	private ArrayList<String> _pointsHistory;

	private HashMap<String, ArrayList<MapsCoordinates>> _trajectories;

	private int _holdingBikeID;


	/**
	 *
	 *  Contructor
	 *
	 * @param name
	 */
	public UbiClient(String name) {
		_name = name;
		setTrajectories(new HashMap<String, ArrayList<MapsCoordinates>>());
		_holdingBikeID = NO_BIKE_ID;
	}




	public String getName() {
		return _name;
	}
	
	public int getPoints() {
		return _points;
	}
	public void setPoints(int points) {
		_points = points;
	}

	public HashMap<String, ArrayList<MapsCoordinates>> getTrajectories() {
		return _trajectories;
	}

	public void setTrajectories(HashMap<String, ArrayList<MapsCoordinates>> trajectories) {
		_trajectories = trajectories;
	}

	public int getHoldingBikeID() {
		return _holdingBikeID;
	}

	public void setHoldingBikeID(int holdingBikeID) {
		_holdingBikeID = holdingBikeID;
	}

	public ArrayList<String> getPointsHistory() {
		return _pointsHistory;
	}

	public void setPointsHistory(ArrayList<String> pointsHistory) {
		_pointsHistory = pointsHistory;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		_password = password;
	}
}
