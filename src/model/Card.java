package model;

/**
 * This class represents a Card in the game.
 * Each card has a color and a value.
 * The color can be RED, YELLOW, GREEN, BLUE, or WILD.
 * The value can be ZERO to NINE, SKIP, REVERSE, DRAW_TWO, WILD, or DRAW_FOUR.
 */
public class Card {
    /**
     * Enum for the color of the card.
     */
    public enum Color {
        RED, YELLOW, GREEN, BLUE, WILD // 'WILD' here to denote the color for wild cards
    }

    /**
     * Enum for the value of the card.
     */
    public enum Value {
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
        SKIP, REVERSE, DRAW_TWO, WILD, DRAW_FOUR // 'WILD' for normal wild, 'DRAW_FOUR' for wild draw four
    }

    private final Color color;
    private final Value value;

    /**
     * Constructor for the Card class.
     * Initializes the color and value fields with the provided values.
     *
     * @param color The color of the card.
     * @param value The value of the card.
     */
    public Card(Color color, Value value) {
        this.color = color;
        this.value = value;
    }

    /**
     * Getter for the color field.
     *
     * @return The color of the card.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Getter for the value field.
     *
     * @return The value of the card.
     */
    public Value getValue() {
        return value;
    }

    /**
     * This method returns a string representation of the card.
     * For wild cards, it does not include the color in the string.
     *
     * @return A string representation of the card.
     */
    @Override
    public String toString() {
        // For wild cards, don't include the color in the string.
        if (value == Value.WILD || value == Value.DRAW_FOUR) {
            return value.toString();
        } else {
            return color + " " + value;
        }
    }

    public boolean matches(Card topCard) {
        // Check if the color or value of the card matches the top card
        return  (color == topCard.color || value == topCard.value || color == Color.WILD);

    }
}