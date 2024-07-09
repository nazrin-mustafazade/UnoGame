package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the login frame of the application.
 * It includes methods for initializing the components of the frame, loading user data from a file,
 * logging in a user, registering a user, and saving user data to a file.
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private Map<String, String> users;
    private Map<String, UserStats> userStatsMap;
    private final File userDataFile = new File("users.txt");

    /**
     * Constructor for the LoginFrame class.
     * Sets the title, size, location, default close operation, and resizability of the frame.
     * Initializes the components and loads user data.
     */
    public LoginFrame() {
        setTitle("Uno Game Login");
        setSize(350, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        initComponents();
        loadUserData();  // Load user data at startup
    }

    /**
     * This method initializes the components of the frame.
     * It sets up the layout, background color, and components such as labels, text fields, and buttons.
     */
    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 224)); // Cream background color
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(51, 153, 255));
        loginButton.setForeground(Color.BLACK);
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(loginButton, gbc);
        loginButton.addActionListener(this::loginUser);

        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(255, 153, 0));
        registerButton.setForeground(Color.BLACK);
        gbc.gridy = 3;
        panel.add(registerButton, gbc);
        registerButton.addActionListener(this::registerUser);

        add(panel);
    }

    /**
     * This method loads the user data from a file.
     * It reads the file line by line and splits each line into parts.
     * Each part is then used to create a UserStats object and map the username to the password and the UserStats object.
     */
    private void loadUserData() {
        users = new HashMap<>();
        userStatsMap = new HashMap<>();
        if (!userDataFile.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    UserStats stats = new UserStats(
                            Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]));
                    users.put(username, password);
                    userStatsMap.put(username, stats);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load user data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * This method is called when the login button is clicked.
     * It checks if the entered username and password match the stored data.
     * If they match, it displays a message and opens the main menu.
     * If they don't match, it displays an error message.
     */
    private void loginUser(ActionEvent e) {
        String username = usernameField.getText().strip();
        String password = new String(passwordField.getPassword()).strip();

        if (users.containsKey(username) && users.get(username).equals(password)) {
            JOptionPane.showMessageDialog(this, "Login successful", "Welcome", JOptionPane.INFORMATION_MESSAGE);
            MainMenuFrame mainMenu = new MainMenuFrame(users, userStatsMap, username);
            mainMenu.setVisible(true);
            mainMenu.setLocationRelativeTo(null); // Center the MainMenuFrame
            this.dispose(); // Close the login frame
        }
         else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called when the register button is clicked.
     * It checks if the entered username already exists.
     * If it exists, it displays an error message.
     * If it doesn't exist, it adds the new user to the data and displays a success message.
     */
    private void registerUser(ActionEvent e) {
        String username = usernameField.getText().strip();
        String password = new String(passwordField.getPassword()).strip();

        if (users.containsKey(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists", "Registration Error", JOptionPane.ERROR_MESSAGE);
        } else {
            users.put(username, password);
            userStatsMap.put(username, new UserStats(0, 0, 0));
            saveUserData();
            JOptionPane.showMessageDialog(this, "Registration successful", "Registration", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * This method saves the user data to a file.
     * It writes the username, password, and UserStats data for each user to the file.
     */
    private void saveUserData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(userDataFile))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                UserStats stats = userStatsMap.get(entry.getKey());
                writer.write(entry.getKey() + "," + entry.getValue() + "," +
                        stats.getGamesPlayed() + "," + stats.getWins() + "," + stats.getTotalScore() + "\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save user data", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * This class represents the statistics of a user.
     * It includes fields for the number of games played, the number of wins, and the total score.
     * It also includes getter and setter methods for these fields.
     */
    public static class UserStats {
        private int gamesPlayed;
        private int wins;
        private int totalScore;

        /**
         * Constructor for the UserStats class.
         * Initializes the gamesPlayed, wins, and totalScore fields with the provided values.
         */
        public UserStats(int gamesPlayed, int wins, int totalScore) {
            this.gamesPlayed = gamesPlayed;
            this.wins = wins;
            this.totalScore = totalScore;
        }
        public int getGamesPlayed() {
            return gamesPlayed;
        }

        public void setGamesPlayed(int gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }

        public int getWins() {
            return wins;
        }

        public void setWins(int wins) {
            this.wins = wins;
        }

        public int getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(int totalScore) {
            this.totalScore = totalScore;
        }

    }

}
