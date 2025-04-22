import java.net.*;
import java.util.*;

public class VoiceServer {
    private static final int PORT = 6000;
    private static final List<DatagramSocket> clients = new ArrayList<>();

    public static void main(String[] args) {
        byte[] buffer = new byte[1024];
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Voice Server started on port " + PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                InetSocketAddress sender = new InetSocketAddress(packet.getAddress(), packet.getPort());

                if (!clients.contains(sender)) {
                    clients.add(serverSocket);
                }

                for (DatagramSocket client : clients) {
                    if (!client.equals(sender)) {
                        DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.getLength(), sender.getAddress(), sender.getPort());
                        serverSocket.send(sendPacket);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
