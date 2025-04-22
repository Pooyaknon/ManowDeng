import java.net.*;
import java.util.*;

public class VoiceServer {
    private static final int PORT = 6000;
    private static final Set<SocketAddress> clients = new HashSet<>();

    public static void main(String[] args) {
        byte[] buffer = new byte[1024];

        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Voice Server started on port " + PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);

                SocketAddress sender = packet.getSocketAddress();
                clients.add(sender);

                for (SocketAddress client : clients) {
                    // ส่งให้ client คนอื่น ยกเว้นคนที่ส่งมา
                    if (!client.equals(sender)) {
                        DatagramPacket sendPacket = new DatagramPacket(packet.getData(), packet.getLength(), ((InetSocketAddress) client).getAddress(), ((InetSocketAddress) client).getPort());
                        serverSocket.send(sendPacket);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
