package chess;

import chess.board.ChessBoard;
import chess.model.Move;
import chess.model.ChessPiece;
import chess.model.ChessTimer;
import javax.swing.*;
import java.awt.*;

/**
 * Main class for the Chess Game application. This class sets up the GUI and
 * manages the game flow.
 *
 * @author Do Duc Hai - CE192014
 */
public class ChessGame extends JFrame implements ChessTimer.TimerListener {

    private ChessBoard board; // The chess board

    public boolean isWhiteTurn = true; // Tracks whose turn it is (White or Black)

    public JLabel statusLabel; // Displays the current game status

    public JTextArea moveHistory; // Displays the history of moves
    private int moveNumber = 1; // Tracks the move number
    private JButton restartButton; // Button to restart the game
    private JButton startButton; // Button to start the game
    private ChessTimer timer; // Timer for the game
    private JLabel whiteTimeLabel; // Displays White's remaining time
    private JLabel blackTimeLabel; // Displays Black's remaining time
    private boolean gameStarted = false; // Tracks if the game has started

    /**
     * Constructor to initialize the Chess Game.
     */
    public ChessGame() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize timer
        timer = new ChessTimer(this);

        // Create board
        board = new ChessBoard(this);
        add(board, BorderLayout.CENTER);

        // Right panel for move history and clocks
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Clock panel
        JPanel clockPanel = new JPanel(new GridLayout(2, 1));
        whiteTimeLabel = new JLabel("White: 10:00", SwingConstants.CENTER);
        blackTimeLabel = new JLabel("Black: 10:00", SwingConstants.CENTER);
        whiteTimeLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        blackTimeLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        clockPanel.add(whiteTimeLabel);
        clockPanel.add(blackTimeLabel);
        rightPanel.add(clockPanel, BorderLayout.NORTH);

        // Move history
        moveHistory = new JTextArea(20, 15);
        moveHistory.setEditable(false);
        rightPanel.add(new JScrollPane(moveHistory), BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel statusPanel = new JPanel();
        statusLabel = new JLabel("Press Start to begin the game");
        statusPanel.add(statusLabel);
        bottomPanel.add(statusPanel, BorderLayout.WEST);

        // Button panel with both Start and Restart buttons
        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start Game");
        startButton.addActionListener(e -> startGame());

        restartButton = new JButton("Restart Game");
        restartButton.addActionListener(e -> restartGame());
        restartButton.setEnabled(false); // Disable restart until game starts

        buttonPanel.add(startButton);
        buttonPanel.add(restartButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Starts the game.
     */
    private void startGame() {
        gameStarted = true;
        startButton.setEnabled(false);
        restartButton.setEnabled(true);
        statusLabel.setText("White's turn");
        board.setGameStarted(true);
        timer.startTurn(true);
    }

    /**
     * Restarts the game.
     */
    public void restartGame() {
        board.restartGame();
        moveHistory.setText("");
        moveNumber = 1;
        isWhiteTurn = true;
        gameStarted = false;
        startButton.setEnabled(true);
        restartButton.setEnabled(false);
        statusLabel.setText("Press Start to begin the game");
        timer.reset();
        board.setGameStarted(false);
    }

    /**
     * Checks if the game has started.
     *
     * @return true if the game has started, false otherwise
     */
    public boolean isGameStarted() {
        return gameStarted;
    }

    /**
     * Adds a move to the move history.
     *
     * @param move The move to be added
     */
    public void addMoveToHistory(Move move) {
        String moveText;
        if (isWhiteTurn) {
            moveText = moveNumber + ". ";
        } else {
            moveText = "   ";
            moveNumber++;
        }

        moveText += getAlgebraicNotation(move) + "\n";
        moveHistory.append(moveText);
        isWhiteTurn = !isWhiteTurn;
        statusLabel.setText(isWhiteTurn ? "White's turn" : "Black's turn");

        // Switch the timer to the next player
        timer.startTurn(isWhiteTurn);
    }

    @Override
    public void onTimeUpdated(boolean isWhite, int timeLeft) {
        String timeStr = ChessTimer.formatTime(timeLeft);
        if (isWhite) {
            whiteTimeLabel.setText("White: " + timeStr);
        } else {
            blackTimeLabel.setText("Black: " + timeStr);
        }
    }

    @Override
    public void onTimeOut(boolean isWhite) {
        timer.stopTimers();
        String winner = isWhite ? "Black" : "White";
        JOptionPane.showMessageDialog(this,
                winner + " wins on time!",
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
        statusLabel.setText(winner + " wins on time!");
    }

    /**
     * Converts a move to algebraic notation.
     *
     * @param move The move to convert
     * @return The move in algebraic notation
     */
    private String getAlgebraicNotation(Move move) {
        String notation;
        if (move.isCastling) {
            notation = move.toCol > move.fromCol ? "O-O" : "O-O-O";
        } else {
            notation = getPieceNotation(move.piece)
                    + (char) ('a' + move.toCol)
                    + (8 - move.toRow);
            if (move.isPromotion) {
                notation += "=Q"; // Default to Queen promotion
            }
        }
        return notation;
    }

    /**
     * Gets the notation for a chess piece.
     *
     * @param piece The chess piece
     * @return The notation for the piece
     */
    private String getPieceNotation(ChessPiece piece) {
        switch (piece.type) {
            case KING:
                return "K";
            case QUEEN:
                return "Q";
            case ROOK:
                return "R";
            case BISHOP:
                return "B";
            case KNIGHT:
                return "N";
            case PAWN:
                return "P";
            default:
                return "";
        }
    }

    /**
     * Main method to start the Chess Game.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGame().setVisible(true);
        });
    }
}
