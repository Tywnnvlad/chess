package edu.kingsu.SoftwareEngineering.Chess.Model.Pieces;

/**
 * This class represents a pawn in a game of chess.
 */
public class Pawn extends Piece {

    private boolean doneDoubleMove;

    /**
     * Creates a new {@link Pawn} instance.
     * @param isWhite a boolean which signifies the piece is white if <code>true</code>, black if <code>false</code>
     */
    public Pawn(boolean isWhite) {
        super(isWhite);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PieceType getPieceType() {
        return PieceType.PAWN;
    }

    /**
     * Returns a boolean representing whether this piece is finished performing initial two-space moves.
     * @return <code>true</code> if this piece is finished performing double-moves, and <code>false</code> if it is still able to double-move
     */

}
