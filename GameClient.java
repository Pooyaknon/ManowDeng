import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GameClient extends JPanel implements KeyListener, Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int paddleX = 200;
    private final int paddleWidth = 60;
    private int ballX = 250, ballY = 250, ballDX = 2, ballDY = 2;
    private boolean gameStarted = false;
    private boolean paused = false;
    private int score = 0;
    private int lives = 3;
    private String playerName;

    private List<Rectangle> blocks = new ArrayList<>();
    private boolean micOn = false;
    private Thread voiceThread;
    private VoiceClient voiceClient;
    private JButton restartButton;

    public GameClient(String serverAddress) {
        try {
            socket = new Socket(serverAddress, 5000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        playerName = JOptionPane.showInputDialog("Enter your name:");
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }
        out.println("NAME:" + playerName);

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                blocks.add(new Rectangle(50 + i * 80, 50 + j * 30, 60, 20));
            }
        }

        voiceClient = new VoiceClient(socket);
        new Thread(this).start();
        new Thread(this::listenToServer).start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.PINK);
        g.fillRect(paddleX, 450, paddleWidth, 10);

        g.setColor(Color.GREEN);
        g.fillOval(ballX, ballY, 10, 10);

        g.setColor(Color.CYAN);
        for (Rectangle block : blocks) {
            g.fillRect(block.x, block.y, block.width, block.height);
        }

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 20, 40);

        g.setColor(Color.RED);
        for (int i = 0; i < lives; i++) {
            g.fillOval(20 + (i * 20), 10, 15, 15);
        }

        g.setColor(Color.YELLOW);
        g.drawString(playerName, getWidth() / 2 - g.getFontMetrics().stringWidth(playerName) / 2, 20);

        if (lives == 0) {
            g.setColor(Color.RED);
            g.drawString("GAME OVER", 220, 250);

            SwingUtilities.invokeLater(() -> {
                if (restartButton != null) {
                    restartButton.setEnabled(true);
                }
            });
        }
    }

    public void run() {
        while (true) {
            try {
                if (gameStarted && lives > 0 && !paused) {
                    ballX += ballDX;
                    ballY += ballDY;

                    if (ballX <= 0 || ballX >= 490) ballDX = -ballDX;
                    if (ballY <= 0) ballDY = -ballDY;

                    if (ballY + 10 >= 440 && ballX + 10 >= paddleX && ballX <= paddleX + paddleWidth) {
                        ballDY = -ballDY;
                        if (ballX < paddleX + 10) {
                            ballDX = -Math.abs(ballDX);
                        } else if (ballX > paddleX + paddleWidth - 10) {
                            ballDX = Math.abs(ballDX);
                        }
                    }

                    for (int i = 0; i < blocks.size(); i++) {
                        Rectangle block = blocks.get(i);
                        if (new Rectangle(ballX, ballY, 5, 10).intersects(block)) {
                            blocks.remove(i);
                            ballDY = -ballDY;
                            score += 1;
                            break;
                        }
                    }

                    if (ballY >= 500) {
                        lives--;
                        gameStarted = false;
                        out.println("SCORE:" + playerName + ":" + score);
                        resetBall();
                    }
                    out.println(ballX + "," + ballY);

                    if (blocks.isEmpty()) {
                        resetBlocks();
                    }
                }
                repaint();
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public void resetBall() {
        ballX = 250;
        ballY = 250;
        ballDX = 2;
        ballDY = 2;
    }

    public void restartGame() {
        lives = 3;
        score = 0;
        resetBlocks();
        resetBall();
        gameStarted = true;
        if (restartButton != null) restartButton.setEnabled(false);
        out.println("START");
        requestFocusInWindow();
    }

    public void togglePause(JToggleButton button) {
        paused = !paused;
        button.setText(paused ? "▶ Resume" : "⏸ Pause");
        requestFocusInWindow();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            paddleX = Math.max(paddleX - 20, 0);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paddleX = Math.min(paddleX + 20, getWidth() - paddleWidth);
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("ManowDeng");
        GameClient client = new GameClient("169.254.210.193");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(client, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JToggleButton micButton = new JToggleButton("🎤 Mic OFF");
        JButton leaderboardButton = new JButton("📊 Show Leaderboard");
        JButton startButton = new JButton("Start");
        client.restartButton = new JButton("🔁 Restart");
        client.restartButton.setEnabled(false);
        JToggleButton pauseButton = new JToggleButton("⏸ Pause");

        micButton.addActionListener(e -> client.toggleMic(micButton));
        leaderboardButton.addActionListener(e -> client.showLeaderboard());
        startButton.addActionListener(e -> {
            synchronized (client) {
                client.gameStarted = true;
                client.resetBall();
                client.out.println("START");
                client.requestFocusInWindow();
            }
        });
        client.restartButton.addActionListener(e -> client.restartGame());
        pauseButton.addActionListener(e -> client.togglePause(pauseButton));

        buttonPanel.add(startButton);
        buttonPanel.add(client.restartButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(micButton);
        buttonPanel.add(leaderboardButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private void toggleMic(JToggleButton button) {
        micOn = !micOn;
        if (micOn) {
            button.setText("🎤 Mic ON");
            voiceThread = new Thread(() -> voiceClient.startVoice());
            voiceThread.start();
        } else {
            button.setText("🎤 Mic OFF");
            voiceClient.stopVoice();
        }
    }

    private void showLeaderboard() {
        out.println("LEADERBOARD");
    }

    private void listenToServer() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("LEADERBOARD:")) {
                    String leaderboardData = message.substring(12).replace(";", "\n");
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, leaderboardData, "Leaderboard", JOptionPane.INFORMATION_MESSAGE));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetBlocks() {
        blocks.clear();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                blocks.add(new Rectangle(40 + i * 60, 50 + j * 25, 50, 20));
            }
        }
    }
}