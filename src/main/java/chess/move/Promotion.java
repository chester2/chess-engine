package chess.move;

import chess.basictypes.Color;
import chess.basictypes.Piece;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class Promotion {
    private static final List<Piece.Type> promotableTypes =
        Collections.unmodifiableList(
            Arrays.asList(Piece.Type.KNIGHT, Piece.Type.BISHOP, Piece.Type.ROOK, Piece.Type.QUEEN)
        );

    public static List<Piece.Type> promotableTypes() { return promotableTypes; }

    public static int rank(final Color color) {
        return Objects.requireNonNull(color) == Color.WHITE ? 6 : 1;
    }
}
