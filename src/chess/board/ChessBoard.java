package chess.board;

import chess.ChessGame;
import chess.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * ChessBoard represents the graphical chessboard and manages game logic.
 *
 * @author Do Duc Hai - CE192014
 */
public class ChessBoard extends JPanel {

    private static final int BOARD_SIZE = 8; // Chessboard size (8x8)
    private static final int SQUARE_SIZE = 100; // Size of each square
    private ChessPiece[][] pieces; // 2D array to store chess pieces
    private ChessPiece selectedPiece; // Currently selected piece
    private int selectedRow = -1; // Row index of selected piece
    private int selectedCol = -1; // Column index of selected piece
    private final ChessGame game; // Reference to the ChessGame object
    private List<Move> moveHistory = new ArrayList<>(); // List of moves made
    private Set<ChessPiece> piecesWithFirstMove = new HashSet<>(); // Track pieces that moved for the first time
    private Position whiteKingPosition; // Position of the white king
    private Position blackKingPosition; // Position of the black king
    private Set<Position> possibleMoves = new HashSet<>(); // Possible moves for selected piece
    private boolean gameStarted = false; // Flag to check if the game has started

    /**
     * Sets the game state to started or not started.
     *
     * @param started true if the game has started, false otherwise
     */
    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        repaint(); // Refresh the board
    }

    /**
     * Constructor initializes the chessboard and adds a mouse listener.
     *
     * @param game The ChessGame instance managing the game
     */
    public ChessBoard(ChessGame game) {
        this.game = game;
        setPreferredSize(new Dimension(BOARD_SIZE * SQUARE_SIZE, BOARD_SIZE * SQUARE_SIZE));
        initializeBoard(); // Setup initial board state
        addMouseListener(new ChessBoardMouseListener()); // Add mouse listener for interactions
    }

    /**
     * Resets the board and restarts the game.
     */
    public void restartGame() {
        // Reset all game state
        pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        selectedPiece = null;
        selectedRow = -1;
        selectedCol = -1;
        moveHistory.clear();
        piecesWithFirstMove.clear();
        whiteKingPosition = null;
        blackKingPosition = null;
        possibleMoves.clear();

        // Reinitialize the board
        initializeBoard();
        repaint(); // Refresh the UI
    }

    /**
     * Initializes the board with all pieces in their starting positions.
     */
    public void initializeBoard() {
        pieces = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

        // Initialize pawns
        for (int i = 0; i < BOARD_SIZE; i++) {
            pieces[1][i] = new ChessPiece(PieceType.PAWN, false); // Black pawns
            pieces[6][i] = new ChessPiece(PieceType.PAWN, true);  // White pawns
        }

        // Initialize black pieces (back row)
        pieces[0][0] = new ChessPiece(PieceType.ROOK, false);
        pieces[0][1] = new ChessPiece(PieceType.KNIGHT, false);
        pieces[0][2] = new ChessPiece(PieceType.BISHOP, false);
        pieces[0][3] = new ChessPiece(PieceType.QUEEN, false);
        pieces[0][4] = new ChessPiece(PieceType.KING, false);
        pieces[0][5] = new ChessPiece(PieceType.BISHOP, false);
        pieces[0][6] = new ChessPiece(PieceType.KNIGHT, false);
        pieces[0][7] = new ChessPiece(PieceType.ROOK, false);

        // Initialize white pieces (back row)
        pieces[7][0] = new ChessPiece(PieceType.ROOK, true);
        pieces[7][1] = new ChessPiece(PieceType.KNIGHT, true);
        pieces[7][2] = new ChessPiece(PieceType.BISHOP, true);
        pieces[7][3] = new ChessPiece(PieceType.QUEEN, true);
        pieces[7][4] = new ChessPiece(PieceType.KING, true);
        pieces[7][5] = new ChessPiece(PieceType.BISHOP, true);
        pieces[7][6] = new ChessPiece(PieceType.KNIGHT, true);
        pieces[7][7] = new ChessPiece(PieceType.ROOK, true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);
    }

    /**
     * Draws the chessboard with alternating colors and highlights.
     *
     * @param g The graphics object to draw on
     */
    private void drawBoard(Graphics g) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Set square color (light and dark squares)
                Color squareColor = (row + col) % 2 == 0 ? new Color(216, 227, 232) : new Color(122, 157, 178);
                g.setColor(squareColor);
                g.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);

                // Draw possible move indicators (semi-transparent black circle)
                if (possibleMoves.contains(new Position(row, col))) {
                    g.setColor(new Color(0, 0, 0, 64));
                    int dotSize = SQUARE_SIZE / 4;
                    int dotX = col * SQUARE_SIZE + (SQUARE_SIZE - dotSize) / 2;
                    int dotY = row * SQUARE_SIZE + (SQUARE_SIZE - dotSize) / 2;
                    g.fillOval(dotX, dotY, dotSize, dotSize);
                }
            }
        }

        // Highlight the selected square (semi-transparent yellow)
        if (selectedRow != -1 && selectedCol != -1) {
            g.setColor(new Color(255, 255, 0, 128));
            g.fillRect(selectedCol * SQUARE_SIZE, selectedRow * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
    }

    /**
     * Draws chess pieces on the board.
     *
     * @param g The graphics object to draw on
     */
    private void drawPieces(Graphics g) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (pieces[row][col] != null) {
                    pieces[row][col].draw(g, col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE);
                }
            }
        }
    }

    /**
     * Checks if a move is valid based on piece rules and game constraints.
     *
     * @param fromRow The starting row
     * @param fromCol The starting column
     * @param toRow The target row
     * @param toCol The target column
     * @return true if the move is valid, false otherwise
     */
    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = pieces[fromRow][fromCol];

        if (piece == null) {
            return false; // No piece selected
        }

        // Ensure the target position is within bounds
        if (toRow < 0 || toRow >= BOARD_SIZE || toCol < 0 || toCol >= BOARD_SIZE) {
            return false;
        }

        // Prevent moving onto a friendly piece
        if (pieces[toRow][toCol] != null && pieces[toRow][toCol].isWhite == piece.isWhite) {
            return false;
        }

        boolean basicMoveValid = false;

        // Special case: Castling
        if (piece.type == PieceType.KING && !piecesWithFirstMove.contains(piece)) {
            if (isCastlingValid(fromRow, fromCol, toRow, toCol)) {
                basicMoveValid = true;
            }
        }

        // Special case: En Passant (pawn capture)
        if (piece.type == PieceType.PAWN) {
            if (isEnPassantValid(fromRow, fromCol, toRow, toCol)) {
                basicMoveValid = true;
            }
        }

        // Validate the move based on the piece type
        if (!basicMoveValid) {
            switch (piece.type) {
                case PAWN:
                    basicMoveValid = isValidPawnMove(fromRow, fromCol, toRow, toCol, piece.isWhite);
                    break;
                case ROOK:
                    basicMoveValid = isValidRookMove(fromRow, fromCol, toRow, toCol);
                    break;
                case KNIGHT:
                    basicMoveValid = isValidKnightMove(fromRow, fromCol, toRow, toCol);
                    break;
                case BISHOP:
                    basicMoveValid = isValidBishopMove(fromRow, fromCol, toRow, toCol);
                    break;
                case QUEEN:
                    basicMoveValid = isValidQueenMove(fromRow, fromCol, toRow, toCol);
                    break;
                case KING:
                    basicMoveValid = isValidKingMove(fromRow, fromCol, toRow, toCol);
                    break;
            }
        }

        if (!basicMoveValid) {
            return false; // The move is invalid
        }

        // Check if the piece is pinned (if moving it would expose the king to check)
        if (isPinned(fromRow, fromCol, toRow, toCol)) {
            return false;
        }

        // Create a temporary board to simulate the move
        ChessPiece[][] tempBoard = copyBoard();
        tempBoard[toRow][toCol] = tempBoard[fromRow][fromCol];
        tempBoard[fromRow][fromCol] = null;

        // After the move, check if the king is in check
        boolean kingInCheck = isInCheck(piece.isWhite, tempBoard);

        return !kingInCheck;
    }

    /**
     * Checks if the given king is in check.
     *
     * @param isWhiteKing true if checking for the white king, false for black
     * king
     * @param board the current chessboard state
     * @return true if the king is in check, false otherwise
     */
    private boolean isInCheck(boolean isWhiteKing, ChessPiece[][] board) {
        // Find the king's position
        Position kingPos = findKing(isWhiteKing, board);
        if (kingPos == null) {
            return false;
        }

        // Check if any opponent piece can attack the king
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece attacker = board[row][col];
                if (attacker != null && attacker.isWhite != isWhiteKing) {
                    if (canPieceAttack(row, col, kingPos.row, kingPos.col, attacker, board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the position of the king for the given color.
     *
     * @param isWhiteKing true if looking for the white king, false for black
     * king
     * @param board the current chessboard state
     * @return the position of the king, or null if not found
     */
    private Position findKing(boolean isWhiteKing, ChessPiece[][] board) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece != null && piece.type == PieceType.KING && piece.isWhite == isWhiteKing) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a given piece can attack a target position.
     *
     * @param fromRow attacker's row
     * @param fromCol attacker's column
     * @param toRow target row
     * @param toCol target column
     * @param attacker the attacking piece
     * @param board the chessboard state
     * @return true if the piece can attack the target position
     */
    private boolean canPieceAttack(int fromRow, int fromCol, int toRow, int toCol,
            ChessPiece attacker, ChessPiece[][] board) {
        switch (attacker.type) {
            case KNIGHT:
                // Knights move in an L-shape, no path checking needed
                int rowDiff = Math.abs(toRow - fromRow);
                int colDiff = Math.abs(toCol - fromCol);
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);

            case BISHOP:
                // Bishops move diagonally, path must be clear
                if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) {
                    return false;
                }
                return isPathClear(fromRow, fromCol, toRow, toCol, board);

            case ROOK:
                // Rooks move in straight lines, path must be clear
                if (fromRow != toRow && fromCol != toCol) {
                    return false;
                }
                return isPathClear(fromRow, fromCol, toRow, toCol, board);

            case QUEEN:
                // Queens move like rooks and bishops, path must be clear
                if (fromRow != toRow && fromCol != toCol
                        && Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) {
                    return false;
                }
                return isPathClear(fromRow, fromCol, toRow, toCol, board);

            case PAWN:
                // Pawns attack diagonally forward
                int direction = attacker.isWhite ? -1 : 1;
                return toRow == fromRow + direction && Math.abs(toCol - fromCol) == 1;

            case KING:
                // Kings attack one square in any direction
                return Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1;

            default:
                return false;
        }
    }

    /**
     * Checks if there are no pieces blocking the path between two positions.
     *
     * @param fromRow starting row
     * @param fromCol starting column
     * @param toRow target row
     * @param toCol target column
     * @param board the chessboard state
     * @return true if the path is clear, false otherwise
     */
    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol, ChessPiece[][] board) {
        int rowDir = Integer.compare(toRow, fromRow);
        int colDir = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowDir;
        int currentCol = fromCol + colDir;

        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != null) {
                return false; // Path is blocked
            }
            currentRow += rowDir;
            currentCol += colDir;
        }

        return true;
    }

    /**
     * Checks if a piece is pinned (cannot move without exposing the king to
     * check).
     *
     * @param fromRow piece's row
     * @param fromCol piece's column
     * @param toRow target row
     * @param toCol target column
     * @return true if the piece is pinned, false otherwise
     */
    private boolean isPinned(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = pieces[fromRow][fromCol];

        // The king cannot be pinned
        if (piece.type == PieceType.KING) {
            return false;
        }

        // Find the king of the same color
        int kingRow = -1, kingCol = -1;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                ChessPiece p = pieces[r][c];
                if (p != null && p.type == PieceType.KING && p.isWhite == piece.isWhite) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
            if (kingRow != -1) {
                break;
            }
        }

        // Check if the piece is between the king and an attacking piece
        int rowDiff = kingRow - fromRow;
        int colDiff = kingCol - fromCol;

        // Only check if the piece is in line with the king
        if (rowDiff == 0 || colDiff == 0 || Math.abs(rowDiff) == Math.abs(colDiff)) {
            int rowDir = Integer.compare(fromRow, kingRow);
            int colDir = Integer.compare(fromCol, kingCol);

            // Check for an attacking piece in the same line
            boolean foundAttacker = false;
            PieceType attackerType = null;

            // Scan from the king past the piece
            int row = kingRow + rowDir;
            int col = kingCol + colDir;
            boolean passedPiece = false;

            while (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                if (row == fromRow && col == fromCol) {
                    passedPiece = true;
                } else {
                    ChessPiece p = pieces[row][col];
                    if (p != null) {
                        if (!passedPiece) {
                            break; // Another piece is blocking
                        }
                        if (p.isWhite != piece.isWhite) {
                            foundAttacker = true;
                            attackerType = p.type;
                        }
                        break;
                    }
                }
                row += rowDir;
                col += colDir;
            }

            if (foundAttacker) {
                boolean isValidAttacker = false;
                if (rowDiff == 0 || colDiff == 0) {
                    isValidAttacker = (attackerType == PieceType.ROOK || attackerType == PieceType.QUEEN);
                } else if (Math.abs(rowDiff) == Math.abs(colDiff)) {
                    isValidAttacker = (attackerType == PieceType.BISHOP || attackerType == PieceType.QUEEN);
                }

                if (isValidAttacker) {
                    int newRowDiff = kingRow - toRow;
                    int newColDiff = kingCol - toCol;

                    if (rowDiff == 0) {
                        return newRowDiff != 0;
                    }
                    if (colDiff == 0) {
                        return newColDiff != 0;
                    }
                    return Math.abs(newRowDiff) != Math.abs(newColDiff)
                            || (newRowDiff * colDiff != newColDiff * rowDiff);
                }
            }
        }

        return false;
    }

    /**
     * Checks if the pawn move is valid.
     *
     * @param fromRow starting row
     * @param fromCol starting column
     * @param toRow target row
     * @param toCol target column
     * @param isWhite true if the pawn is white
     * @return true if the move is valid
     */
    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol, boolean isWhite) {
        int direction = isWhite ? -1 : 1;
        boolean isStartingPosition = (isWhite && fromRow == 6) || (!isWhite && fromRow == 1);

        // Moving straight forward
        if (fromCol == toCol) {
            // Move 1 square
            if (toRow == fromRow + direction && pieces[toRow][toCol] == null) {
                return true;
            }
            // Move 2 squares from starting position
            if (isStartingPosition && toRow == fromRow + 2 * direction
                    && pieces[toRow][toCol] == null
                    && pieces[fromRow + direction][toCol] == null) {
                return true;
            }
        }

        // Capturing diagonally
        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction) {
            // Normal capture
            if (pieces[toRow][toCol] != null && pieces[toRow][toCol].isWhite != isWhite) {
                return true;
            }
            // En passant capture
            if (pieces[toRow][toCol] == null && isEnPassantValid(fromRow, fromCol, toRow, toCol)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the rook move is valid.
     */
    private boolean isValidRookMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }

        // Check if path is clear
        int rowDirection = Integer.compare(toRow, fromRow);
        int colDirection = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowDirection;
        int currentCol = fromCol + colDirection;

        while (currentRow != toRow || currentCol != toCol) {
            if (pieces[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowDirection;
            currentCol += colDirection;
        }

        return true;
    }

    /**
     * Checks if the knight move is valid.
     */
    private boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }

    /**
     * Checks if the bishop move is valid.
     */
    private boolean isValidBishopMove(int fromRow, int fromCol, int toRow, int toCol) {
        if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) {
            return false;
        }

        // Check if path is clear
        int rowDirection = Integer.compare(toRow, fromRow);
        int colDirection = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowDirection;
        int currentCol = fromCol + colDirection;

        while (currentRow != toRow && currentCol != toCol) {
            if (pieces[currentRow][currentCol] != null) {
                return false;
            }
            currentRow += rowDirection;
            currentCol += colDirection;
        }

        return true;
    }

    /**
     * Checks if the queen move is valid.
     */
    private boolean isValidQueenMove(int fromRow, int fromCol, int toRow, int toCol) {
        return isValidRookMove(fromRow, fromCol, toRow, toCol)
                || isValidBishopMove(fromRow, fromCol, toRow, toCol);
    }

    /**
     * Checks if the king move is valid.
     */
    private boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece king = pieces[fromRow][fromCol];
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Castling check - Ensure the king has not moved before
        if (colDiff == 2 && rowDiff == 0 && !piecesWithFirstMove.contains(king)) {
            return isCastlingValid(fromRow, fromCol, toRow, toCol);
        }

        // Normal king move - one square in any direction
        if (rowDiff > 1 || colDiff > 1) {
            return false;
        }

        // Create a temporary board state
        ChessPiece[][] tempBoard = copyBoard();
        tempBoard[toRow][toCol] = tempBoard[fromRow][fromCol];
        tempBoard[fromRow][fromCol] = null;

        // Check if the move puts the king in check
        return !isInCheck(king.isWhite, tempBoard);
    }

    /**
     * Checks if the king is in checkmate.
     */
    private boolean isCheckmate(boolean isWhiteKing) {
        if (!isInCheck(isWhiteKing, pieces)) {
            return false;
        }

        // Check all possible moves of the same color pieces
        for (int fromRow = 0; fromRow < BOARD_SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < BOARD_SIZE; fromCol++) {
                ChessPiece piece = pieces[fromRow][fromCol];
                if (piece != null && piece.isWhite == isWhiteKing) {
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                                // Try the move
                                ChessPiece[][] tempBoard = copyBoard();
                                tempBoard[toRow][toCol] = tempBoard[fromRow][fromCol];
                                tempBoard[fromRow][fromCol] = null;

                                if (!isInCheck(isWhiteKing, tempBoard)) {
                                    return false; // Found a move that escapes check
                                }
                            }
                        }
                    }
                }
            }
        }
        return true; // No valid move to escape check
    }

    /**
     * Checks if castling is a valid move.
     */
    private boolean isCastlingValid(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece king = pieces[fromRow][fromCol];

        // Check if the king or rook has moved before
        if (piecesWithFirstMove.contains(king)) {
            return false;
        }

        // Determine the rook's position
        int rookCol = toCol > fromCol ? 7 : 0;
        ChessPiece rook = pieces[fromRow][rookCol];

        // Ensure the rook exists, is of correct type, and hasn't moved
        if (rook == null || rook.type != PieceType.ROOK || piecesWithFirstMove.contains(rook)) {
            return false;
        }

        // Check if the king is in check
        if (isInCheck(king.isWhite, pieces)) {
            return false;
        }

        // Check that the path is clear and safe
        int direction = toCol > fromCol ? 1 : -1;
        for (int col = fromCol + direction; col != rookCol; col += direction) {
            if (pieces[fromRow][col] != null) {
                return false;
            }

            // Ensure passing squares are not under attack
            ChessPiece[][] tempBoard = copyBoard();
            tempBoard[fromRow][col] = king;
            tempBoard[fromRow][fromCol] = null;

            if (isInCheck(king.isWhite, tempBoard)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if en passant is a valid move.
     */
    private boolean isEnPassantValid(int fromRow, int fromCol, int toRow, int toCol) {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.get(moveHistory.size() - 1);
        ChessPiece movingPawn = pieces[fromRow][fromCol];

        // Validate en passant conditions:
        // 1. The last move was a pawn
        // 2. The pawn moved two squares forward
        // 3. The opponent's pawn is in the correct position
        // 4. The move must be done immediately
        return lastMove.piece.type == PieceType.PAWN
                && Math.abs(lastMove.toRow - lastMove.fromRow) == 2
                && lastMove.toCol == toCol
                && ((movingPawn.isWhite && fromRow == 3) || (!movingPawn.isWhite && fromRow == 4))
                && Math.abs(fromCol - toCol) == 1
                && lastMove.toRow == fromRow;
    }

    /**
     * Handles pawn promotion when it reaches the last row.
     */
    private void handlePromotion(int row, int col) {
        ChessPiece pawn = pieces[row][col];
        if (pawn.type == PieceType.PAWN && ((pawn.isWhite && row == 0) || (!pawn.isWhite && row == 7))) {

            String[] options = {"Queen", "Rook", "Bishop", "Knight"};
            int choice = JOptionPane.showOptionDialog(this,
                    "Choose promotion piece:",
                    "Pawn Promotion",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            PieceType newType;
            switch (choice) {
                case 0:
                    newType = PieceType.QUEEN;
                    break;
                case 1:
                    newType = PieceType.ROOK;
                    break;
                case 2:
                    newType = PieceType.BISHOP;
                    break;
                case 3:
                    newType = PieceType.KNIGHT;
                    break;
                default:
                    newType = PieceType.QUEEN;
                    break;
            }

            // Promote the pawn to the chosen piece
            pieces[row][col] = new ChessPiece(newType, pawn.isWhite);
        }
    }

    /**
     * Creates a copy of the chessboard.
     */
    private ChessPiece[][] copyBoard() {
        ChessPiece[][] newBoard = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (pieces[i][j] != null) {
                    newBoard[i][j] = new ChessPiece(pieces[i][j].type, pieces[i][j].isWhite);
                }
            }
        }
        return newBoard;
    }

    /**
     * Checks if the game is in stalemate.
     */
    private boolean isStalemate(boolean isWhitePlayer) {
        // If the king is in check, it's not a stalemate
        if (isInCheck(isWhitePlayer, pieces)) {
            return false;
        }

        // Check if the player has any valid moves left
        for (int fromRow = 0; fromRow < BOARD_SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < BOARD_SIZE; fromCol++) {
                ChessPiece piece = pieces[fromRow][fromCol];
                if (piece != null && piece.isWhite == isWhitePlayer) {
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
                            if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Handles a move from one position to another. Checks for castling, en
     * passant, and promotion. Updates the board and game state accordingly.
     */
    private void handleMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece movingPiece = pieces[fromRow][fromCol];
        ChessPiece capturedPiece = pieces[toRow][toCol];
        boolean isCastling = false;
        boolean isEnPassant = false;
        boolean isPromotion = false;

        // Handle castling move
        if (movingPiece.type == PieceType.KING && Math.abs(toCol - fromCol) == 2) {
            int rookFromCol = toCol > fromCol ? 7 : 0;
            int rookToCol = toCol > fromCol ? toCol - 1 : toCol + 1;
            pieces[toRow][rookToCol] = pieces[toRow][rookFromCol];
            pieces[toRow][rookFromCol] = null;
            isCastling = true;
        }

        // Handle en passant capture
        if (movingPiece.type == PieceType.PAWN && fromCol != toCol && pieces[toRow][toCol] == null) {
            pieces[fromRow][toCol] = null;
            isEnPassant = true;
        }

        // Move the piece to the new position
        pieces[toRow][toCol] = movingPiece;
        pieces[fromRow][fromCol] = null;

        // Update king position if the moved piece is a king
        if (movingPiece.type == PieceType.KING) {
            if (movingPiece.isWhite) {
                whiteKingPosition = new Position(toRow, toCol);
            } else {
                blackKingPosition = new Position(toRow, toCol);
            }
        }

        // Handle pawn promotion
        if (movingPiece.type == PieceType.PAWN && (toRow == 0 || toRow == 7)) {
            handlePromotion(toRow, toCol);
            isPromotion = true;
        }

        // Record first move if applicable
        if (!piecesWithFirstMove.contains(movingPiece)) {
            piecesWithFirstMove.add(movingPiece);
        }

        // Save move to history
        Move move = new Move(fromRow, fromCol, toRow, toCol,
                movingPiece, capturedPiece, isCastling,
                isEnPassant, isPromotion);
        moveHistory.add(move);
        game.addMoveToHistory(move);

        // Check for game-ending conditions
        boolean isOpponentInCheck = isInCheck(!movingPiece.isWhite, pieces);

        if (isCheckmate(!movingPiece.isWhite)) {
            // Call onGameWon before showing dialog
            game.onGameWon(movingPiece.isWhite);
        } else if (isStalemate(!movingPiece.isWhite)) {
            // For stalemate, we could either:
            // 1. Count it as a draw and restart the game
            // 2. Give the win to the player who didn't cause the stalemate
            // Here we're implementing option 1
            JOptionPane.showMessageDialog(null, "Stalemate! The game is a draw!");
            game.restartGame(); // Restart the game but keep the match score
        } else if (isOpponentInCheck) {
            // Just notify about check
            JOptionPane.showMessageDialog(null,
                    (movingPiece.isWhite ? "Black" : "White") + " is in check!");
        }
    }

    /**
     * Calculates all possible moves for a given piece. Considers pins, check
     * situations, and piece type movement rules.
     */
    private void calculatePossibleMoves(int row, int col) {
        possibleMoves.clear();
        ChessPiece piece = pieces[row][col];
        if (piece == null) {
            return;
        }

        // First, find all theoretical moves based on piece type
        Set<Position> theoreticalMoves = new HashSet<>();

        switch (piece.type) {
            case PAWN:
                addPawnTheoriticalMoves(row, col, piece.isWhite, theoreticalMoves);
                break;
            case KNIGHT:
                addKnightTheoriticalMoves(row, col, piece.isWhite, theoreticalMoves);
                break;
            case BISHOP:
                addBishopTheoriticalMoves(row, col, piece.isWhite, theoreticalMoves);
                break;
            case ROOK:
                addRookTheoriticalMoves(row, col, piece.isWhite, theoreticalMoves);
                break;
            case QUEEN:
                addBishopTheoriticalMoves(row, col, piece.isWhite, theoreticalMoves);
                addRookTheoriticalMoves(row, col, piece.isWhite, theoreticalMoves);
                break;
            case KING:
                addKingTheoriticalMoves(row, col, piece.isWhite, theoreticalMoves);
                break;
        }

        // Then filter moves that would leave/put the king in check
        for (Position move : theoreticalMoves) {
            if (isValidMove(row, col, move.row, move.col)) {
                possibleMoves.add(move);
            }
        }
    }

    private void addPawnTheoriticalMoves(int row, int col, boolean isWhite, Set<Position> moves) {
        int direction = isWhite ? -1 : 1;

        // Forward move
        if (isWithinBounds(row + direction, col) && pieces[row + direction][col] == null) {
            moves.add(new Position(row + direction, col));

            // Initial two-square move
            if ((isWhite && row == 6) || (!isWhite && row == 1)) {
                if (pieces[row + 2 * direction][col] == null) {
                    moves.add(new Position(row + 2 * direction, col));
                }
            }
        }

        // Captures
        for (int deltaCol : new int[]{-1, 1}) {
            int newRow = row + direction;
            int newCol = col + deltaCol;

            if (isWithinBounds(newRow, newCol)) {
                // Normal capture
                if (pieces[newRow][newCol] != null && pieces[newRow][newCol].isWhite != isWhite) {
                    moves.add(new Position(newRow, newCol));
                }

                // En passant
                if (canEnPassant(row, col, newRow, newCol, isWhite)) {
                    moves.add(new Position(newRow, newCol));
                }
            }
        }
    }

    private void addKnightTheoriticalMoves(int row, int col, boolean isWhite, Set<Position> moves) {
        int[][] knightMoves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];

            if (isWithinBounds(newRow, newCol)) {
                if (pieces[newRow][newCol] == null || pieces[newRow][newCol].isWhite != isWhite) {
                    moves.add(new Position(newRow, newCol));
                }
            }
        }
    }

    private void addBishopTheoriticalMoves(int row, int col, boolean isWhite, Set<Position> moves) {
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int currentRow = row + dir[0];
            int currentCol = col + dir[1];

            while (isWithinBounds(currentRow, currentCol)) {
                if (pieces[currentRow][currentCol] == null) {
                    moves.add(new Position(currentRow, currentCol));
                } else {
                    if (pieces[currentRow][currentCol].isWhite != isWhite) {
                        moves.add(new Position(currentRow, currentCol));
                    }
                    break;
                }
                currentRow += dir[0];
                currentCol += dir[1];
            }
        }
    }

    private void addRookTheoriticalMoves(int row, int col, boolean isWhite, Set<Position> moves) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] dir : directions) {
            int currentRow = row + dir[0];
            int currentCol = col + dir[1];

            while (isWithinBounds(currentRow, currentCol)) {
                if (pieces[currentRow][currentCol] == null) {
                    moves.add(new Position(currentRow, currentCol));
                } else {
                    if (pieces[currentRow][currentCol].isWhite != isWhite) {
                        moves.add(new Position(currentRow, currentCol));
                    }
                    break;
                }
                currentRow += dir[0];
                currentCol += dir[1];
            }
        }
    }

    private void addKingTheoriticalMoves(int row, int col, boolean isWhite, Set<Position> moves) {
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
        };

        // Normal moves
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isWithinBounds(newRow, newCol)) {
                if (pieces[newRow][newCol] == null || pieces[newRow][newCol].isWhite != isWhite) {
                    moves.add(new Position(newRow, newCol));
                }
            }
        }

        // Castling moves
        if (!piecesWithFirstMove.contains(pieces[row][col])) {
            // Check kingside castling
            if (canCastle(row, col, true, isWhite)) {
                moves.add(new Position(row, col + 2));
            }
            // Check queenside castling
            if (canCastle(row, col, false, isWhite)) {
                moves.add(new Position(row, col - 2));
            }
        }
    }

    private boolean isWithinBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private boolean canEnPassant(int fromRow, int fromCol, int toRow, int toCol, boolean isWhite) {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.get(moveHistory.size() - 1);
        return pieces[fromRow][toCol] != null
                && pieces[fromRow][toCol].type == PieceType.PAWN
                && pieces[fromRow][toCol].isWhite != isWhite
                && lastMove.piece == pieces[fromRow][toCol]
                && Math.abs(lastMove.fromRow - lastMove.toRow) == 2
                && lastMove.toCol == toCol
                && ((isWhite && fromRow == 3) || (!isWhite && fromRow == 4));
    }

    /**
     * Checks if castling is possible for the king.
     *
     * @param row The row position of the king
     * @param col The column position of the king
     * @param kingSide True for kingside castling, false for queenside
     * @param isWhite True if the piece is white, false otherwise
     * @return True if castling is possible, false otherwise
     */
    private boolean canCastle(int row, int col, boolean kingSide, boolean isWhite) {
        int rookCol = kingSide ? 7 : 0; // Column where the rook is located
        ChessPiece rook = pieces[row][rookCol];

        // Check if the rook exists and hasn't moved
        if (rook == null || rook.type != PieceType.ROOK || piecesWithFirstMove.contains(rook)) {
            return false;
        }

        // Check if the path between king and rook is clear
        int direction = kingSide ? 1 : -1;
        int endCol = kingSide ? rookCol - 1 : rookCol + 1;

        for (int c = col + direction; c != endCol + direction; c += direction) {
            if (pieces[row][c] != null) {
                return false;
            }
            // Also check if the king would pass through an attacked square
            ChessPiece[][] tempBoard = copyBoard();
            tempBoard[row][c] = tempBoard[row][col]; // Simulate the king moving
            tempBoard[row][col] = null;
            if (isInCheck(isWhite, tempBoard)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Handles mouse clicks for selecting and moving pieces.
     */
    private class ChessBoardMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            // Ignore clicks if the game hasn't started
            if (!gameStarted) {
                return;
            }

            int col = e.getX() / SQUARE_SIZE; // Get the column based on click position
            int row = e.getY() / SQUARE_SIZE; // Get the row based on click position

            if (selectedPiece == null) {
                // Select a piece if it belongs to the current player
                if (pieces[row][col] != null && pieces[row][col].isWhite == game.isWhiteTurn) {
                    selectedPiece = pieces[row][col];
                    selectedRow = row;
                    selectedCol = col;
                    calculatePossibleMoves(row, col); // Highlight possible moves
                }
            } else {
                // Attempt to move the selected piece
                if (isValidMove(selectedRow, selectedCol, row, col)) {
                    handleMove(selectedRow, selectedCol, row, col);
                }
                // Deselect the piece after move attempt
                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;
                possibleMoves.clear();
            }
            repaint(); // Refresh the board display
        }
    }

    /**
     * Represents a position on the chessboard.
     */
    private static class Position {

        int row, col;

        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Position position = (Position) o;
            return row == position.row && col == position.col;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
}

// thêm tính năng lưu những trận đã đấu
// thêm tính năng undo tối đa 3 lần
// thêm tiếng ăn cờ, đi cờ
// thêm đã ăn con gì cộng điểm lợi thế
