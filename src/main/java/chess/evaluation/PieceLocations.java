package chess.evaluation;

import chess.basictypes.Color;
import chess.basictypes.Piece;
import chess.basictypes.Square;

class PieceLocations {
    private static final int[][] values = new int[Piece.count()][];
    static {
        values[Piece.WP.ordinal()] = new int[] {
            0, 0, 0, 0, 0, 0, 0, 0,
            10, 10, 0, -10, -10, 0, 10, 10,
            5, 0, 0, 5, 5, 0, 0, 5,
            0, 0, 10, 20, 20, 10, 0, 0,
            5, 5, 5, 10, 10, 5, 5, 5,
            10, 10, 10, 20, 20, 10, 10, 10,
            20, 20, 20, 30, 30, 20, 20, 20,
            0, 0, 0, 0, 0, 0, 0, 0
        };

        values[Piece.WN.ordinal()] = new int[] {
            0, -10, 0, 0, 0, 0, -10, 0,
            0, 0, 0, 5, 5, 0, 0, 0,
            0, 0, 10, 10, 10, 10, 0, 0,
            0, 0, 10, 20, 20, 10, 5, 0,
            5, 10, 15, 20, 20, 15, 10, 5,
            5, 10, 10, 20, 20, 10, 10, 5,
            0, 0, 5, 10, 10, 5, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
        };

        values[Piece.WB.ordinal()] = new int[] {
            0, 0, -10, 0, 0, -10, 0, 0,
            0, 0, 0, 10, 10, 0, 0, 0,
            0, 0, 10, 15, 15, 10, 0, 0,
            0, 10, 15, 20, 20, 15, 10, 0,
            0, 10, 15, 20, 20, 15, 10, 0,
            0, 0, 10, 15, 15, 10, 0, 0,
            0, 0, 0, 10, 10, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
        };

        values[Piece.WR.ordinal()] = new int[] {
            0, 0, 5, 10, 10, 5, 0, 0,
            0, 0, 5, 10, 10, 5, 0, 0,
            0, 0, 5, 10, 10, 5, 0, 0,
            0, 0, 5, 10, 10, 5, 0, 0,
            0, 0, 5, 10, 10, 5, 0, 0,
            0, 0, 5, 10, 10, 5, 0, 0,
            25, 25, 25, 25, 25, 25, 25, 25,
            0, 0, 5, 10, 10, 5, 0, 0
        };

        values[Piece.WQ.ordinal()] = new int[Square.count()];

        values[Piece.WK.ordinal()] = new int[Square.count()];

        for (final var type : Piece.Type.valueList()) {
            final var table = new int[Square.count()];
            for (final var sq : Square.valueList())
                table[sq.ordinal()] = -values[Piece.of(Color.WHITE, type).ordinal()][sq.verticalFlip().ordinal()];
            values[Piece.of(Color.BLACK, type).ordinal()] = table;
        }
    }

    public static int getValue(final Piece piece, final Square square) {
        return values[piece.ordinal()][square.ordinal()];
    }
}
