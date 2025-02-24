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

    private ChessBoard board;
    public boolean isWhiteTurn = true;
    public JLabel statusLabel;
    public JTextArea moveHistory;
    private int moveNumber = 1;
    private JButton restartButton;
    private JButton startButton;
    private ChessTimer timer;
    private JLabel whiteTimeLabel;
    private JLabel blackTimeLabel;
    private boolean gameStarted = false;

    // Bo3 related fields
    private JLabel scoreLabel;
    private double whiteWins = 0.0;
    private double blackWins = 0.0;
    private static final double WINS_NEEDED = 2.0;

    public ChessGame() {
        setTitle("Chess Game (Best of 3)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize timer and board
        timer = new ChessTimer(this);
        board = new ChessBoard(this);

        // Set up the main game area
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(board, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        // Right panel setup
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        // Score panel
        JPanel scorePanel = new JPanel();
        scoreLabel = new JLabel("<html>-- Score --<br>White: 0.0<br>Black: 0.0</html>");
        scoreLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        scorePanel.add(scoreLabel);
        rightPanel.add(scorePanel);

        // Clock panel
        JPanel clockPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        whiteTimeLabel = new JLabel("White: 10:00", SwingConstants.CENTER);
        blackTimeLabel = new JLabel("Black: 10:00", SwingConstants.CENTER);
        whiteTimeLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        blackTimeLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        clockPanel.add(whiteTimeLabel);
        clockPanel.add(blackTimeLabel);
        rightPanel.add(clockPanel);

        // Move history
        moveHistory = new JTextArea(20, 15);
        moveHistory.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(moveHistory);
        rightPanel.add(scrollPane);

        add(rightPanel, BorderLayout.EAST);

        // Bottom control panel
        JPanel controlPanel = new JPanel(new BorderLayout());

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Press Start to begin the game");
        statusPanel.add(statusLabel);
        controlPanel.add(statusPanel, BorderLayout.WEST);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        startButton = new JButton("Start Game");
        restartButton = new JButton("Restart");

        startButton.addActionListener(e -> startGame());
        restartButton.addActionListener(e -> restartMatch());

        restartButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(restartButton);
        controlPanel.add(buttonPanel, BorderLayout.EAST);

        add(controlPanel, BorderLayout.SOUTH);

        // Window setup
        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
    }

    public void startGame() {
        gameStarted = true;
        startButton.setEnabled(false);
        restartButton.setEnabled(true);
        statusLabel.setText("White's turn");
        board.setGameStarted(true);
        timer.startTurn(true);
        board.initializeBoard(); // Use the existing initialization method
        board.repaint(); // Ensure the board is redrawn
    }

    public void restartMatch() {
        whiteWins = 0.0;
        blackWins = 0.0;
        updateScoreLabel();
        restartGame();
    }

    public void restartGame() {
        board.initializeBoard(); // Use the existing initialization method
        board.repaint(); // Ensure the board is redrawn
        moveHistory.setText("");
        moveNumber = 1;
        isWhiteTurn = true;
        gameStarted = false;
        startButton.setEnabled(true);
        restartButton.setEnabled(false);
        statusLabel.setText("Press Start to begin the game");
        timer.reset();
        board.setGameStarted(false);
        updateScoreLabel();
    }

    private void updateScoreLabel() {
        scoreLabel.setText(String.format("<html>-- Score --<br>White: %.1f<br>Black: %.1f</html>", whiteWins, blackWins));
    }

    public void onGameWon(boolean whiteWinner) {
        timer.stopTimers();

        if (whiteWinner) {
            whiteWins++;
        } else {
            blackWins++;
        }
        updateScoreLabel();

        // Handle match or game completion
        SwingUtilities.invokeLater(() -> {
            if (whiteWins >= WINS_NEEDED || blackWins >= WINS_NEEDED) {
                handleMatchEnd();
            } else {
                handleGameEnd(whiteWinner);
            }
        });
    }

    public void onGameDraw() {
        timer.stopTimers();

        // Add 0.5 points to both players
        whiteWins += 0.5;
        blackWins += 0.5;
        updateScoreLabel();

        // Handle match or game completion
        SwingUtilities.invokeLater(() -> {
            if (whiteWins >= WINS_NEEDED || blackWins >= WINS_NEEDED) {
                handleMatchEnd();
            } else {
                handleGameDraw();
            }
        });
    }

    private void handleGameDraw() {
        String message = String.format("Game is a draw! Current score: White %.1f - Black %.1f",
                whiteWins, blackWins);
        JOptionPane.showMessageDialog(this, message, "Game Draw", JOptionPane.INFORMATION_MESSAGE);
        restartGame();
    }

    private void handleMatchEnd() {
        String matchWinner;
        if (whiteWins > blackWins) {
            matchWinner = "White";
        } else if (blackWins > whiteWins) {
            matchWinner = "Black";
        } else {
            matchWinner = "Draw";
        }

        String message;
        if (matchWinner.equals("Draw")) {
            message = String.format("Match ends in a draw %.1f - %.1f!", whiteWins, blackWins);
        } else {
            message = String.format("%s wins the match %.1f - %.1f!",
                    matchWinner,
                    Math.max(whiteWins, blackWins),
                    Math.min(whiteWins, blackWins));
        }

        JOptionPane.showMessageDialog(this, message, "Match Over", JOptionPane.INFORMATION_MESSAGE);
        restartMatch();
    }

    private void handleGameEnd(boolean whiteWinner) {
        String gameWinner = whiteWinner ? "White" : "Black";
        String message = String.format("%s wins the game! Current score: %.1f - %.1f",
                gameWinner, whiteWins, blackWins);

        JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        restartGame();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

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
        onGameWon(!isWhite);  // Award win to the player who didn't run out of time
    }

    private String getAlgebraicNotation(Move move) {
        String notation;
        if (move.isCastling) {
            notation = move.toCol > move.fromCol ? "O-O" : "O-O-O";
        } else {
            notation = getPieceNotation(move.piece)
                    + (char) ('a' + move.toCol)
                    + (8 - move.toRow);
            if (move.isPromotion) {
                notation += "=Q";
            }
        }
        return notation;
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChessGame().setVisible(true);
        });
    }
}
