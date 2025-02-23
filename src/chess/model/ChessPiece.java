package chess.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Represents a chess piece with its type, color, and image.
 *
 * @author Do Duc Hai - CE192014
 */
public class ChessPiece {

    private BufferedImage image; // Image of the chess piece

    /**
     * The type of the chess piece (e.g., KING, QUEEN, etc.).
     */
    public PieceType type;

    /**
     * Indicates whether the piece is white or black.
     */
    public boolean isWhite;

    /**
     * Constructor to create a chess piece.
     *
     * @param type The type of the piece (e.g., KING, QUEEN, etc.).
     * @param isWhite True if the piece is white, false if black.
     */
    public ChessPiece(PieceType type, boolean isWhite) {
        this.type = type;
        this.isWhite = isWhite;
        loadImage(); // Load the image for the piece
    }

    /**
     * Loads the image for the chess piece from the resources folder.
     */
    private void loadImage() {
        // Construct the filename based on the piece type and color
        String filename = "/resources/" + (isWhite ? "white_" : "black_") + type.toString().toLowerCase() + ".png";
        try {
            // Load the image from the file
            image = ImageIO.read(getClass().getResource(filename));
        } catch (IOException | IllegalArgumentException e) {
            // Handle errors if the image cannot be loaded
            System.err.println("Could not load image: " + filename);
            image = null; // Set image to null if loading fails
        }
    }

    /**
     * Draws the chess piece on the board.
     *
     * @param g The Graphics object used for drawing.
     * @param x The x-coordinate of the piece's position.
     * @param y The y-coordinate of the piece's position.
     * @param size The size of the piece (width and height).
     */
    public void draw(Graphics g, int x, int y, int size) {
        if (image != null) {
            // Draw the image if it is loaded
            g.drawImage(image, x, y, size, size, null);
        } else {
            // Draw a colored square if the image is not available
            g.setColor(isWhite ? Color.WHITE : Color.BLACK);
            g.fillRect(x, y, size, size); // Draw a square as a fallback
        }
    }
}
