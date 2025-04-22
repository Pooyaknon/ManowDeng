import java.net.*;
import javax.sound.sampled.*;

public class VoiceClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int port = 6000;
    private TargetDataLine microphone;
    private boolean running = false;

    public VoiceClient(String ip) {
        try {
            serverAddress = InetAddress.getByName(ip);
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startVoice() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[1024];
            running = true;
            System.out.println("Voice chat started (UDP).");

            while (running) {
                int count = microphone.read(buffer, 0, buffer.length);
                if (count > 0) {
                    DatagramPacket packet = new DatagramPacket(buffer, count, serverAddress, port);
                    socket.send(packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopVoice() {
        running = false;
        if (microphone != null) microphone.close();
        if (socket != null && !socket.isClosed()) socket.close();
        System.out.println("Voice chat stopped.");
    }

    private AudioFormat getAudioFormat() {
        return new AudioFormat(16000.0F, 16, 1, true, false);
    }
}
