package pt.ulisboa.tecnico.cmu.ubibike.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class UbiClient {
	private String _name;
	private int _points;
	private ArrayList<String> _pointsHistory;
	private HashMap<String, String> _trajectories;
	private int _holdingBikeID;
	
	public UbiClient(String name) {
		_name = name;
		setTrajectories(new HashMap<String, String>());
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

	public HashMap<String, String> getTrajectories() {
		return _trajectories;
	}

	public void setTrajectories(HashMap<String, String> trajectories) {
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
	
}
