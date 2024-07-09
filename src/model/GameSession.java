package model;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;



/**
 * This class represents a game session in the UNO game.
 * It manages the game flow, player turns, card plays, and game rules.
 */
public class GameSession {
    private List<Player> players;
    private Deck deck;
    private Card currentCard;
    private int currentPlayerIndex = 0;
    private boolean directionClockwise = true;
    private GameEventListener listener;
    private Card.Color currentColor; // Tracks the current color set by wild cards
    private final File userDataFile = new File("users.txt");
    private GameLogger logger;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private File saveFile;
    private boolean isLoaded = false;


    /**
     * Constructor for a game session.
     * @param players The list of players in the game.
     * @param listener The listener for game events.
     */
    public GameSession(List<Player> players, GameEventListener listener) {
        this.players = new ArrayList<>(players);
        this.deck = new Deck();
        this.listener = listener;
        this.logger = new GameLogger("GameSessionLog-");
        System.out.println("When did we come to the consturctor? " + isLoaded);
        shuffleAndDeal(); // Shuffle and deal cards if not loaded from a save

    }

    public GameSession() {
        players = new ArrayList<>(); // Ensure this is always initialized
        deck = new Deck(); // Initialize a new deck
        this.logger = new GameLogger("GameSessionLog-");
    }


    /**
     * Saves the current game state to a file.
     * The game state includes the current player index, current card, game direction, players' hands, and the deck.
     */
    public void saveGame() {
        try (PrintWriter out = new PrintWriter(new FileWriter(getSaveFileName()))) {
            out.println("Current Index:" + currentPlayerIndex);
            out.println("Current Card:" + currentCard);
            out.println("Current direction:" + directionClockwise);
            for (Player player : players) {
                out.println(player.getUsername() + ":" + player.handToString());
            }
            out.println("Deck:" + deckToString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving game: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Generates a save file name based on the current date and time.
     * @return The path of the save file.
     */
    private String getSaveFileName() {
        File saveDir = new File("saves");
        if (!saveDir.exists()) saveDir.mkdirs();  // Create the directory if it does not exist

        if (saveFile == null) {
            String filename = "UnoSave_" + dateFormat.format(new Date()) + ".txt";
            saveFile = new File(saveDir, filename);
        }
        return saveFile.getPath();
    }

    /**
     * Loads a game state from a file.
     * The game state includes the current player index, current card, game direction, players' hands, and the deck.
     * @param filePath The path of the file to load the game from.
     */
    public void loadGame(String filePath) {
        try (Scanner scanner = new Scanner(new File(filePath))) {
            currentPlayerIndex = Integer.parseInt(scanner.nextLine().split(":")[1].trim());
            currentCard = parseCard(scanner.nextLine().split(":")[1].trim());
            directionClockwise = Boolean.parseBoolean(scanner.nextLine().split(":")[1].trim());

            players.clear();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("Deck:")) {
                    deck = parseDeck(line.substring(5));
                    continue; // Skip to next iteration after processing deck
                }

                // Parse players assuming line is not about the deck
                try {
                    players.add(parsePlayer(line));
                } catch (Exception e) {
                    System.err.println("Failed to parse player from line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "File not found: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading game: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Converts the deck to a string representation.
     * The string representation includes the undrawn cards and the discarded cards.
     * @return The string representation of the deck.
     */
    public String deckToString() {
        return deck.getUndrawnCards().stream()
                .map(card -> card.getColor() + "_" + card.getValue())
                .collect(Collectors.joining(",")) +
                ";" +
                deck.getDiscardedCards().stream()
                        .map(card -> card.getColor() + "_" + card.getValue())
                        .collect(Collectors.joining(","));
    }

    /**
     * Parses a card from a string representation.
     * The string representation includes the card's color and value.
     * @param cardData The string representation of the card.
     * @return The parsed card.
     * @throws IllegalArgumentException If the card data is invalid.
     */
    private Card parseCard(String cardData) {
        cardData = cardData.replace("_", " "); // Ensure consistency in card data formatting
        String[] parts = cardData.split("\\s+");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid card data: " + cardData);
        }

        String colorStr = parts[0];
        String valueStr = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));

        try {
            Card.Color color = Card.Color.valueOf(colorStr);
            Card.Value value = Card.Value.valueOf(valueStr.replace(" ", "_").toUpperCase());
            return new Card(color, value);
        } catch (IllegalArgumentException e) {
            System.err.println("Error parsing card data: " + cardData);
            throw e;
        }
    }


    private Deck parseDeck(String deckStr) {
        String[] parts = deckStr.split(";");
        List<Card> undrawnCards = Arrays.stream(parts[0].split(",")).map(this::parseCard).collect(Collectors.toList());
        List<Card> discardedCards = Arrays.stream(parts[1].split(",")).map(this::parseCard).collect(Collectors.toList());
        return new Deck(undrawnCards, discardedCards); // You'll need to adjust the Deck constructor to accept these lists
    }

    private Player parsePlayer(String line) {
        // Assumes player line is structured as "username:card_list"
        String[] parts = line.split(":");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid player data: " + line);
        }
        String username = parts[0].trim();
        List<Card> hand = parseHand(parts[1].trim());
        boolean isBot = username.startsWith("Bot");  // Assuming bot names start with 'Bot'
        return new Player(username, isBot, hand);
    }

    private List<Card> parseHand(String handData) {
        List<Card> hand = new ArrayList<>();
        String[] cardStrs = handData.split(",");
        for (String cardStr : cardStrs) {
            try {
                hand.add(parseCard(cardStr.trim()));
                System.out.println("Added card to hand: " + cardStr.trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Error parsing card data: " + cardStr);
                // Handle error or continue parsing the next card
            }
        }
        return hand;
    }

    /**
     * Sets the current color in play.
     * @param color The color to set.
     */
    public void setCurrentColor(Card.Color color) {
        this.currentColor = color; // Method to update the current color
        System.out.println("Current color set to: " + color);

    }

    /**
     * Returns the current card in play.
     * @return The current card.
     */
    public Card getCurrentCard() {
        return currentCard;
    }

    /**
     * Returns the current color in play.
     * @return The current color.
     */
    public Card.Color getCurrentColor() {
        return currentColor;
    }


    /**
     * Shuffles the deck and deals cards to the players.
     */
    private void shuffleAndDeal() {
        if(isLoaded){
            return;
        }
        System.out.println("Current status of the loaded game: " + isLoaded);
        deck.shuffle();
        for (Player player : players) {
            System.out.println("hand of the player: " + player.getHand());
            if(!player.getHand().isEmpty()){
                System.out.println("did we get here?");
                currentCard = deck.draw();  // Start the discard pile with a valid card

                return;
            }
            for (int i = 0; i < 7; i++) {
                player.drawCard(deck.draw());
            }System.out.println(player.getUsername() + " has been dealt " + player.getHand());

        }
        
        
        
        currentCard = deck.draw();  // Start the discard pile with a valid card
        while(currentCard.getValue()==Card.Value.DRAW_FOUR
           || currentCard.getValue()==Card.Value.DRAW_TWO
           || currentCard.getValue()==Card.Value.SKIP
           || currentCard.getValue()==Card.Value.REVERSE
           || currentCard.getValue()==Card.Value.WILD) {
        	currentCard = deck.draw(); 
        }
        currentPlayerIndex = 0; // Start with the first player, adjust as needed for your game rules

    }

    /**
     * Proceeds to the next turn in the game.
     */
    public void nextTurn() {
        // If the game is over, stop proceeding to the next turn
        if (isGameOver()) {
            if (listener != null) {
                System.out.println("We came here unforuntly")  ;
                endGame();
            }
            return;
        }


        // Calculate and update the current player index
        currentPlayerIndex = calculateNextIndex();
        Player currentPlayer = players.get(currentPlayerIndex);
        logger.log("It is now " + currentPlayer.getUsername() + "'s turn.");

        // Notify the UI about the turn change
        if (listener != null) {
            listener.onTurnChanged(currentPlayer);
        }


        // If it's a human player's turn, the UI will wait for user interaction
    }



    /**
     * Handles the turn for a bot player.
     * @param botPlayer The bot player whose turn it is.
     */
    public void playBotTurn(Player botPlayer) {
        // Ensure that this method does not call nextTurn() directly to prevent recursion
        Card playedCard = botDecideCardToPlay(botPlayer);

        if (playedCard != null) {
            // Bot plays a card
            playCard(playedCard, botPlayer);
        } else {
            // Bot draws a card
            Card drawnCard = drawCardForPlayer(botPlayer);
            logger.log(getCurrentPlayer().getUsername() + " draws: " + drawnCard);
            if (drawnCard != null && isValidPlay(drawnCard)) {
                playCard(drawnCard, botPlayer);
            }
            // Note: No else needed because if the bot cannot play, it just ends its turn
        }

        // Instead of calling nextTurn(), we notify the UI about the bot action
        // The UI should handle when to call nextTurn() based on user interaction or a delay
        if (listener != null) {
            listener.onBotAction(botPlayer, playedCard);
        }
    }

    /**
     * Calculates the index of the next player.
     * @return The index of the next player.
     */
    private int calculateNextIndex() {  // Helper method to calculate the next player's index
        int step = directionClockwise ? 1 : -1;

        return (currentPlayerIndex + step + players.size()) % players.size();
    }

    /**
     * Attempts to play a card for a player.
     * @param card The card to play.
     * @param player The player who is playing the card.
     * @return True if the card was successfully played, false otherwise.
     */
    public boolean playCard(Card card, Player player) {
        int initialHandSize = player.getHand().size();
        System.out.println("Initial hand size: " + initialHandSize);
        System.out.println(player.getUsername() + " attempts to play " + card);
        System.out.println("Current play is: " + player.isBot());
        if (isValidPlay(card)) {
            if(!player.isBot()){
                int score = calculateScoreForCard(card);
                player.incrementScore(score);
            }
            boolean unoDeclared = player.hasDeclaredUno();
            executePlay(card, player);

            System.out.println("player hasn't declared uno: " + unoDeclared);
            if (initialHandSize == 2 && !unoDeclared) {
                System.out.println(player.getUsername() + " did not declare UNO.");
                applyPenaltyForNotDeclaringUno(player);
            }

            if(listener != null) {
                listener.onUpdateStatusPanel();
            }
            nextTurn();  // Move to the next player
            return true;
        }
        System.out.println("Invalid move attempted by " + player.getUsername());

        return false;
    }


    /**
     * Executes the play of a card.
     * @param card The card to play.
     * @param player The player who is playing the card.
     */
    private void executePlay(Card card, Player player) {
        currentCard = card; // Update the current card when a card is played
        deck.addToDiscardPile(currentCard); // Add the played card to the discard pile
        player.removeCard(card); // Remove the card from the player's hand
        System.out.println(player.getUsername() + " plays " + card + ", updating current card.");
        logger.log(player.getUsername() + " plays " + card);
        player.setUnoDeclared(false); // Reset the Uno declaration status

        applyCardEffect(card); // Apply the effect of the card if any ( we are not considering this in this project)
    }

    /**
     * Checks if a card is valid to play.
     * @param card The card to check.
     * @return True if the card is valid to play, false otherwise.
     */
    public boolean isValidPlay(Card card) {
        // A card can be played if:
        // 1. The card color matches the current color (or if it's a Wild card)
        // 2. The card value matches the current card's value
        // The currentColor is used if it's set; otherwise, the currentCard's color is used.

        boolean colorMatch = (currentColor != null) ? card.getColor() == currentColor : card.getColor() == currentCard.getColor();
        boolean valueMatch = card.getValue() == currentCard.getValue();
        boolean isWildCard = card.getColor() == Card.Color.WILD;

        return colorMatch || valueMatch || isWildCard;
    }

    /**
     * THIS IS NOT IMPLEMENTED IN THIS PROJECT
     * Applies the effect of a card.
     * @param card The card whose effect to apply.
     */
    private void applyCardEffect(Card card) {
        System.out.println("Applying effect of " + card);
        Player nextPlayer = players.get(calculateNextIndex()); // Get the next player


        switch (card.getValue()) {
            case SKIP:
                // Skip the next player
            	nextTurn();

                System.out.println("Next player skipped.");

                break;
            case REVERSE:
                // Change the direction of play
                logger.log("Player " + getCurrentPlayer().getUsername() + " reversed the direction of play.");
                directionClockwise = !directionClockwise; // Reverse the direction
                System.out.println("Direction of play reversed.");
                if (players.size() == 2) { // Special case for 2 players where reverse acts like skip
                    nextTurn();
                }

                if (listener != null) {
                    listener.onDirectionChanged(directionClockwise);  // Notify listener about direction change
                }
                break;
            case DRAW_TWO:
                // The next player draws two cards and skips their turn
                logger.log("Player " + getCurrentPlayer().getUsername() + " made " + nextPlayer.getUsername() + " draw two cards.");
                nextPlayer = players.get(calculateNextIndex());
                drawCardForPlayer(nextPlayer);
                drawCardForPlayer(nextPlayer);
                nextTurn();
                System.out.println(nextPlayer.getUsername() + " draws two cards.");
                break;
            case WILD:
                logger.log("Player " + getCurrentPlayer().getUsername() + " played WILD card.");
                requestColorChange(); // current player changes the color depending on the game interface
                if(listener != null) {
                    listener.onUpdateStatusPanel();
                }
                break;
            case DRAW_FOUR:
            	logger.log("Player " + getCurrentPlayer().getUsername() + " played DRAW_FOUR card.");
                requestColorChange();// current player changes the color depending on the game interface
                nextPlayer = players.get(calculateNextIndex());
                drawCardForPlayer(nextPlayer);
                drawCardForPlayer(nextPlayer);
                drawCardForPlayer(nextPlayer);
                drawCardForPlayer(nextPlayer);
                nextTurn();
                if(listener != null) {
                    listener.onUpdateStatusPanel();
                }
                break;
        }
    }
    /**
     * Requests a color change from the current player.
     */
    private void requestColorChange() {
        // This method should prompt the current player for a color if they are human,
        // or automatically choose a color if they are a bot.
        Player currentPlayer = getCurrentPlayer();
        if (currentPlayer.isBot()) {
            // Bot chooses a color automatically.
            Card.Color chosenColor = currentPlayer.chooseColorAutomatically();
            setCurrentColor(chosenColor);
            logger.log(currentPlayer.getUsername() + " changes Deck color to:  " + chosenColor);

        } else {
            // Notify the listener (GameInterface)to prompt the user for a color.
            if (listener != null) {
                listener.onRequestColorSelection(currentPlayer, new Card(Card.Color.WILD, Card.Value.WILD));
            } else {
                // Handle the case where there is no listener connected.
                System.out.println("Error: No listener to handle color selection."); // happens when we dont pass the
            }
        }
    }


    

    /**
     * Checks if the game is over.
     * @return True if the game is over, false otherwise.
     */
    public boolean isGameOver() {
        return players.stream().anyMatch(player -> player.getHand().isEmpty());
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }


    /**
     * Handles the play of a Wild card.
     * @param player The player who played the card.
     * @param card The card that was played.
     */
    public void handleWildCard(Player player, Card card) {
        if (card.getValue() == Card.Value.WILD || card.getValue() == Card.Value.DRAW_FOUR) {
            if (!player.isBot()) {
                listener.onRequestColorSelection(player, card);
            } else {
                Card.Color chosenColor = player.chooseColorAutomatically();
                setCurrentColor(chosenColor);  // Set the current color for game logic
                // setCurrentCard(new Card(Card.Color.WILD, card.getValue()));  // Keep the card as Wild without a specific color
            }
        }
    }


    public void setCurrentCard(Card card) {
        this.currentCard = card;  // Set the new current card after color selection
        System.out.println("New current card set to: " + card);
    }


    /**
     * Draws a card for a player.
     * @param player The player to draw a card for.
     * @return The card that was drawn, or null if the card is not playable.
     */
    public Card drawCardForPlayer(Player player) {
        Card drawnCard = deck.draw();
        player.drawCard(drawnCard);
        System.out.println(player.getUsername() + " draws " + drawnCard);
        if(listener != null) {
            listener.onUpdateStatusPanel();
        }
        if (isValidPlay(drawnCard)) {
            return drawnCard; // Return the drawn card if it's playable
        }
        nextTurn(); // Automatically pass the turn if the card is not playable
        return null;
    }

    /**
     * Decides which card a bot player should play.
     * @param botPlayer The bot player who needs to decide.
     * @return The card the bot player chooses to play, or null if no valid card is found.
     */
    public Card botDecideCardToPlay(Player botPlayer) {
        List<Card> playableCards = new ArrayList<>();

        // Check for cards of the same color or value, or wild cards. Collect them together then bot will decide on it
        for (Card card : botPlayer.getHand()) {
            if (isValidPlay(card)) {
                playableCards.add(card);
            }
        }

        // If there are no playable cards, return null so the bot will draw a card.
        if (playableCards.isEmpty()) {
            return null;
        }

        // Decision logic for which card to play.
        // This simple logic prioritizes action cards and then chooses randomly among them.
        List<Card> actionCards = playableCards.stream()
                .filter(card -> card.getValue() == Card.Value.SKIP ||
                        card.getValue() == Card.Value.REVERSE ||
                        card.getValue() == Card.Value.DRAW_TWO ||
                        card.getValue() == Card.Value.WILD ||
                        card.getValue() == Card.Value.DRAW_FOUR)
                .collect(Collectors.toList());

        if (!actionCards.isEmpty()) {
            Collections.shuffle(actionCards);
            return actionCards.get(0); // Play an action card if available.
        }

        // If no action cards, play any other playable card.
        Collections.shuffle(playableCards);
        return playableCards.get(0);
    }

    public List<Player> getPlayers() {
        return players;
    }



    /**
     * Updates the player statistics and saves them to a file.
     */
    private void updateAndSaveStatistics() {
        Player realPlayer = null;
        for (Player p : players) {
            if (!p.isBot()) { // Check if the player is not a bot
                realPlayer = p;
                break; // Since there's only one real player, break once found
            }
        }
    
        if (realPlayer == null) {
            System.err.println("No real player found.");
            return;
        }
    
        boolean userFound = false;
    
        // Read the existing file and update the player's statistics
        try (BufferedReader reader = new BufferedReader(new FileReader(userDataFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(userDataFile + ".tmp"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5 && parts[0].equals(realPlayer.getUsername())) {
                    // Update the real player's statistics
                    int gamesPlayed = Integer.parseInt(parts[2]) + 1; // Increment games played
                    int wins = Integer.parseInt(parts[3]) + (realPlayer.getHand().isEmpty() ? 1 : 0); // Increment wins if the player has no cards left
                    int totalScore = Integer.parseInt(parts[4]) + realPlayer.getTotalScore();
                    writer.write(String.format("%s,%s,%d,%d,%d\n", parts[0], parts[1], gamesPlayed, wins, totalScore));
                    System.out.println("Updated statistics for user: " + parts[0]);
                    userFound = true;
                } else {
                    writer.write(line + "\n"); // Write other users' data as is
                }
            }
            // If user not found in file, add new entry
            if (!userFound) {
                writer.write(String.format("%s,%s,%d,%d,%d\n", realPlayer.getUsername(), "password", 1, realPlayer.getHand().isEmpty() ? 1 : 0, realPlayer.getTotalScore()));
                System.out.println("Added new user statistics for: " + realPlayer.getUsername());
            }
        } catch (IOException e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            return;
        }
    
        // Ensure the original file is closed before renaming
        try {
            // Close the BufferedReader before renaming the file
            BufferedReader reader = new BufferedReader(new FileReader(userDataFile));
            reader.close();
    
            File tempFile = new File(userDataFile + ".tmp");
            File originalFile = new File(String.valueOf(userDataFile));
            
            // Attempt to delete the original file first
            if (originalFile.delete()) {
                System.out.println("Original file deleted successfully.");
            } else {
                System.err.println("Failed to delete the original file.");
            }
    
            // Rename the temp file to the original file
            if (tempFile.renameTo(originalFile)) {
                System.out.println("Temporary file renamed to original file successfully.");
            } else {
                System.err.println("Could not rename temporary file to original file.");
            }
        } catch (IOException e) {
            System.err.println("Error closing file: " + e.getMessage());
        }
    }


    /**
     * Ends the game and updates the player statistics.
     */
    public void endGame() {
        updateAndSaveStatistics();
        if (listener != null) {
            listener.onGameOver();
        }
    }

    /**
     * Calculates the score for a card.
     * @param card The card to calculate the score for.
     * @return The score for the card.
     */
    private int calculateScoreForCard(Card card) {
        switch (card.getValue()) {
            case DRAW_TWO:
            case REVERSE:
            case SKIP:
                return 20;
            case WILD:
            case DRAW_FOUR:
                return 50;
            default:
                return card.getValue().ordinal();
        }
    }

    public void setListener(GameEventListener listener) {
        this.listener = listener;
    }

    public boolean isDirectionClockwise() {
        return directionClockwise;
    }


    public Deck getDeck() {
        return deck;
    }

    public void declareUno(Player player) {
        if (canDeclareUno(player)) {
            player.setUnoDeclared(true);
            listener.onUnoDeclared(player);
        } else {
            listener.onUnoDeclaredFailed(player);
        }
    }

    public boolean canDeclareUno(Player player) {
        if (player.getHand().size() == 2) {
            return true;
        }
    
        Card topCard = getCurrentCard();
        return player.getHand().stream().anyMatch(card -> card.matches(topCard));
    }

    private void applyPenaltyForNotDeclaringUno(Player player) {
        if (!player.hasDeclaredUno()) {
            for (int i = 0; i < 2; i++) {
                player.drawCard(deck.draw());
            }
            listener.onPenaltyApplied(player);
        }
    }













}







