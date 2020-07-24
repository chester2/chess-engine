package chess.basictypes;

import java.util.Random;

public class Zobrist {
    private static final long blackToMoveKey;
    private static final long[][] squarePieceKeys = new long[Square.count()][Piece.count()];
    private static final long[] castlingRightsKeys = new long[CastlingRights.count()];
    private static final long[] epFileKeys = new long[8];
    static {
        final var rng = new Random(1);

        blackToMoveKey = rng.nextLong();

        for (final var sq : Square.valueList())
            for (final var piece : Piece.valueList())
                squarePieceKeys[sq.ordinal()][piece.ordinal()] = rng.nextLong();

        for (int i = 0; i < CastlingRights.count(); i++)
            castlingRightsKeys[i] = rng.nextLong();

        for (var i = 0; i < 8; i++)
            epFileKeys[i] = rng.nextLong();
    }

    // We are completely defeating the point of Zobrist hashing here by having this function instead of incrementally
    // updating the position key. That's okay because speed is not the focus of this engine.
    public static long hash(final Position position) {
        var key = position.getCurrentColor() == Color.BLACK ? blackToMoveKey : 0L;

        for (final var sq : Square.valueList()) {
            final var piece = position.getPieceAt(sq);
            if (piece == null) continue;
            key ^= squarePieceKeys[sq.ordinal()][piece.ordinal()];
        }

        key ^= castlingRightsKeys[position.getCastlingRights().getOrdinal()];

        if (position.getEpSquare() != null) {
            key ^= epFileKeys[position.getEpSquare().file()];
        }

        return key;
    }
}
