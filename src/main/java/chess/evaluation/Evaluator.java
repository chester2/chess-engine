package chess.evaluation;

import chess.basictypes.Bitboard;
import chess.basictypes.Color;
import chess.basictypes.Piece;
import chess.basictypes.Position;

public class Evaluator {
    public static final int BEST_VALUE = Integer.MAX_VALUE;
    public static final int WORST_VALUE = -Integer.MAX_VALUE;
    public static final int MATE_VALUE = -30000;
    public static final int DRAW_VALUE = 0;

    public static int run(final Position position) {
        var material = 0;
        var pieceLoc = 0;
        for (final var piece : Piece.valueList()) {
            for (final var sq : Bitboard.iterSquares(position.getBB(piece))) {
                material += Material.getValue(piece);
                pieceLoc += PieceLocations.getValue(piece, sq);
            }
        }

        var score = material + pieceLoc;
//        return score;
        return (position.getCurrentColor() == Color.WHITE) ? score : -score;
    }
}
