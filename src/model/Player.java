package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This class represents a Player in the game.
 * A Player has a username, a hand of cards, and various game statistics.
 */
public class Player {
    private String username;
    private boolean isBot;
    private List<Card> hand;
    private boolean declaredUno;
    private int gamesPlayed;
    private int wins;
    private int totalScore;

    /**
     * Constructor for the Player class.
     * Initializes the player's username, hand of cards, and game statistics.
     *
     * @param username The username of the player.
     * @param isBot Whether the player is a bot or not.
     */
    public Player(String username, boolean isBot) {
        this.username = username;
        this.hand = new ArrayList<>();
        this.isBot = isBot;
        this.declaredUno = false;
        this.gamesPlayed = 0;
        this.wins = 0;
        this.totalScore = 0;
    }

    public Player(String username, boolean isBot, List<Card> hand) {
        this.username = username;
        this.isBot = isBot;
        this.hand = new ArrayList<>(hand); // Ensuring a new list is created
        this.gamesPlayed = 0;
        this.wins = 0;
        this.totalScore = 0;
        this.declaredUno = false;

    }

    /**
     * This method adds a card to the player's hand and resets the Uno declaration.
     *
     * @param card The card to add to the player's hand.
     */
    public void drawCard(Card card) {
        this.hand.add(card);
        this.declaredUno = false;  // Reset Uno declaration when a new card is drawn
    }

    /**
     * This method checks if the player has declared Uno.
     *
     * @return True if the player has declared Uno, false otherwise.
     */
    public boolean hasDeclaredUno() {
        return declaredUno;
    }

    /**
     * This method returns the player's hand of cards.
     *
     * @return The player's hand of cards.
     */
    public List<Card> getHand() {
        return hand;
    }

    /**
     * This method returns the player's username.
     *
     * @return The player's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * This method checks if the player is a bot.
     *
     * @return True if the player is a bot, false otherwise.
     */
    public boolean isBot() {
        return isBot;
    }

    /**
     * This method removes a specified card from the player's hand.
     *
     * @param card The card to be removed from the player's hand.
     */
    public void removeCard(Card card) {
        hand.remove(card);
    }

    /**
     * This method simulates a bot playing a card. OPTIONAL FOR FUTURE IMPLEMENTATION.
     *
     * @param currentCard The current card in play.
     * @return The card the bot chooses to play, or null if no valid card is found.
     */
    public Card autoPlay(Card currentCard) {
        for (Card card : hand) {
            if (card.getColor() == currentCard.getColor() || card.getValue() == currentCard.getValue() || card.getColor() == Card.Color.WILD) {
                return card;
            }
        }
        return null;  // No valid card to play
    }


    public String handToString() {
        return hand.stream()
                .map(card -> card.getColor() + "_" + card.getValue())
                .collect(Collectors.joining(","));
    }



    /**
     * This method chooses a color automatically based on the highest count in the player's hand.
     *
     * @return The chosen color.
     */
    public Card.Color chooseColorAutomatically() {
        int[] colorCounts = new int[Card.Color.values().length - 1];  // Exclude WILD
        for (Card c : hand) {
            if (c.getColor() != Card.Color.WILD) {
                colorCounts[c.getColor().ordinal()]++;
            }
        }
        int maxIndex = 0;
        for (int i = 1; i < colorCounts.length; i++) {
            if (colorCounts[i] > colorCounts[maxIndex]) {
                maxIndex = i;
            }
        }
        return Card.Color.values()[maxIndex];
    }

    /**
     * This method returns the number of games the player has played.
     *
     * @return The number of games the player has played.
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * This method sets the number of games the player has played.
     *
     * @param gamesPlayed The number of games the player has played.
     */
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    /**
     * This method returns the number of games the player has won.
     *
     * @return The number of games the player has won.
     */
    public int getWins() {
        return wins;
    }

    /**
     * This method sets the number of games the player has won.
     *
     * @param wins The number of games the player has won.
     */
    public void setWins(int wins) {
        this.wins = wins;
    }

    /**
     * This method returns the player's total score.
     *
     * @return The player's total score.
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * This method sets the player's total score.
     *
     * @param totalScore The player's total score.
     */
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    /**
     * This method increments the player's score by a specified amount.
     *
     * @param score The amount to increment the player's score by.
     */
    public void incrementScore(int score) {
        this.totalScore += score;
    }

    /**
     * This method returns a string representation of the player.
     *
     * @return A string representation of the player.
     */
    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + '\'' +
                ", hand=" + hand +
                '}';
    }

    public void setUnoDeclared(boolean declaredUno) {
        this.declaredUno = declaredUno;
    }
}