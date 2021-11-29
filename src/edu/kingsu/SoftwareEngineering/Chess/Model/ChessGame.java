package edu.kingsu.SoftwareEngineering.Chess.Model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import edu.kingsu.SoftwareEngineering.Chess.GUI.ChessGameView;
import edu.kingsu.SoftwareEngineering.Chess.GUI.GameSetUp;
import edu.kingsu.SoftwareEngineering.Chess.GUI.ClockView;
import edu.kingsu.SoftwareEngineering.Chess.Model.Moves.*;
import edu.kingsu.SoftwareEngineering.Chess.Model.Pieces.*;
import edu.kingsu.SoftwareEngineering.Chess.PGN.*;

/**
 * This class fully represents a game of chess involving all its components.
 */
public class ChessGame {
    /**
     * The number of milliseconds in a second.
     */
    public static final int SECOND = 1000;
    // public static final int CLOCK_TICKS_PER_SECOND = 10;
    // private int clockTickBuffer;
    private Map<String, String> tagPairs;
    private Player whitePlayer;
    private Thread whitePlayerThread;
    private Player blackPlayer;
    private Thread blackPlayerThread;
    private Board board;
    private int playerInterval;
    private int playerIncrement;
    private Player playerTurn;
    private final Object playerTurnLock;
    private List<Move> moveHistory;
    private List<String> algebraicHistory;
    private int moveNo;
    private List<ChessGameView> views;
    private static final int HINT_THREAD_ATTEMPT_MAX = 10;
    private static final int HINT_THREAD_ATTEMPT_SLEEP = 1000;
    private GameState currentState;

    // Overall game time
    private int interval;
    private Timer timer;
    private ClockView totalGameTimeClock;
    private boolean totalClockOn = true;

    /**
     * Creates a new chess game.
     * @param whiteAI an integer determining the level AI of the white player.
     *  negative one signifies a human player, zero is a random player, and
     *  positive values indicate an AI with the given minimax depth of search
     * @param blackAI an integer determining the level AI of the black player.
     *  negative one signifies a human player, zero is a random player, and
     *  positive values indicate an AI with the given minimax depth of search
     * @param playerInterval the maximum time allotted to player's clocks
     * @param playerIncrement the increment of time given to player's clocks
     *  upon making a move
     */
    public ChessGame(int whiteAI, int blackAI, int playerInterval, int playerIncrement) {
        tagPairs = new LinkedHashMap<String,String>();
        playerTurnLock = new Object();
        views = new ArrayList<ChessGameView>();
        initialize(whiteAI, blackAI, playerInterval, playerIncrement);
        // initialize(whiteAI, blackAI, 100, 15);
        // clockTickBuffer = 0;
        timer = new Timer();
        interval = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                setInterval();
            }
        }, SECOND, SECOND);
    }

    /**
     * Initializes the chess game using default values. A white human player
     * plays against a black random AI player with no time controls.
     */
    public void initialize() {
        initialize(-1, 0, -1, -1);
    }

    /**
     * Initializes the chess game.
     * @param whiteAI an integer determining the level AI of the white player.
     *  negative one signifies a human player, zero is a random player, and
     *  positive values indicate an AI with the given minimax depth of search
     * @param blackAI an integer determining the level AI of the black player.
     *  negative one signifies a human player, zero is a random player, and
     *  positive values indicate an AI with the given minimax depth of search
     * @param playerInterval the maximum time allotted to player's clocks
     * @param playerIncrement the increment of time given to player's clocks
     *  upon making a move
     */
    public void initialize(int whiteAI, int blackAI, int playerInterval, int playerIncrement) {
        moveNo = 0;
        moveHistory = new ArrayList<Move>();
        algebraicHistory = new ArrayList<String>();
        this.playerInterval = playerInterval;
        this.playerIncrement = playerIncrement;
        if (whiteAI == -1) {
            whitePlayer = new PlayerHuman(this, true, playerInterval);
        } else {
            whitePlayer = new PlayerAI(this, true, playerInterval, whiteAI);
        }
        if (blackAI == -1) {
            blackPlayer = new PlayerHuman(this, false, playerInterval);
        } else {
            blackPlayer = new PlayerAI(this, false, playerInterval, blackAI);
        }
        playerTurn = whitePlayer;
        // whitePlayerThread = new Thread(whitePlayer);
        // blackPlayerThread = new Thread(blackPlayer);
        board = new Board();
        updateState();
    }

    /**
     * Returns a 2-dimensional array of characters indicating the placement of
     * pieces on the board. See {@link Board#getChars} for details.
     */
    public char[][] getBoardChars() { return board.getChars(); }

    /**
     * Returns the maximum time allotted to player's clocks.
     * @return the maximum time allotted to player's clocks
     */
    public int getPlayerInterval() { return playerInterval; }

    /**
     * Returns the time allotted to player's clocks upon making moves.
     * @return the time allotted to player's clocks upon making moves
     */
    public int getPlayerIncrement() { return playerIncrement; }

    /**
     * Returns the map of PGN tag pairs associated with this game.
     * @return the map of PGN tag pairs associated with this game
     */
    public Map<String, String> tagPairMap() { return tagPairs; }

    /**
     * Returns the board associated with this game.
     * @return the board associated with this game
     */
    public Board getBoard() { return board; }

    /**
     * Returns the player whose turn it currently is.
     * @return the player whose turn it currently is
     */
    public Player getPlayerTurn() {
        synchronized (playerTurnLock) {
            return playerTurn;
        }
    }

    /**
     * Forcibly sets the player whose turn it currently is.
     * @param toWhite <code>true</code> if the current turn should be given
     *  to white, <code>false</code> if black
     */
    public void forceSetPlayerTurn(boolean toWhite) {
        synchronized (playerTurnLock) {
            playerTurn = toWhite ? whitePlayer : blackPlayer;
        }
    }

    /**
     * Returns the list of moves performed in this game, in order of occurrence.
     * @return the list of moves performed in this game, in order of occurrence.
     */
    public List<Move> getMoveHistory() { return moveHistory; }

    /**
     * Returns the list of PGN-format moves performed in this game, in order of occurrence.
     * @return the list of PGN-format moves performed in this game, in order of occurrence.
     */
    public List<String> getAlgebraicHistory() { return algebraicHistory; }

    /**
     * Returns the current state of the game.
     * @return the current state of the game.
     */
    public GameState getState() {
        return currentState;
    }

    /**
     * Updates the current state of the game.
     */
    public void updateState() {
        boolean isWhiteTurn = getPlayerTurn().isWhite();
        if (board.getCheckmate(!isWhiteTurn)) {
            currentState = isWhiteTurn == true ? GameState.BLACK_CHECKMATE : GameState.WHITE_CHECKMATE;
        } else if (board.getCheck(!isWhiteTurn)) {
            currentState = isWhiteTurn == true ? GameState.BLACK_CHECK : GameState.WHITE_CHECK;
        } else if (board.getAllMoves(isWhiteTurn).isEmpty()) {
            currentState = GameState.STALEMATE_NOMOVES;
        } else {
            boolean sufficientMaterial = false;
            int whiteBishopStatus = -1;
            boolean whiteKnightExists = false;
            int blackBishopStatus = -1;
            boolean blackKnightExists = false;
            for (int i=0; !sufficientMaterial && i<Board.ROWS; i++) {
                for (int j=0; !sufficientMaterial && j<Board.ROWS; j++) {
                    Piece aPiece = getBoard().getPiece(i, j);
                    if (aPiece == null) continue;
                    if (aPiece.getPieceType() == PieceType.QUEEN || aPiece.getPieceType() == PieceType.ROOK || aPiece.getPieceType() == PieceType.PAWN) {
                        sufficientMaterial = true;
                    } else if (aPiece.getPieceType() == PieceType.BISHOP) {
                        if (aPiece.isWhite()) {
                            sufficientMaterial = whiteKnightExists || whiteBishopStatus != -1 && whiteBishopStatus != (i+j)%2;
                            whiteBishopStatus = (i+j)%2;
                        } else {
                            sufficientMaterial = blackKnightExists || blackBishopStatus != -1 && blackBishopStatus != (i+j)%2;
                            blackBishopStatus = (i+j)%2;
                        }
                    } else if (aPiece.getPieceType() == PieceType.KNIGHT) {
                        if (aPiece.isWhite()) {
                            whiteKnightExists = true;
                        } else {
                            blackKnightExists = true;
                        }
                    }
                }
            }
            if (sufficientMaterial) {
                currentState = GameState.ACTIVE;
            } else {
                currentState = GameState.STALEMATE_NOMATERIAL;
            }
        }
    }

    /**
     * Registers a view with the board, to be notified of changes.
     * @param view the view to register with this game of chess
     */
    public void registerView(ChessGameView view) {
        views.add(view);
    }

    /**
     * Notifies all registered views to re-query the state of the board.
     */
    public void notifyViews() {
        for (ChessGameView view : views) {
            view.update();
        }
    }

    /**
     * Attempts to perform a move in this game of chess.
     * @param rowFrom the row from which to move a piece
     * @param colFrom the col from which to move a piece
     * @param rowTo the row to which to move a piece
     * @param colTo the col to which to move a piece
     * @param humanMoveMaker <code>true</code> if this move is attempted to be
     *  made by a human, <code>false</code> if AI
     * @return <code>true</code> if the move attempt is successful,
     *  <code>false</code> otherwise
     */
    public boolean performMove(int rowFrom, int colFrom, int rowTo, int colTo, boolean humanMoveMaker) {
        return performMove(rowFrom, colFrom, rowTo, colTo, humanMoveMaker, null);
    }

    /**
     * Attempts to perform a move in this game of chess.
     * @param rowFrom the row from which to move a piece
     * @param colFrom the col from which to move a piece
     * @param rowTo the row to which to move a piece
     * @param colTo the col to which to move a piece
     * @param humanMoveMaker <code>true</code> if this move is attempted to be
     * @param promotionType the type of piece to promote a pawn to
     *  made by a human, <code>false</code> if AI
     * @return <code>true</code> if the move attempt is successful,
     *  <code>false</code> otherwise
     */
    public boolean performMove(int rowFrom, int colFrom, int rowTo, int colTo, boolean humanMoveMaker, PieceType promotionType) {
        for (Move m : getBoard().getMoves(rowFrom, colFrom)) {
            if (m.hasDestination(rowTo, colTo)) {
                if ((promotionType == null) || (m.getType() == MoveType.PAWN_PROMOTION && ((PawnPromotionMove)m).getPromotionType() == promotionType)) {
                    return performMove(m, humanMoveMaker);
                }
            }
        }
        return false;
    }

    /**
     * Attempts to perform a move in this game of chess.
     * @param move the move to perform in this game of chess
     * @param humanMoveMaker <code>true</code> if this move is attempted to be
     *  made by a human, <code>false</code> if AI
     * @return <code>true</code> if the move attempt is successful,
     *  <code>false</code> otherwise
     */
    public  boolean performMove(Move move, boolean humanMoveMaker) {
        if (humanMoveMaker != getPlayerTurn().isHuman()) {
            return false;
        }
        synchronized (this) {
            if (validateMove(move)) {
                synchronized (playerTurnLock) {
                    playerTurn.incrementTimer();
                    performMove(move);
                    updateState();
                    // System.err.println("GameState is : "+getState().name());
                    // System.err.println("Board CM for white is : "+board.getCheckmate(true));
                    // // System.err.println("Board all moves black is : "+board.getAllMoves(false));
                    // List<Move> ay = board.getAllMoves(false);
                    // Move yo = ay.size()>0 ? ay.get(0) : null;
                    // System.err.println("Board first move black is : "+yo.getRowFrom()+" "+yo.getColFrom()+" "+yo.getRowTo()+" "+yo.getColTo());
                    if (getState() != GameState.ACTIVE && getState() != GameState.WHITE_CHECK && getState() != GameState.BLACK_CHECK) {
                        gameOver();
                    }
                    notifyViews();
                }
                this.notifyAll();
                // System.err.println("Move made & all notified");
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Attempts to perform a move in this game of chess.
     * @param movePgn the PGN-format string corresponding to the move to be
     *  made in this game of chess
     * @param humanMoveMaker <code>true</code> if this move is attempted to be
     *  made by a human, <code>false</code> if AI
     * @return <code>true</code> if the move attempt is successful,
     *  <code>false</code> otherwise
     */
    public boolean performMove(String movePgn, boolean humanMoveMaker) {
        Move move = null;
        try {
            move = PGNTranslator.translatePGNToMove(movePgn, board, getPlayerTurn().isWhite());
        } catch(Exception e){
            return false;
        }
        return performMove(move, humanMoveMaker);
    }

    /**
     * Performs a move on the chess board.
     * @param addToHistory <code>true</code> if the given move should be added to
     *  the history list of moves, <code>false</code> otherwise
     * @param move the move to perform on the board
     */
    private void performMove(Move move) {
        // if (addToHistory) {
        //     if (moveNo < moveHistory.size()) {
        //         moveHistory = moveHistory.subList(0, moveNo);
        //         algebraicHistory = algebraicHistory.subList(0, moveNo);
        //     }
        //     moveHistory.add(move);
        //     algebraicHistory.add(PGNTranslator.translateMoveToPGN(move, board));
        // }
        boolean appendToHistory = true;
        if (0 <= moveNo && moveNo < moveHistory.size()) {
            // the history position tracker is in the middle of our move history
            if (moveHistory.get(moveNo).equals(move)) {
                // redoing the next move in our list
                appendToHistory = false;
            } else {
                // rewrite history
                appendToHistory = true;
                moveHistory = moveHistory.subList(0, moveNo);
                algebraicHistory = algebraicHistory.subList(0, moveNo);
            }
        }
        if (appendToHistory) {
            moveHistory.add(move);
            algebraicHistory.add(PGNTranslator.translateMoveToPGN(move, board));
        }
        move.perform(board);
        moveNo++;
        // (new Thread(new Runnable() {
        //     public void run() {
        //         playerTurn.stop();
        //     }
        // })).start();
        playerTurn.resetAIThread();
        // playerTurn.notifyAll();
        playerTurn = playerTurn == whitePlayer ? blackPlayer : whitePlayer;
    }

    /**
     * Returns a boolean indicating whether the given move is currently valid
     * in this game of chess.
     * @param move the move to be checked for validity
     * @return <code>true</code> if the given move is currently valid,
     *  <code>false</code> if it is not
     */
    private boolean validateMove(Move move) {
        return (move != null) && (board.getPiece(move.getRowFrom(), move.getColFrom()).isWhite() == getPlayerTurn().isWhite()) && (board.getMoves(move.getRowFrom(), move.getColFrom()).contains(move));
    }

    /**
     * Begins the player threads for this game of chess, such that clocks begin
     * and AI players move when it's their turn.
     */
    public void start() {
        synchronized (this) {
            notifyViews();
            whitePlayer.pauseTimer();
            whitePlayerThread = new Thread(whitePlayer);
            whitePlayerThread.start();
            blackPlayer.pauseTimer();
            blackPlayerThread = new Thread(blackPlayer);
            blackPlayerThread.start();
            this.notifyAll();
        }
    }

    /**
     * Stops the player threads for this game of chess, such that clocks do not
     * tick down, and AI players do not move on their turn.
     */
    public void stop() {
        if (whitePlayerThread != null) {
            whitePlayerThread.interrupt();
            whitePlayerThread = null;
        }
        if (blackPlayerThread != null) {
            blackPlayerThread.interrupt();
            blackPlayerThread = null;
        }
    }

    /**
     * Ends this game of chess according to the current game state.
     */
    public void gameOver() {
        stop();
        whitePlayer.pauseTimer();
        blackPlayer.pauseTimer();
        notifyViews();
    }

    /**
     * Increments the total time game timer.
     */
    private void setInterval() {
        interval++;
        if (totalGameTimeClock != null) {
            totalGameTimeClock.updateTotalGameTime(GameSetUp.getMinAndSec(interval));
        }
    }

    /**
     * Undoes the most recent move in this game of chess.
     * @return <code>true</code> if a move was successfully undone, and
     *  <code>false</code> if unsuccessful, such as due to their being no
     *  moves to undo
     */
    public boolean undo() {
        if (playerIncrement >= 0 || (!whitePlayer.isHuman() && !blackPlayer.isHuman())) {
            return false;
        }
        synchronized(this) {
            Player otherPlayer = playerTurn==whitePlayer ? blackPlayer : whitePlayer;
            int endMoveNo = moveNo-1;
            if (!otherPlayer.isHuman()) {
                endMoveNo--;
            }
            if (endMoveNo < 0) {
                return false;
            }
            stop();
            board = new Board();
            moveNo = 0;
            synchronized(playerTurnLock) {
                playerTurn = whitePlayer;
            }
            for (int i=0; i<endMoveNo; i++) {
                performMove(moveHistory.get(i));
            }
        }
        if (currentState != GameState.ACTIVE) {
            updateState();
        }
        start();
        return true;
    }

    /**
     * Redoes the most recent move in this game of chess.
     * @return <code>true</code> if a move was successfully redone, and
     *  <code>false</code> if unsuccessful, such as due to their being no
     *  moves to redo
     */
    public boolean redo() {
        if (playerIncrement >= 0 || !whitePlayer.isHuman() && !blackPlayer.isHuman()) {
            return false;
        }
        synchronized(this) {
            Player otherPlayer = playerTurn==whitePlayer ? blackPlayer : whitePlayer;
            int endMoveNo = moveNo+1;
            if (!otherPlayer.isHuman()) {
                endMoveNo++;
            }
            if (endMoveNo > moveHistory.size()) {
                return false;
            }
            stop();
            for (int i=moveNo; i<endMoveNo; i++) {
                performMove(moveHistory.get(i));
            }
        }
        updateState();
        start();
        return true;
    }

    /**
     * Resets the clocks for the players of this game of chess.
     */
    public void resetTimers() {
        whitePlayer.resetTimer();
        blackPlayer.resetTimer();
    }

    /**
     * Returns a 2 dimensional character array signifing the possible moves
     * available from a particular location on the board for the current player.
     * @param r the row from which possible moves should be considered
     * @param c the column from which possible moves should be considered
     * @return a 2 dimensional character array signifing the possible moves
     *  available from a particular location on the board for the current player.
     *  <code>null</code> is returned if an invalid start location is given,
     *  the start location does not have a piece of the current player's
     *  color, or the current player is AI.
     */
    public char[][] getMoveHighlights(int r, int c) {
        if (r < 0 || r >= Board.ROWS || c < 0 || c >= Board.COLS || getPlayerTurn() == null || !getPlayerTurn().isHuman()) {
            return null;
        }
        boolean playerIsWhite = getPlayerTurn().isWhite();
        Piece fromPiece = board.getPiece(r, c);
        if (fromPiece == null || playerIsWhite != fromPiece.isWhite()) {
            return null;
        }
        char[][] highlights = {
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '}
        };
        highlights[r][c] = 'f'; // Tile we come from
        for (Move move : board.getMoves(r, c)) {
            highlights[move.getRowTo()][move.getColTo()] = getHighlightChar(move);
        }
        return highlights;
    }

    /**
     * Returns a 2 dimensional character array signifing the calculated best
     * moves available on the board for the current human player.
     * @return a 2 dimensional character array signifing the best move
     *  available on the board for the current human player. <code>null</code>
     *  is returned if the current player is AI, or the the thread is
     *  interrupted or times out
     */
    public char[][] getHumanHint() {
        // System.err.println("HINT CALLED");
        if (getPlayerTurn() == null || !getPlayerTurn().isHuman()) {
            return null;
        }
        char[][] highlights = {
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' '}
        };
        ChessAIThread hintThread = null;
        try {
            // int attempts = HINT_THREAD_ATTEMPT_MAX;
            // while (hintThread == null) {
            //     System.err.println("ATTEMPTS "+attempts);
            //     // synchronized (this) {
            //     //     hintThread = getPlayerTurn().getStartedAIThread();
            //     // }
            //     // hintThread = getPlayerTurn().getStartedAIThread();
            //     hintThread = getPlayerTurn().getAIThread();
            //     attempts--;
            //     if (attempts > 0) {
            //         if (hintThread == null) {
            //             Thread.sleep(HINT_THREAD_ATTEMPT_SLEEP);
            //         }
            //     } else {
            //         throw new InterruptedException();
            //     }
            // }

            // synchronized (playerTurnLock) {
            //     Player curPlayer = getPlayerTurn();
            //     synchronized (curPlayer) {
            //         if (curPlayer.getAIThread() == null) {
            //             curPlayer.wait();
            //         }
            //         hintThread = curPlayer.getAIThread();
            //         if (hintThread == null) {
            //             return null;
            //         }
            //     }
            // }

            // synchronized (playerTurnLock) {
                synchronized (getPlayerTurn()) {
                // synchronized (playerTurn) {
                    // if (playerTurn.getAIThread() == null) {
                    //     System.err.println("ABOUT TO WAIT");
                    //     playerTurn.wait();
                    //     System.err.println("DONE WAITING");
                    // }
                    // hintThread = playerTurn.getAIThread();
                    while (hintThread == null) {
                        hintThread = playerTurn.getAIThread();
                        if (hintThread == null) {
                            // System.err.println("ABOUT TO WAIT");
                            playerTurn.wait();
                            // System.err.println("DONE WAITING");
                        }
                    }
                }
            // }
            // if (hintThread == null) {
            //     return null;
            // }
        } catch(InterruptedException e) {
            System.err.println("Error: Was interrupted/timed out during move calculation thread retrieval.");
            return null;
        }
        Move hintMove = hintThread.getResult();
        highlights[hintMove.getRowFrom()][hintMove.getColFrom()] = 'f';
        highlights[hintMove.getRowTo()][hintMove.getColTo()] = getHighlightChar(hintMove);
        return highlights;
    }

    /**
     * Returns a character representing the action of this move on the current
     * board.
     * @param move the move whose effect should be queried and returned via
     *  a character representation.
     * @return 'c' if the move is castling, 'e' if the move is en passant,
     *  'd' if the move is a double pawn move, 'p' if the move is a pawn
     *  promotion, 'x' if the move is a capturing move, 't' if the move is
     *  a regular non-capturing move, and ' ' otherwise (invalid moves will
     *  return ' ')
     */
    private char getHighlightChar(Move move) {
        Piece pieceMoving = board.getPiece(move.getRowFrom(), move.getColFrom());
        if (pieceMoving == null) {
            return ' ';
        }
        Piece pieceTarget = board.getPiece(move.getRowTo(), move.getColTo());
        char highlight = ' ';
        if (pieceTarget == null) {
            if (move.getType() == MoveType.NORMAL) {
                highlight = 't'; // Tile we go to (normal)
            } else if (move.getType() == MoveType.CASTLING) {
                highlight = 'c'; // Tile we go to (castling)
            } else if (move.getType() == MoveType.EN_PASSANT) {
                highlight = 'e'; // Tile we go to (en passant)
            } else if (move.getType() == MoveType.PAWN_DOUBLE) {
                highlight = 'd'; // Tile we go to (pawn double)
            } else if (move.getType() == MoveType.PAWN_PROMOTION) {
                highlight = 'p'; // Tile we go to (pawn promo)
            }
        } else if (pieceTarget.isWhite() != pieceMoving.isWhite()) {
            highlight = 'x'; // Tile we go to (capture)
        }
        return highlight;
    }

    /**
     * Checks if movement from the given location to the given location would
     * result in pawn promotion.
     * @param rowFrom the proposed row from which a piece moves
     * @param colFrom the proposed column from which a piece moves
     * @param rowTo the proposed row to which a piece moves
     * @param colTo the proposed column to which a piece moves
     * @return <code>true</code> if the proposed movement would result in pawn
     *  promotion, otherwise <code>false</code>
     */
    public boolean checkPawnPromotion(int rowFrom, int colFrom, int rowTo, int colTo) {
        // Piece movingPiece = board.getPiece(rowFrom, colFrom);
        // return movingPiece != null && movingPiece.getPieceType() == PieceType.PAWN &&
        //     ((movingPiece.isWhite() && rowTo == 0) || (!movingPiece.isWhite() && rowTo == Board.ROWS-1));
        return board.validPromotion(rowFrom, colFrom, rowTo, colTo);
    }

    /**
     * Returns the index of the last move present on the board in the game's
     * move history.
     * @return the index of the last move present on the board in the game's
     *  move history, or -1 if no moves are present on the board
     * @see getMoveHistory
     * @see getAlgebraicHistory
     */
    public int latestMoveIndex() {
        return moveNo-1;
    }

    /**
     * Resigns the current player of this chess game, if the current player is
     * human.
     */
    public void resign() {
        Player resigner = getPlayerTurn();
        if (resigner.isHuman()) {
            currentState = resigner.isWhite() ? GameState.WHITE_RESIGN : GameState.BLACK_RESIGN;
            gameOver();
        }
    }

    /**
     * Returns a PGNFile object representation of this chess game.
     * @return a PGNFile object representation of this chess game
     */
    public PGNFile getPGNFile() {
        String resultStr = "";
        switch (currentState) {
            case WHITE_CHECKMATE:
            case BLACK_RESIGN:
                resultStr = "1-0";
                break;
            case BLACK_CHECKMATE:
            case WHITE_RESIGN:
                resultStr = "0-1";
                break;
            case STALEMATE_50MOVES:
            case STALEMATE_NOMOVES:
            case STALEMATE_REPITITION:
                resultStr = "1/2-1/2";
                break;
            default:
                resultStr = "*";
                break;
        }
        String whiteStr = (whitePlayer.isHuman() ? "Human" : "AI "+whitePlayer.getAIDepth());
        String blackStr = (blackPlayer.isHuman() ? "Human" : "AI "+blackPlayer.getAIDepth());
        tagPairs.putIfAbsent("Event", "Casual Chess Game");
        tagPairs.putIfAbsent("Site", System.getProperty("user.name")+"'s computer");
        tagPairs.putIfAbsent("Date", ""+java.time.LocalDate.now());
        tagPairs.putIfAbsent("Round", "");
        tagPairs.putIfAbsent("White", whiteStr);
        tagPairs.putIfAbsent("Black", blackStr);
        tagPairs.put("Result", resultStr);
        return new PGNFile(tagPairMap(), getAlgebraicHistory(), resultStr);
    }

    /**
     * Loads the given PGN information into this game, including making the
     * game's moves. The game is then resumed if able.
     * @param pgnFile the PGN file object with information to load into this game
     * @return <code>true</code> if the file load was successful, otherwise
     *  <code>false</code>
     */
    public boolean loadPGNFile(PGNFile pgnFile) {
        if (pgnFile == null) {
            System.err.println("Attempted to load null pgn file");
            return false;
        }
        stop();
        Board oldBoard = board;
        List<Move> oldMoveHistory = new ArrayList<Move>(moveHistory);
        List<String> oldAlgebraicHistory = new ArrayList<String>(algebraicHistory);
        int oldMoveNo = moveNo;
        moveHistory = new ArrayList<Move>();
        algebraicHistory = new ArrayList<String>();
        moveNo = 0;
        board = new Board();
        forceSetPlayerTurn(true);
        boolean failFlag = false;
        for (String movePgn : pgnFile.getMoveTextList()) {
            Move move = null;
            try {
                move = PGNTranslator.translatePGNToMove(movePgn, board, getPlayerTurn().isWhite());
                performMove(move);
            } catch(Exception e){
                System.err.println("Could not load PGNFile to game:\n");
                e.printStackTrace();
                failFlag = true;
            }
        }
        if (failFlag) {
            board = oldBoard;
            moveHistory = oldMoveHistory;
            algebraicHistory = oldAlgebraicHistory;
            moveNo = oldMoveNo;
            return false;
        }
        tagPairMap().putAll(pgnFile.getTagPairMap());
        updateState();
        start();
        return true;
    }

    /**
     * Reinitializes and starts this chess game with the same settings.
     */
    public void rematch() {
        stop();
        moveHistory.clear();
        algebraicHistory.clear();
        resetTimers();
        board = new Board();
        moveNo = 0;
        forceSetPlayerTurn(true);
        updateState();
        notifyViews();
        start();
    }

    /**
     * Registers a clock to the given player.
     * @param clock the clock to register with a player
     * @param isWhiteClock <code>true</code> if the clock should be registered
     *  to the white player, <code>false</code> if the clock should be registered
     *  to the black player
     */
    public void registerPlayerClock(ClockView clock, boolean isWhiteClock) {
        if (isWhiteClock) {
            whitePlayer.registerPlayerClock(clock);
        } else {
            blackPlayer.registerPlayerClock(clock);
        }
    }

    /**
     * Registeres a clock to be updated holding total game time.
     * @param clock the clock to hold this chess games total game time
     */
    public void registerTotalGameTimeClock(ClockView clock) {
        totalGameTimeClock = clock;
    }

    /**
     * Causes the game to end due to the given player running out of time.
     * @param timedOutPlayer the timed-out, losing player
     */
    public void timeOutGame(Player timedOutPlayer) {
        stop();
        currentState = timedOutPlayer.isWhite() ? GameState.WHITE_TIMEOUT : GameState.BLACK_TIMEOUT;
        gameOver();
    }

    /**
     * Removes the registered clocks from the white and black players.
     */
    public void removeClocks() {
        if (totalClockOn) {
            if (whitePlayer != null) {
                whitePlayer.pauseTimer();
                whitePlayer.removeClocks();
            }
            if (blackPlayer != null) {
                blackPlayer.pauseTimer();
                blackPlayer.removeClocks();
            }
            timer.cancel();
            totalClockOn = false;
        }
    }

    /**
     * Changes the types of the players currently playing this chess game.
     * @param whiteType an integer determining the level AI of the white player.
     *  negative one signifies a human player, zero is a random player, and
     *  positive values indicate an AI with the given minimax depth of search
     * @param blackType an integer determining the level AI of the black player.
     *  negative one signifies a human player, zero is a random player, and
     *  positive values indicate an AI with the given minimax depth of search
     */
    public void changePlayerTypes(int whiteType, int blackType) {
        if (playerIncrement < 0) {
            stop();
            boolean isWhiteTurn = getPlayerTurn().isWhite();
            if (whiteType < 0) {
                whitePlayer = new PlayerHuman(whitePlayer);
            } else {
                whitePlayer = new PlayerAI(whitePlayer, whiteType);
            }
            if (blackType < 0) {
                blackPlayer = new PlayerHuman(blackPlayer);
            } else {
                blackPlayer = new PlayerAI(blackPlayer, blackType);
            }
            // TODO update text thing
            // synchronized (playerTurnLock) {
            //     playerTurn = isWhiteTurn ? whitePlayer : blackPlayer;
            // }
            forceSetPlayerTurn(isWhiteTurn);
            start();
        }
    }

}
