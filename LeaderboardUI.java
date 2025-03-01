import java.awt.*;
import java.util.*;
import javax.swing.*;

public class LeaderboardUI {
    public static void showLeaderboard(Map<String, Integer> scores) {
        JFrame frame = new JFrame("Leaderboard");
        frame.setSize(300, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columns = {"Player", "Score"};
        String[][] data = new String[scores.size()][2];

        int i = 0;
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            data[i][0] = entry.getKey();
            data[i][1] = String.valueOf(entry.getValue());
            i++;
        }

        JTable table = new JTable(data, columns);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
