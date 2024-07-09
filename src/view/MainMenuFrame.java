package view;

import model.GameEventListener;
import model.GameSession;
import model.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;


/**
 * This class represents the main menu frame of the game.
 * It includes methods for setting up the leaderboard and control panels, loading user statistics, and starting a new game.
 */
public class MainMenuFrame extends JFrame {
    private Map<String, String> users; // User data passed from LoginFrame
    private String currentUser; // To store the currently logged in user
    private List<Player> players;
    private GameEventListener listener;




    private Map<String, LoginFrame.UserStats> userStatsMap = new HashMap<>();
    private final File userDataFile = new File("users.txt");

    /**
     * Default constructor for the MainMenuFrame class.
     * Remember you will use it when game will end, and you will switch to the main menu.
     * Other one is for the first time when you will start the game.
     */
    public MainMenuFrame(){
        // default constructor

    }

    /**
     * Constructor for the MainMenuFrame class.
     * Initializes the users, userStatsMap, and currentUser.
     * Sets up the frame and its components.
     *
     * @param users The users data.
     * @param userStats The user statistics.
     * @param currentUser The currently logged in user.
     */
    public MainMenuFrame(Map<String, String> users, Map<String, LoginFrame.UserStats> userStats, String currentUser) {
        this.users = users;
        this.userStatsMap = userStats;
        this.currentUser = currentUser; // Assume this is set from the login process
        loadUserStats();


        setTitle("Uno Game - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Background setup
        JLabel background = new JLabel(scaleImageIcon("/resources/background.jpg", getWidth(), getHeight()));
        background.setLayout(new BorderLayout());
        add(background);

        // Leaderboard panel
        JPanel leaderboardPanel = setupLeaderboardPanel();
        background.add(leaderboardPanel, BorderLayout.WEST);

        // Control panel setup
        JPanel controlPanel = setupControlPanel();
        background.add(controlPanel, BorderLayout.CENTER);

        // Ensure components resize with the frame
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                background.setIcon(scaleImageIcon("/resources/background.jpg", getWidth(), getHeight()));
            }
        });
    }

    /**
     * This method loads user statistics from a file.
     */
    private void loadUserStats() {
        // Check if the user data file exists
        if (!userDataFile.exists()) {
            return;
        }
        // Try to read the user data file
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFile))) {
            String line;
            // Read each line of the file
            while ((line = reader.readLine()) != null) {
                // Split the line into parts
                String[] parts = line.split(",");
                // Check if the line has the correct number of parts
                if (parts.length == 5) {
                    // Parse the user stats from the line
                    String username = parts[0];
                    int gamesPlayed = Integer.parseInt(parts[2]);
                    int wins = Integer.parseInt(parts[3]);
                    int totalScore = Integer.parseInt(parts[4]);
                    // Add the user stats to the map
                    userStatsMap.put(username, new LoginFrame.UserStats(gamesPlayed, wins, totalScore));
                }
            }
        } catch (IOException e) {
            // Show an error message if the user data file could not be read
            JOptionPane.showMessageDialog(this, "Failed to load user statistics", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method sets up the leaderboard panel.
     *
     * @return The leaderboard panel.
     */
    private JPanel setupLeaderboardPanel() {
        // Create a new JPanel with BorderLayout
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false); // Make the panel transparent

        // Create a title label for the leaderboard
        JLabel title = new JLabel("Leaderboard", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24)); // Set the font of the title
        title.setForeground(Color.WHITE); // Set the color of the title
        panel.add(title, BorderLayout.NORTH); // Add the title to the top of the panel

        // Create a list model to hold the leaderboard data
        DefaultListModel<String> model = new DefaultListModel<>();
        // Populate the list model with user stats
        userStatsMap.forEach((username, stats) -> model.addElement(username + " - Games: " + stats.getGamesPlayed() +
                ", Wins: " + stats.getWins() + ", Score: " + stats.getTotalScore()));
        // Create a JList using the list model
        JList<String> list = new JList<>(model);
        list.setOpaque(false); // Make the list transparent
        list.setForeground(Color.WHITE); // Set the color of the list items
        list.setCellRenderer(new LeaderboardCellRenderer()); // Set the cell renderer for the list

        // Add a mouse listener to the list to handle click events
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Show user stats when a user is clicked in the leaderboard
                if (e.getClickCount() == 1) {
                    String selectedUser = list.getSelectedValue().split(" - ")[0];
                    LoginFrame.UserStats stats = userStatsMap.get(selectedUser);
                    if (stats != null) {
                        float winLossRatio = stats.getWins() / (float) (stats.getGamesPlayed() - stats.getWins());
                        float averageScore = stats.getGamesPlayed() > 0 ? stats.getTotalScore() / (float) stats.getGamesPlayed() : 0;
                        JOptionPane.showMessageDialog(MainMenuFrame.this,
                                "Statistics for " + selectedUser + ":\n" +
                                        "Total Games Played: " + stats.getGamesPlayed() + "\n" +
                                        "Total Wins: " + stats.getWins() + "\n" +
                                        "Total Losses: " + (stats.getGamesPlayed() - stats.getWins()) + "\n" +
                                        "Total Points: " + stats.getTotalScore() + "\n" +
                                        "Average Score: " + averageScore + "\n" +
                                        "Win/Loss Ratio: " + String.format("%.2f", winLossRatio),
                                "User Statistics", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        // Create a scroll pane for the list and make it transparent
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        // Add the scroll pane to the center of the panel
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel; // Return the fully constructed panel
    }
    private JPanel setupControlPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setOpaque(false);

        JButton newGameButton = createImageButton("/resources/new.png", 150, 150);
        JButton continueGameButton = createImageButton("/resources/continue.png", 150, 150);
        JButton exitButton = createImageButton("/resources/exit.png", 150, 150);

        panel.add(newGameButton);
        panel.add(continueGameButton);
        panel.add(exitButton);
        continueGameButton.addActionListener(this::displaySavedGames);

        newGameButton.addActionListener(e -> startNewGame());
        exitButton.addActionListener(e -> System.exit(0));

        return panel;
    }

    /**
     * This method creates an image button.
     *
     * @param imagePath The path of the image.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return The image button.
     */
    private JButton createImageButton(String imagePath, int width, int height) {
        ImageIcon icon = scaleImageIcon(imagePath, width, height);
        JButton button = new JButton(icon);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        return button;
    }

    /**
     * This method scales an ImageIcon.
     *
     * @param path The path of the image.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return The scaled ImageIcon.
     */
    private ImageIcon scaleImageIcon(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    class LeaderboardCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setOpaque(true);
            label.setForeground(Color.WHITE);
            label.setBackground(isSelected ? Color.DARK_GRAY : new Color(0, 0, 0, 0));
            return label;
        }
    }

    private void startNewGame() {
        // Ask the user for the number of players
        int numPlayers = promptNumberOfPlayers();

        if (numPlayers < 2 || numPlayers > 10) {
            JOptionPane.showMessageDialog(this, "Number of players must be between 2 and 10.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create players including bots
        List<Player> players = createPlayers(numPlayers, currentUser);

        // Create a new GameInterface frame and dispose of the current MainMenuFrame
        GameInterface gameInterface = new GameInterface(players);
        gameInterface.setVisible(true);
        dispose(); // Dispose of the current MainMenuFrame
    }

    private int promptNumberOfPlayers() {
        String input = JOptionPane.showInputDialog(this, "Enter the number of players (2-10):");
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // Indicates invalid input
        }
    }

    /**
     * This method creates players for the game.
     *
     * @param numPlayers The number of players.
     * @param username The username of the user player.
     * @return The list of players.
     */
    private List<Player> createPlayers(int numPlayers, String username) {
        List<Player> players = new ArrayList<>();

        // Add user player
        players.add(new Player(username, false)); // Assuming the second parameter isBot

        // Add bot players
        for (int i = 1; i < numPlayers; i++) {
            String botName = "Bot " + i;
            players.add(new Player(botName, true)); // Assuming the second parameter isBot
            System.out.println("Created player " + botName + " as a bot.: " + players.get(i).isBot());
        }

        return players;
    }

    private void displaySavedGames(ActionEvent event) {
        File folder = new File("saves");
        System.out.println("Checking for saved games in: " + folder.getAbsolutePath()); // Debug: Print the absolute path

        if (!folder.exists() || !folder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "No saved games found.", "Load Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] listOfFiles = folder.listFiles((dir, name) -> name.startsWith("UnoSave_") && name.endsWith(".txt"));
        if (listOfFiles == null || listOfFiles.length == 0) {
            JOptionPane.showMessageDialog(this, "No saved games found.", "Load Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("Found " + listOfFiles.length + " saved games."); // Debug: Print the number of files found


        DefaultListModel<String> model = new DefaultListModel<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                // Display filename without extension for user-friendliness
                String displayName = file.getName().substring(0, file.getName().length() - 4);
                model.addElement(displayName);
                System.out.println("Added game to list: " + displayName); // Debug: Print each file added to the list

            }
        }

        JScrollPane scrollPane = getjScrollPane(model);

        scrollPane.setPreferredSize(new Dimension(200, 300));

        // Create a new JFrame or use a dedicated panel within the existing layout for displaying the list
        JFrame frame = new JFrame("Select a Game to Continue");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(this);  // Position relative to the main frame
        frame.setVisible(true);
    }

    private JScrollPane getjScrollPane(DefaultListModel<String> model) {
        JList<String> list = new JList<>(model);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !list.isSelectionEmpty()) {
                String selectedGame = list.getSelectedValue();
                System.out.println("Selected game to load: " + selectedGame); // Debug: Print selected game
                // Ensure that the file extension is included when attempting to load the game
                loadGame("saves/" + selectedGame + ".txt");
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(200, 300)); // Set preferred size for scroll pane
        return scrollPane;
    }




    /**
     * Loads a game from a file specified by the filePath.
     * The method first checks if the file exists. If not, it shows an error message and returns.
     * If the file exists, it reads the file and extracts the username of the saved game.
     * It then checks if the current user is the same as the saved user. If not, it shows an error message and returns.
     * If the current user is the same as the saved user, it continues loading the game.
     * It creates a new GameSession and loads the game from the file.
     * If no players are loaded from the save file, it shows an error message and returns.
     * If players are loaded successfully, it creates a new GameInterface and sets it visible.
     * Finally, it disposes the MainMenuFrame.
     *
     * @param filePath The path of the file to load the game from.
     * @throws IOException If an I/O error occurs when reading from the file.
     */
    private void loadGame(String filePath) {
        File gameFile = new File(filePath);
        if (!gameFile.exists()) {
            JOptionPane.showMessageDialog(null, "Game file not found.", "Load Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(gameFile))) {
            // Skip first three lines as they are not needed for this check
            for (int i = 0; i < 3; i++) {
                reader.readLine();
            }
            // Read the fourth line to get the username
            String line = reader.readLine();
            if (line == null) {
                JOptionPane.showMessageDialog(null, "Game file is corrupted.", "Load Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Extract username from the line (assuming the format is 'username:')
            String[] parts = line.split(":");
            if (parts.length < 2) {
                JOptionPane.showMessageDialog(null, "Game file is corrupted.", "Load Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String savedUserName = parts[0];

            // Check if the currentUser is the same as the saved user
            if (!currentUser.equals(savedUserName)) {
                JOptionPane.showMessageDialog(null, "This saved game belongs to another user.", "Unauthorized", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // If everything is fine, continue loading the game
            GameSession gameSession = new GameSession();
            gameSession.loadGame(filePath); // load the file
            if (gameSession.getPlayers().isEmpty()) {
                JOptionPane.showMessageDialog(null, "No players loaded from the save file.", "Load Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            GameInterface gameInterface = new GameInterface(gameSession.getPlayers());
            gameInterface.setVisible(true);
            this.dispose(); // Close the MainMenuFrame
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading the game file: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}
