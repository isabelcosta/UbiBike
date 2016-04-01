package pt.ulisboa.tecnico.cmu.ubibike;

public final class DummyData {

    private static String[] trajectories =
            {       "#1 - 34 min - 4 km",
                    "#2 - 35 min - 3 km",
                    "#3 - 34 min - 3.1 km",
                    "#4 - 34 min - 10 km",
                    "#5 - 10 min - 4 km",
                    "#6 - 23 min - 6 km",
                    "#7 - 36 min - 21 km",
                    "#8 - 36 min - 21 km",
                    "#9 - 36 min - 21 km",
                    "#10 - 36 min - 21 km"
            };


    private static String[] peers =
            {       "Person 1",
                    "Person 2",
                    "Person 3",
                    "Person 4",
                    "Person 5",
                    "Person 6",
                    "Person 7",
                    "Person 8",
                    "Person 9",
                    "Person 10"
            };

    public static String[] getTrajectories() {
            return trajectories;
    }
    public static String[] getPeers() {
        return peers;
    }
}
