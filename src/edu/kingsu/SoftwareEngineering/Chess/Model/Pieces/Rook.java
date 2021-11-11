package edu.kingsu.SoftwareEngineering.Chess.Model.Pieces;

/**
 * This class represents a rook in a game of chess.
 */
public class Rook extends Piece {

    private boolean doneCastling;

    /**
     * Creates a new {@link Rook} instance.
     * @param isWhite a boolean which signifies the piece is white if <code>true</code>, black if <code>false</code>
     */
    public Rook(boolean isWhite) {
        super(isWhite);
        this.doneCastling = false;
    }

    /**
     * Creates a new {@link Rook} instance.
     * @param color a string representing the color of the piece.
     */
    public Rook(String color) {
        super(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Piece copyPiece() {
        Rook copy = new Rook(isWhite());
        copy.doneCastling = this.doneCastling;
        return copy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PieceType getPieceType() {
        return PieceType.ROOK;
    }

    /**
     * Returns a boolean representing whether this piece is finished castling.
     * @return <code>true</code> if this piece is done castling, and <code>false</code> if it is still able to castle
     */
    public boolean isDoneCastling() {
        return doneCastling;
    }

    /**
     * Sets the flag for this {@link Rook} to be finished performing castling moves
     */
    public void doneCastling() {
        doneCastling = true;
    }
}
