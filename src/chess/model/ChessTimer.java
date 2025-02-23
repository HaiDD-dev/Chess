package chess.model;

import javax.swing.Timer;

/**
 * Manages the chess game timer for both players (White and Black). This class
 * handles countdown, turn switching, and time updates.
 *
 * @author Do Duc Hai - CE192014
 */
public class ChessTimer {

    private int whiteTimeLeft; // Remaining time for White (in milliseconds)
    private int blackTimeLeft; // Remaining time for Black
    private Timer whiteTimer; // Timer for White's turn
    private Timer whiteUpdateTimer; // Timer to update White's time display
    private Timer blackTimer; // Timer for Black's turn
    private Timer blackUpdateTimer; // Timer to update Black's time display
    private boolean isWhiteTurn; // Tracks whose turn it is
    private TimerListener listener; // Listener for time updates and timeouts

    /**
     * Interface for handling timer events.
     */
    public interface TimerListener {

        /**
         * Called when the time is updated for a player.
         *
         * @param isWhite True if the update is for White, false for Black.
         * @param timeLeft The remaining time in milliseconds.
         */
        void onTimeUpdated(boolean isWhite, int timeLeft);

        /**
         * Called when a player runs out of time.
         *
         * @param isWhite True if White runs out of time, false for Black.
         */
        void onTimeOut(boolean isWhite);
    }

    /**
     * Constructor to initialize the chess timer.
     *
     * @param listener The listener to handle timer events.
     */
    public ChessTimer(TimerListener listener) {
        this.listener = listener;
        this.whiteTimeLeft = 600000; // 10 minutes = 600,000 milliseconds
        this.blackTimeLeft = 600000;

        // Timer for White's turn (counts down every second)
        whiteTimer = new Timer(1000, e -> {
            whiteTimeLeft -= 1000;
            if (whiteTimeLeft <= 0) {
                whiteTimer.stop();
                whiteUpdateTimer.stop();
                listener.onTimeOut(true); // Notify that White has run out of time
            }
        });

        // Timer to update White's time display every 100ms
        whiteUpdateTimer = new Timer(100, e -> {
            listener.onTimeUpdated(true, whiteTimeLeft);
        });

        // Timer for Black's turn (counts down every second)
        blackTimer = new Timer(1000, e -> {
            blackTimeLeft -= 1000;
            if (blackTimeLeft <= 0) {
                blackTimer.stop();
                blackUpdateTimer.stop();
                listener.onTimeOut(false); // Notify that Black has run out of time
            }
        });

        // Timer to update Black's time display every 100ms
        blackUpdateTimer = new Timer(100, e -> {
            listener.onTimeUpdated(false, blackTimeLeft);
        });
    }

    /**
     * Starts the timer for the current player's turn.
     *
     * @param isWhiteTurn True if it's White's turn, false for Black.
     */
    public void startTurn(boolean isWhiteTurn) {
        this.isWhiteTurn = isWhiteTurn;
        if (isWhiteTurn) {
            blackTimer.stop();
            blackUpdateTimer.stop();
            whiteTimer.start();
            whiteUpdateTimer.start();
        } else {
            whiteTimer.stop();
            whiteUpdateTimer.stop();
            blackTimer.start();
            blackUpdateTimer.start();
        }
    }

    /**
     * Stops all timers.
     */
    public void stopTimers() {
        whiteTimer.stop();
        whiteUpdateTimer.stop();
        blackTimer.stop();
        blackUpdateTimer.stop();
    }

    /**
     * Resets the timer to the initial state (10 minutes for both players).
     */
    public void reset() {
        whiteTimeLeft = 600000;
        blackTimeLeft = 600000;
        stopTimers();
        // Update the display for both players
        listener.onTimeUpdated(true, whiteTimeLeft);
        listener.onTimeUpdated(false, blackTimeLeft);
    }

    /**
     * Formats the time in milliseconds into a "MM:SS" string.
     *
     * @param milliseconds The time in milliseconds.
     * @return The formatted time string.
     */
    public static String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
