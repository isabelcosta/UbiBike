package pt.ulisboa.tecnico.cmu.ubibike;

public final class DummyData {

    private static String[] trajectories =
            {       "#1 - 34 min - 4 km",
                    "#2 - 35 min - 3 km",
                    "#3 - 34 min - 3.1 km",
                    "#4 - 34 min - 10 km",
                    "#5 - 10 min - 4 km",
                    "#6 - 23 min - 6 km",
                    "#7 - 36 min - 21 km"
            };

    public static String[] getTrajectories() {
            return trajectories;
    }
}
