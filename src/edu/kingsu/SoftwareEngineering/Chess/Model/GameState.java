package edu.kingsu.SoftwareEngineering.Chess.Model;

public enum GameState {
    ACTIVE,
    STALEMATE_50MOVES,
    STALEMATE_NOMOVES,
    STALEMATE_REPITITION,
    WHITE_CHECK,
    BLACK_CHECK,
    WHITE_CHECKMATE,
    BLACK_CHECKMATE,
    WHITE_RESIGN,
    BLACK_RESIGN
}
