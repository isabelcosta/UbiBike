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

	public HashMap<String, String> get_trajDates() {
		return _trajDates;
	}

	public void set_trajDates(HashMap<String, String> _trajDates) {
		this._trajDates = _trajDates;
	}

	private HashMap<String, String> _trajDates;
	private int _holdingBikeID;

    public String getHoldingBikeStation() {
        return holdingBikeStation;
    }

    public void setHoldingBikeStation(String holdingBikeStation) {
        this.holdingBikeStation = holdingBikeStation;
    }

    private String holdingBikeStation;


	/**
	 *
	 *  Contructor
	 *
	 * @param name
	 */
	public UbiClient(String name) {
		_name = name;
		setTrajectories(new HashMap<String, ArrayList<MapsCoordinates>>());
		set_trajDates(new HashMap<String, String>());
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
