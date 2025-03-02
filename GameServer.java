import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static Map<String, Integer> scores = new HashMap<>();
    private static List<PrintWriter> clients = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000, 50, InetAddress.getByName("0.0.0.0"))) {
            System.out.println("Server started...");
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String playerName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                clients.add(out);

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("NAME:")) {
                        playerName = input.substring(5);
                    } else if (input.startsWith("SCORE:")) {
                        String[] parts = input.split(":");
                        String player = parts[1];
                        int playerScore = Integer.parseInt(parts[2]);

                        scores.put(player, playerScore);
                    } else if (input.equals("LEADERBOARD")) {
                        sendLeaderboard(out);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                clients.remove(out);
            }
        }

        private static void sendLeaderboard(PrintWriter client) {
            List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(scores.entrySet());
            sortedScores.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
            StringBuilder leaderboard = new StringBuilder("LEADERBOARD:");
            for (Map.Entry<String, Integer> entry : sortedScores) {
                leaderboard.append(entry.getKey()).append(": ").append(entry.getValue()).append(";");
            }
        
            client.println(leaderboard);
        }
    }
}