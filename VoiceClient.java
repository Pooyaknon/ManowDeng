import java.net.*;
import javax.sound.sampled.*;

public class VoiceClient {
    private DatagramSocket socket;
    private boolean running = false;

    public VoiceClient(Socket gameSocket) {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void startVoice() {
        running = true;
        new Thread(this::captureAndSendAudio).start();
    }

    public void stopVoice() {
        running = false;
    }

    private void captureAndSendAudio() {
        try {
            AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            byte[] buffer = new byte[1024];
            while (running) {
                microphone.read(buffer, 0, buffer.length);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("localhost"), 55555);
                socket.send(packet);
            }

            microphone.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
