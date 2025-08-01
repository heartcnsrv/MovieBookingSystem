package lumiere.server;

import javax.swing.SwingUtilities;

public class ServerMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerView serverView = new ServerView();
            serverView.setVisible(true);
        });
    }
}