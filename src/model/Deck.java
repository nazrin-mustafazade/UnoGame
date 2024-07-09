package model;

import java.util.Collections;
import java.util.List;
import java.util.Stack;



/**
 * This class represents a Deck in the game.
 * A Deck is a collection of Cards.
 * It includes methods for initializing the deck, shuffling the deck, reshuffling the discard pile into the deck,
 * adding a card to the deck, peeking at the top card, adding a card to the discard pile, drawing a card, and checking if the deck is empty.
 */
public class Deck {
    private Stack<Card> cards = new Stack<>();
    private Stack<Card> discardPile; // The discard pile


    /**
     * Constructor for the Deck class.
     * Initializes the deck of cards and shuffles it.
     * Also initializes the discard pile.
     */
    public Deck() {
        initializeDeck();
        shuffle();
        discardPile = new Stack<>();
    }

    /**
     * Constructor for the Deck class that accepts external lists of cards.
     * This constructor is used primarily for loading a saved game state.
     *
     * @param undrawnCards The list of undrawn cards to be loaded into the deck.
     * @param discardedCards The list of discarded cards to be loaded into the discard pile.
     */
    public Deck(List<Card> undrawnCards, List<Card> discardedCards) {
        cards = new Stack<>();
        discardPile = new Stack<>();
        cards.addAll(undrawnCards);
        discardPile.addAll(discardedCards);
    }



    /**
     * This method initializes the deck of cards.
     * It adds number cards, action cards, and wild cards to the deck.
     */
    private void initializeDeck() {
        // Add number cards
        for (Card.Color color : Card.Color.values()) {
            if (!color.equals(Card.Color.WILD)) { // Skip wild color for number cards
                for (int i = 0; i < 2; i++) {  // Each number card twice except 0
                    for (int num = 1; num <= 9; num++) {
                        cards.add(new Card(color, Card.Value.values()[num]));
                    }
                }
                cards.add(new Card(color, Card.Value.ZERO));  // Zero card once per color
            }
        }

        // Add action and wild cards
        for (int i = 0; i < 2; i++) {
            for (Card.Color color : Card.Color.values()) {
                if (!color.equals(Card.Color.WILD)) {
                    cards.add(new Card(color, Card.Value.SKIP));
                    cards.add(new Card(color, Card.Value.REVERSE));
                    cards.add(new Card(color, Card.Value.DRAW_TWO));
                }
            }
            // Wild and Wild Draw Four are always colorless
            cards.add(new Card(Card.Color.WILD, Card.Value.WILD));
            cards.add(new Card(Card.Color.WILD, Card.Value.DRAW_FOUR));
        }
    }



    /**
     * This method shuffles the deck of cards.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * This method reshuffles the discard pile back into the deck when the deck is empty.
     */
    public void reshuffleDiscardIntoDeck() {
        if (cards.isEmpty() && !discardPile.isEmpty()) {
            Card topDiscard = discardPile.pop();  // Preserve the current top card of the discard pile
            Collections.shuffle(discardPile);  // Shuffle the rest of the discard pile
            cards.addAll(discardPile);  // Move the shuffled discard pile back to the draw pile
            discardPile.clear();  // Clear the discard pile
            discardPile.push(topDiscard);  // Return the top card back to the discard pile
            System.out.println("Discard pile reshuffled into the draw pile.");
        }
    }

    /**
     * This method adds a card to the top of the deck.
     *
     * @param card The card to add to the deck.
     */
    public void push(Card card) {
        cards.push(card);
    }


    /**
     * This method returns the card at the top of the deck without removing it.
     *
     * @return The card at the top of the deck.
     */
    public Card peek() {
        if (!cards.isEmpty()) {
            return cards.peek();
        } else {
            return null; // or throw an exception, depending on your error handling
        }
    }

    /**
     * This method adds a card to the discard pile.
     *
     * @param card The card to add to the discard pile.
     */
    public void addToDiscardPile(Card card) {
        discardPile.push(card);
    }

    /**
     * This method removes and returns the card at the top of the deck.
     * If the deck is empty, it reshuffles the discard pile back into the deck.
     *
     * @return The card at the top of the deck.
     */
    public Card draw() {
        if (cards.isEmpty()) {
            reshuffleDiscardIntoDeck();
        }
        return cards.pop();
    }

    // Getters for serialization purposes
    public Stack<Card> getUndrawnCards() {
        return cards;
    }

    public Stack<Card> getDiscardedCards() {
        return discardPile;
    }




    public boolean isEmpty() {
        return cards.isEmpty();
    }





}
