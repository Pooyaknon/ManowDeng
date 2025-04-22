import java.net.*;
import javax.sound.sampled.*;

public class VoiceClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private final int port = 6000;

    private TargetDataLine microphone;
    private boolean running = false;

    public VoiceClient(String serverIp) {
        try {
            serverAddress = InetAddress.getByName(serverIp);
            socket = new DatagramSocket();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startVoice() {
        try {
            AudioFormat format = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Microphone not supported.");
                return;
            }

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

            microphone.stop();
            microphone.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopVoice() {
        running = false;
        try {
            if (microphone != null) {
                microphone.stop();
                microphone.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Voice chat stopped.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }
}