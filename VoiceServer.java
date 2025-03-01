import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class VoiceServer {
    public static void main(String[] args) {
        int port = 55555;
        byte[] buffer = new byte[1024];

        try (DatagramSocket serverSocket = new DatagramSocket(port)) {
            System.out.println("Voice Chat Server Started on Port " + port);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(packet);
                serverSocket.send(packet); // ส่งข้อมูลกลับไปให้ทุก client
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
