import java.net.*;
import javax.sound.sampled.*;

public class VoiceClient {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private final int port = 6000;

    private TargetDataLine microphone;
    private SourceDataLine speakers;

    private Thread captureThread;
    private Thread playbackThread;

    private boolean running = false;

    public VoiceClient(String serverIp) {
        try {
            serverAddress = InetAddress.getByName(serverIp);
            socket = new DatagramSocket();
        } catch (Exception e) {
            System.err.println("Failed to initialize VoiceClient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startVoice() {
        running = true;
        startAudioCapture();
        startAudioPlayback();
        System.out.println("Voice chat started (UDP).");
    }

    public void stopVoice() {
        running = false;

        if (captureThread != null) captureThread.interrupt();
        if (playbackThread != null) playbackThread.interrupt();

        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }
        if (speakers != null) {
            speakers.drain();
            speakers.stop();
            speakers.close();
        }

        if (socket != null && !socket.isClosed()) {
            socket.close();
        }

        System.out.println("Voice chat stopped.");
    }

    private void startAudioCapture() {
        captureThread = new Thread(() -> {
            try {
                AudioFormat format = getAudioFormat();
                microphone = getMicrophoneLine(format);
                microphone.open(format);
                microphone.start();

                byte[] buffer = new byte[1024];
                while (running) {
                    int count = microphone.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        DatagramPacket packet = new DatagramPacket(buffer, count, serverAddress, port);
                        socket.send(packet);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in audio capture: " + e.getMessage());
                e.printStackTrace();
            }
        });
        captureThread.start();
    }

    private void startAudioPlayback() {
        playbackThread = new Thread(() -> {
            try {
                AudioFormat format = getAudioFormat();
                speakers = AudioSystem.getSourceDataLine(format);
                speakers.open(format);
                speakers.start();

                byte[] buffer = new byte[1024];
                while (running) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    speakers.write(packet.getData(), 0, packet.getLength());
                }
            } catch (Exception e) {
                System.err.println("Error in audio playback: " + e.getMessage());
                e.printStackTrace();
            }
        });
        playbackThread.start();
    }

    private TargetDataLine getMicrophoneLine(AudioFormat format) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported.");
        }
        return (TargetDataLine) AudioSystem.getLine(info);
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
