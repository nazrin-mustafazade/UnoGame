package view;

import model.GameEventListener;
import model.Player;
import model.Card;
import model.GameSession;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * This class represents the game interface for the Uno game.
 * It implements the GameEventListener interface to handle game events.
 */
public class GameInterface extends JFrame  implements GameEventListener {
    private GameSession gameSession;
    private JPanel playerHandPanel;
    private JLabel discardPileLabel;

    private JLabel directionLabel;
    private JButton drawPileButton;
    private JLabel turnLabel; 
    private final int BOT_DELAY_MS = 3000; // 1 second delay for bot actions
    private Timer botTimer;

    private JPanel statusPanel;
    private JButton declareUnoButton;


    /**
     * Constructor for the GameInterface class.
     * Initializes the game session and the UI.
     *
     * @param players The list of players in the game.
     */
    public GameInterface(List<Player> players) {
        gameSession = new GameSession(players, this);
        initializeUI();
        updateGameState();

        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Cannot initialize game with no players.");
        }
    }


    /**
     * This method initializes the UI for the game interface.
     */
    private void initializeUI() {
        setTitle("Uno Game - Play Session");
        setSize(1200, 800); // Further increased size for better component fitting
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel for discard pile display
        discardPileLabel = new JLabel();
        add(discardPileLabel, BorderLayout.CENTER);

        // Top panel for game controls and information
        JPanel topPanel = new JPanel(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);

        // Button for drawing cards placed on the right
        drawPileButton = new JButton("Draw Card: "+gameSession.getDeck().getUndrawnCards().size() + " cards");
        drawPileButton.addActionListener(e -> drawCard());
        JPanel drawPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        drawPanel.add(drawPileButton);
        topPanel.add(drawPanel, BorderLayout.EAST);

        // Center Panel to include turn and game direction information
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        turnLabel = new JLabel("Turn: ", SwingConstants.CENTER);
        directionLabel = new JLabel("Direction: Clockwise");
        centerPanel.add(turnLabel);
        centerPanel.add(directionLabel);
        topPanel.add(centerPanel, BorderLayout.CENTER);

        // Save game button placed on the left
        JButton saveGameButton = new JButton("Save Game");
        saveGameButton.addActionListener(e -> {
            gameSession.saveGame();
            backToMainMenu();
        });
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        savePanel.add(saveGameButton);
        topPanel.add(savePanel, BorderLayout.WEST);

        // Declare Uno button placed at the top
        declareUnoButton = new JButton("UNO!");
        declareUnoButton.addActionListener(e -> {
            Player currentPlayer = gameSession.getCurrentPlayer();
            if (gameSession.canDeclareUno(currentPlayer)) {
                gameSession.declareUno(currentPlayer);
            } else {
                JOptionPane.showMessageDialog(this, "You can't declare Uno now!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JPanel unoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        unoPanel.add(declareUnoButton);
        topPanel.add(unoPanel, BorderLayout.SOUTH);
        declareUnoButton.setEnabled(true); // Ensure the button is always enabled


        // Panel for showing player hands (if there are human players)
        boolean hasHumanPlayers = gameSession.getPlayers().stream().anyMatch(player -> !player.isBot());
        if (hasHumanPlayers) {
            playerHandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            add(playerHandPanel, BorderLayout.SOUTH);
        }

        // Dedicated status panel to the side for less interference
        statusPanel = new JPanel(new GridLayout(2, 1));
        updateStatusPanel(statusPanel);
        add(statusPanel, BorderLayout.EAST); // Changed from SOUTH to EAST for better visibility and layout
    }


    /**
     * This method updates the status panel to display the current game status.
     * It shows the number of cards in each player's hand and the number of cards in the draw pile.
     * This method should be called whenever the game status changes and the status panel needs to be updated.
     * It is designed to be called on the Event Dispatch Thread (EDT) to ensure thread safety for UI updates.
     */
    private void updateStatusPanel(JPanel statusPanel) {
        statusPanel.removeAll(); // Clear the panel first to refresh it

        // Player card counts
        JPanel playerCountsPanel = new JPanel(new GridLayout(0, 1));
        for (Player player : gameSession.getPlayers()) {
        	if(player.isBot()) {
        		JLabel playerLabel = new JLabel(player.getUsername() + ": " + player.getHand().size() + " cards");
                playerCountsPanel.add(playerLabel);
        	}
            
        }
        statusPanel.add(playerCountsPanel);

        statusPanel.revalidate();
        statusPanel.repaint();
    }


    private void backToMainMenu() {
        MainMenuFrame mainMenu = new MainMenuFrame(); // Pass appropriate user info
        mainMenu.setVisible(true);
        this.dispose();
    }

    private void updateDirectionDisplay(boolean isClockwise) {
        String directionText = isClockwise ? "Clockwise" : "Counter-Clockwise";
        directionLabel.setText("Direction: " + directionText);
    }
    
    /**
     * returns the color in type Color that is String
     *
     * @param String value, name of the color
     */
    
    private static Color getColorFromString(String color) {
        switch (color.toLowerCase()) {
            case "red":
                return Color.RED;
            case "blue":
                return Color.BLUE;
            case "green":
                return Color.GREEN;
            case "yellow":
            	return Color.YELLOW;
            default:
                return Color.BLACK;
        }
    }

    /**
     * Updates the game state in the user interface.
     * This method is responsible for reflecting the current state of the game in the user interface.
     * It should be invoked whenever there is a change in the game state that needs to be reflected in the UI.
     * This method is designed to be called on the Event Dispatch Thread (EDT) to ensure thread safety for UI updates.
     *
     * The method performs the following operations:
     * 1. Updates the turn label to reflect the current player's turn.
     * 2. Updates the discard pile label to show the current card on the discard pile.
     * 3. Updates the player's hand in the UI if the current player is a human player.
     * 4. If the game is over, it displays a game over message.
     */
    private void updateGameState() {
        Player currentPlayer = gameSession.getCurrentPlayer();
        discardPileLabel.setText(gameSession.getCurrentCard().getValue().toString());
        discardPileLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text horizontally
        discardPileLabel.setVerticalAlignment(SwingConstants.CENTER); 
        Dimension preferredSize = new Dimension(50, 100);
        discardPileLabel.setPreferredSize(preferredSize);
        discardPileLabel.setBackground(getColorFromString(gameSession.getCurrentCard().getColor().toString()));
        discardPileLabel.setForeground(Color.WHITE);
        discardPileLabel.setOpaque(true);
        discardPileLabel.setBorder(null);
        add(discardPileLabel, BorderLayout.CENTER);
        updatePlayerHand(currentPlayer);

        if (gameSession.isGameOver()) {
            JOptionPane.showMessageDialog(this, "Game Over! Winner: " + gameSession.getCurrentPlayer().getUsername(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    


    /**
     * Refreshes the display of the player's hand in the user interface.
     * This method is responsible for updating the visual representation of the player's hand in the UI, ensuring it accurately reflects the current state of the player's hand.
     * It is designed to be invoked any time there is a change in the player's hand that needs to be visually updated in the UI.
     * To ensure thread safety for UI updates, this method is intended to be called on the Event Dispatch Thread (EDT).
     * If the player is a bot, this method will not perform any UI updates, as bot hands are not visually represented in the UI.
     * For human players, this method will update the UI to display the current state of the player's hand. It will also attach action listeners to the card buttons in the UI, enabling the human player to play cards by clicking on them.
     * Therefore, for human players, this method should be invoked whenever there is a change in the player's hand that needs to be reflected in the UI.
     *
     * @param player The player whose hand needs to be updated in the UI.
     */
    private void updatePlayerHand(Player player) {
        playerHandPanel.removeAll();
        if (!player.isBot()) {  // Only update hand if it's a human player
            for (Card card : player.getHand()) {
                String cardText = card.getValue().toString();
                JButton cardButton = new JButton(cardText);
                Dimension preferredSize = new Dimension(100, 200);
                cardButton.setPreferredSize(preferredSize);
                Color buttonColor = getColorFromString(card.getColor().toString());
                cardButton.setOpaque(true);
                cardButton.setBackground(buttonColor);
                cardButton.setForeground(Color.WHITE);
                cardButton.setBorder(null);
                
                
                cardButton.addActionListener(e -> {
                    if (gameSession.playCard(card, player)) {
                        updateGameState();
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid move!", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                });
                playerHandPanel.add(cardButton);
            }
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }
    
   

    /**
     * This method is called when the player clicks the "Draw Card" button.
     * It draws a card for the current player and prompts the player to play the card if it is a valid move.
     */
    private void drawCard() {
        Player currentPlayer = gameSession.getCurrentPlayer();
        Card drawnCard = gameSession.drawCardForPlayer(currentPlayer);
        updateGameState();
        if (drawnCard != null && gameSession.isValidPlay(drawnCard)) {
            int response = JOptionPane.showConfirmDialog(this, "You drew " + drawnCard + ". Do you want to play this card?",
                    "Play Drawn Card?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                gameSession.playCard(drawnCard, currentPlayer);
                updateGameState();
            } else {
                gameSession.nextTurn(); // Manually pass the turn if the player decides not to play the drawn card
                updateGameState();
            }
        }
    }

    /**
     * This method is called when a color selection is requested.
     * It prompts the player to choose a color for a wild card.
     *
     * @param player The player who needs to select a color.
     * @param wildCard The wild card that triggered the color selection.
     */
    @Override

    public void onRequestColorSelection(Player player, Card wildCard) {
        // If this is being called, you don't need to invoke SwingUtilities.invokeLater
        // since this method should only be called as a result of GUI interactions which are already on the EDT.
        String[] options = {"Red", "Green", "Blue", "Yellow"};
        int response = JOptionPane.showOptionDialog(null, "Choose a color:", "Wild Card",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if (response >= 0) {
            Card.Color chosenColor = Card.Color.valueOf(options[response].toUpperCase());
            gameSession.setCurrentColor(chosenColor);
            gameSession.setCurrentCard(new Card(Card.Color.WILD, wildCard.getValue())); // Reflect this in the current card.
            updateGameState();
        }
    }

    /**
     * Invoked when the turn changes in the game session.
     * This method is responsible for updating the game state based on the current player's turn.
     * It distinguishes between human and bot players and updates the game state accordingly.
     * For a human player, it updates the game state to reflect the human player's turn.
     * For a bot player, it initiates the bot's turn and updates the game state after the bot's turn.
     * This method should be invoked whenever the turn changes in the game session.
     *
     * @param currentPlayer The player who has the current turn.
     */
    @Override
    public void onTurnChanged(Player currentPlayer) {
        if (currentPlayer == null) {
            // Handle the end of the game
            JOptionPane.showMessageDialog(this, "Game over!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            onGameOver(); // Nergiz check this if bug occurs
            return;
        }
        if (currentPlayer.isBot()) {
            updateGameStateForBot(currentPlayer);
        } else {
            updateGameStateForHuman(currentPlayer);
        }
    }


    /**
     * Updates the game state for a human player.
     * This method is responsible for updating the game state in the user interface for a human player.
     * It should be called when it is the human player's turn to play.
     * The method updates the UI to reflect the current state of the game and prompts the human player to take action.
     *
     * @param currentPlayer The human player whose turn it is.
     */
    private void updateGameStateForHuman(Player currentPlayer) {
        // This would enable UI components and potentially highlight the current player's turn
        updateGameState();  // Refresh the UI to show the current state
    }

    /**
     * Updates the game state for a bot player.
     * This method is responsible for updating the game state in the user interface for a bot player.
     * It should be called when it is the bot player's turn to play.
     * The method updates the UI to reflect the current state of the game and executes the bot's turn.
     *
     * @param botPlayer The bot player whose turn it is.
     */
    private void updateGameStateForBot(Player botPlayer) {
        // Update the UI without updating the player's hand
        Player currentPlayer = gameSession.getCurrentPlayer();
        turnLabel.setText("Turn: " + currentPlayer.getUsername());
        discardPileLabel.setText("Discard Pile: " + gameSession.getCurrentCard());
        declareUnoButton.setEnabled(gameSession.canDeclareUno(currentPlayer));



        // critical part of project

        // Delay before executing the bot's turn
        //int delay = 1000; // Adjust delay time as needed

            gameSession.playBotTurn(botPlayer);  // Execute the bot's turn
            updateGameState();  // Update the UI after the bot's turn

    }

    /**
     * This method is called when a bot takes an action.
     * 
     *
     * @param botPlayer The bot player who took the action.
     * @param playedCard The card that the bot player played.
     */
    @Override
    public void onBotAction(Player botPlayer, Card playedCard) {
        botTimer = new Timer(BOT_DELAY_MS, e -> {
            botTimer.stop();
            // The actual game progression to the next turn will be handled in other parts of the code
            // Specifically, it should be handled where human players interact, or after a complete bot cycle
        });
        botTimer.setRepeats(false); // Only run once
        botTimer.start();
    }

    /**
     * This method is called when the game is over.
     * It displays a game over message and returns to the main menu.
     */
    @Override
    public void onGameOver() {
        JOptionPane.showMessageDialog(this, "Game Over!", "End of Game", JOptionPane.INFORMATION_MESSAGE);
        dispose(); // Close the game window
        new MainMenuFrame().setVisible(true);
    }

    @Override
    public void onDirectionChanged(boolean isClockwise) {
        updateDirectionDisplay(isClockwise);
    }
    @Override
    public void onUpdateStatusPanel() {
        updateStatusPanel(statusPanel);
    }


    @Override
    public void onUnoDeclared(Player player) {
        JOptionPane.showMessageDialog(this, player.getUsername() + " has declared Uno!", "Uno Declared", JOptionPane.INFORMATION_MESSAGE);
        updateGameState();
    }

    @Override
    public void onUnoDeclaredFailed(Player player) {
        JOptionPane.showMessageDialog(this, player.getUsername() + " cannot declare Uno!", "Uno Declaration Failed", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onPenaltyApplied(Player player) {
        JOptionPane.showMessageDialog(this, player.getUsername() + " didn't declare Uno and must draw 2 cards!", "Penalty Applied", JOptionPane.WARNING_MESSAGE);
        updateGameState();
    }

    



}