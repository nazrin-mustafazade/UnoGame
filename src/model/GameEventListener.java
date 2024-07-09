package model;

/**
 * This interface represents a listener for game events.
 * It includes methods for handling color selection requests, turn changes, bot actions, and game over events.
 */
public interface GameEventListener {
    /**
     * This method is called when a color selection is requested.
     *
     * @param player The player who needs to select a color.
     * @param wildCard The wild card that triggered the color selection.
     */
    void onRequestColorSelection(Player player, Card wildCard);

    /**
     * This method is called when the turn changes.
     *
     * @param currentPlayer The player whose turn it is now.
     */
    void onTurnChanged(Player currentPlayer);

    /**
     * This method is called when a bot takes an action.
     *
     * @param botPlayer The bot player who took the action.
     * @param playedCard The card that the bot player played.
     */
    void onBotAction(Player botPlayer, Card playedCard);

    /**
     * This method is called when the game ends.
     */
    void onGameOver();
    void onDirectionChanged(boolean isClockwise);

    void onUpdateStatusPanel();
    void onUnoDeclared(Player player);
    void onPenaltyApplied(Player player) ;
    void onUnoDeclaredFailed(Player player) ;
    






}