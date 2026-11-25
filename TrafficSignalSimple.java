import java.io.*;
import java.util.*;

public class TrafficSignalSimple {

    // 1. CIRCULAR QUEUE
    static String[] roads = new String[4];
    static int roadCount = 0;
    static int current = 0;

    static void nextInCircle() {
        current = (current + 1) % roadCount;
    }

    // 2. GRAPH
    static ArrayList<ArrayList<Integer>> graph = new ArrayList<>();

    static void buildGraph() {
        graph.clear();
        for (int i = 0; i < roadCount; i++)
            graph.add(new ArrayList<>());

        for (int i = 0; i < roadCount; i++) {
            int next = (i + 1) % roadCount;
            if (next != i) {
                graph.get(i).add(next);
                graph.get(next).add(i);
            }
        }
    }

    static int findRoad(String name) {
        for (int i = 0; i < roadCount; i++)
            if (roads[i].equalsIgnoreCase(name.trim()))
                return i;
        return -1;
    }

    // 3. PRIORITY QUEUE
    static class Emergency {
        int road;
        int priority;
        int order;

        Emergency(int road, int priority, int order) {
            this.road = road;
            this.priority = priority;
            this.order = order;
        }
    }

    static PriorityQueue<Emergency> emergencyQueue =
        new PriorityQueue<>((a, b) -> {
            if (a.priority != b.priority)
                return b.priority - a.priority;
            return a.order - b.order;
        });

    static int arrivalCounter = 0;

    // SIGNAL STATES
    static String[] signal = new String[4];
    static boolean started = false;

    static void setOnlyGreen(int road) {
        for (int i = 0; i < roadCount; i++)
            signal[i] = "RED";

        signal[road] = "GREEN";
        current = road;
    }

    // 4. FILE HANDLING
    static final String FILE_NAME = "traffic_signals.txt";

    static void loadFile() {
        roadCount = 0;

        try {
            File f = new File(FILE_NAME);

            if (f.exists()) {
                Scanner fs = new Scanner(f);

                while (fs.hasNextLine()) {
                    String line = fs.nextLine().trim();

                    if (line.equals("") ||
                        line.equalsIgnoreCase("ROADS"))
                        continue;

                    if (line.toUpperCase().startsWith("CURRENT="))
                        continue;

                    if (roadCount < 4)
                        roads[roadCount++] = line;
                }

                fs.close();
            }
        } catch (Exception e) {
            System.out.println("Could not read file. Using default roads.");
            roadCount = 0;
        }

        if (roadCount == 0) {
            roads = new String[]{"North", "East", "South", "West"};
            roadCount = 4;
        }

        buildGraph();

        for (int i = 0; i < roadCount; i++)
            signal[i] = "RED";
    }

    static void saveFile() {
        try {
            PrintWriter pw =
                new PrintWriter(new FileWriter(FILE_NAME));

            pw.println("ROADS");

            for (int i = 0; i < roadCount; i++)
                pw.println(roads[i]);

            pw.println("CURRENT=" + roads[current]);
            pw.close();

            System.out.println("Data saved in " + FILE_NAME);

        } catch (IOException e) {
            System.out.println("Could not save file.");
        }
    }

    // OPERATIONS
    static void startSimulator() {
        started = true;
        setOnlyGreen(0);
        showStatus();
    }

    static void nextPhase() {
        if (!started) {
            System.out.println("Please start the simulator first.");
            return;
        }

        if (roadCount == 1 && emergencyQueue.isEmpty()) {
            System.out.println("Signal remains in the configured phase.");
            showStatus();
            return;
        }

        System.out.println("Transition: " + roads[current] + " YELLOW");
        signal[current] = "YELLOW";

        System.out.println("Transition: All RED");

        for (int i = 0; i < roadCount; i++)
            signal[i] = "RED";

        if (!emergencyQueue.isEmpty()) {
            Emergency e = emergencyQueue.poll();

            System.out.println("Emergency priority granted.");
            setOnlyGreen(e.road);

        } else {
            nextInCircle();
            setOnlyGreen(current);
        }

        showStatus();
    }

    static void emergencyRequest(String name, int priority) {
        int road = findRoad(name);

        if (road == -1) {
            System.out.println("Invalid road. Request ignored.");
            return;
        }

        emergencyQueue.add(
            new Emergency(road, priority, arrivalCounter++)
        );

        System.out.println(
            "Emergency request received: " +
            roads[road] + " (Priority: " + priority + ")"
        );
    }

    static void showStatus() {
        if (!started) {
            System.out.println("Please start the simulator first.");
            return;
        }

        System.out.println(
            "Signal: " + roads[current] + " " + signal[current]
        );

        String others = "Other roads: ";

        for (int i = 0; i < roadCount; i++) {
            if (i != current)
                others += roads[i] + " " + signal[i] + "  ";
        }

        System.out.println(others);

        System.out.println(
            "Waiting emergency requests: " +
            emergencyQueue.size()
        );
    }

    // MAIN METHOD
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        loadFile();

        int choice = 0;

        while (choice != 5) {

            System.out.println("\nTRAFFIC SIGNAL SIMULATOR");
            System.out.println("1. Start Simulator");
            System.out.println("2. Next Phase");
            System.out.println("3. Emergency Request");
            System.out.println("4. Display Status");
            System.out.println("5. Save and Exit");

            System.out.print("Enter choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                choice = 0;
            }

            if (choice == 1) {
                startSimulator();

            } else if (choice == 2) {
                nextPhase();

            } else if (choice == 3) {

                System.out.print(
                    "Enter road (North/East/South/West): "
                );

                String name = sc.nextLine();

                System.out.print(
                    "Enter priority (1-3, 3 = highest): "
                );

                int p;

                try {
                    p = Integer.parseInt(sc.nextLine().trim());
                } catch (Exception e) {
                    p = 3;
                }

                if (p < 1 || p > 3)
                    p = 3;

                emergencyRequest(name, p);

            } else if (choice == 4) {
                showStatus();

            } else if (choice == 5) {
                saveFile();
                System.out.println("Goodbye!");

            } else {
                System.out.println("Invalid option. Try again.");
            }
        }

        sc.close();
    }
}