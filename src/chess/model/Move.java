package chess.model;

/**
 * Represents a move in the chess game. This class stores information about the
 * move, such as the starting and ending positions, the piece being moved, and
 * special move conditions like castling or promotion.
 *
 * @author Do Duc Hai - CE192014
 */
public class Move {

    /**
     * The starting row of the move.
     */
    public int fromRow;

    /**
     * The starting column of the move.
     */
    public int fromCol;

    /**
     * The ending row of the move.
     */
    public int toRow;

    /**
     * The ending column of the move.
     */
    public int toCol;

    /**
     * The chess piece being moved.
     */
    public ChessPiece piece;

    /**
     * The chess piece that is captured (if any).
     */
    public ChessPiece capturedPiece;

    /**
     * Indicates if the move is a castling move.
     */
    public boolean isCastling;

    /**
     * Indicates if the move is an en passant move.
     */
    public boolean isEnPassant;

    /**
     * Indicates if the move is a pawn promotion.
     */
    public boolean isPromotion;

    /**
     * Constructor to create a move.
     *
     * @param fromRow The starting row of the move.
     * @param fromCol The starting column of the move.
     * @param toRow The ending row of the move.
     * @param toCol The ending column of the move.
     * @param piece The chess piece being moved.
     * @param capturedPiece The chess piece that is captured (if any).
     * @param isCastling True if the move is castling, false otherwise.
     * @param isEnPassant True if the move is en passant, false otherwise.
     * @param isPromotion True if the move is a pawn promotion, false otherwise.
     */
    public Move(int fromRow, int fromCol, int toRow, int toCol, ChessPiece piece,
            ChessPiece capturedPiece, boolean isCastling, boolean isEnPassant, boolean isPromotion) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
        this.isPromotion = isPromotion;
    }
}
