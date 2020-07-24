package chess.evaluation;

import chess.basictypes.Color;
import chess.basictypes.Piece;

class Material {
    private static final int[] values = new int[Piece.count()];
    static {
        values[Piece.WP.ordinal()] = 100;
        values[Piece.WN.ordinal()] = 300;
        values[Piece.WB.ordinal()] = 300;
        values[Piece.WR.ordinal()] = 500;
        values[Piece.WQ.ordinal()] = 900;
        values[Piece.WK.ordinal()] = 30000;
        for (final var type : Piece.Type.valueList())
            values[Piece.of(Color.BLACK, type).ordinal()] = -values[Piece.of(Color.WHITE, type).ordinal()];
    }

    public static int getValue(final Piece piece) {
        return values[piece.ordinal()];
    }
}
