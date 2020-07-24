package chess.move;

import chess.basictypes.*;

class Motions {
    //region Non-Ray

    static long kingAttacks(final Square square) {
        return kingAttacksBySquare[square.ordinal()];
    }

    static long knightAttacks(final Square square) {
        return knightAttacksBySquare[square.ordinal()];
    }

    static long pawnAttacks(final Color color, final Square square) {
        return pawnAttacksByColorAndSquare[color.ordinal()][square.ordinal()];
    }

    static long pawnSinglePush(final Color color, final Square square) {
        return pawnSinglePushesByColorAndSquare[color.ordinal()][square.ordinal()];
    }

    static long pawnDoublePush(final Color color, final Square square) {
        return pawnDoublePushesByColorAndSquare[color.ordinal()][square.ordinal()];
    }

    private static final long[] kingAttacksBySquare = new long[Square.count()];
    private static final long[] knightAttacksBySquare = new long[Square.count()];
    private static final long[][] pawnAttacksByColorAndSquare = new long[Color.count()][Square.count()];
    private static final long[][] pawnSinglePushesByColorAndSquare = new long[Color.count()][Square.count()];
    private static final long[][] pawnDoublePushesByColorAndSquare = new long[Color.count()][Square.count()];
    static {
        for (final var sq : Square.valueList()) {
            final var sqbb = Bitboard.fromSquare(sq);

            for (final var dir : new String[]{"n", "ne", "e", "se", "s", "sw", "w", "nw"})
                kingAttacksBySquare[sq.ordinal()] |= step(dir, sqbb);
            for (final var dir : new String[]{"nne", "nee", "see", "sse", "ssw", "sww", "nww", "nnw"})
                knightAttacksBySquare[sq.ordinal()] |= step(dir, sqbb);

            if (sq.rank() < 7)
                pawnAttacksByColorAndSquare[Color.WHITE.ordinal()][sq.ordinal()] = step("ne", sqbb) | step("nw", sqbb);
            if (sq.rank() > 0)
                pawnAttacksByColorAndSquare[Color.BLACK.ordinal()][sq.ordinal()] = step("se", sqbb) | step("sw", sqbb);

            if (sq.rank() > 0 && sq.rank() < 7) {
                pawnSinglePushesByColorAndSquare[Color.WHITE.ordinal()][sq.ordinal()] = step("n", sqbb);
                pawnSinglePushesByColorAndSquare[Color.BLACK.ordinal()][sq.ordinal()] = step("s", sqbb);
            }

            if (sq.rank() == 1)
                pawnDoublePushesByColorAndSquare[Color.WHITE.ordinal()][sq.ordinal()] = step("n", step("n", sqbb));
            else if (sq.rank() == 6)
                pawnDoublePushesByColorAndSquare[Color.BLACK.ordinal()][sq.ordinal()] = step("s", step("s", sqbb));
        }
    }

    private static long step(final String direction, final long from) {
        final var excludeA = ~Bitboard.file(0);
        final var excludeAB = ~Bitboard.file(1) & excludeA;
        final var excludeH = ~Bitboard.file(7);
        final var excludeGH = ~Bitboard.file(6) & excludeH;

        switch (direction) {
            case "n":
                return from << 8;
            case "e":
                return (from & excludeH) << 1;
            case "s":
                return from >>> 8;
            case "w":
                return (from & excludeA) >> 1;

            case "ne":
                return (from & excludeH) << 9;
            case "se":
                return (from & excludeH) >>> 7;
            case "sw":
                return (from & excludeA) >>> 9;
            case "nw":
                return (from & excludeA) << 7;

            case "nne":
                return (from & excludeH) << 17;
            case "nee":
                return (from & excludeGH) << 10;
            case "see":
                return (from & excludeGH) >>> 6;
            case "sse":
                return (from & excludeH) >>> 15;
            case "ssw":
                return (from & excludeA) >>> 17;
            case "sww":
                return (from & excludeAB) >>> 10;
            case "nww":
                return (from & excludeAB) << 6;
            case "nnw":
                return (from & excludeA) << 15;

            default:
                throw new IllegalArgumentException("Bad direction '" + direction + "'");
        }
    }

    //endregion

    //region Ray Moves

    static long queenAttacks(final Square square, final long occupied) {
        return rookAttacks(square, occupied) | bishopAttacks(square, occupied);
    }

    static long rookAttacks(final Square square, final long occupied) {
        return rankAttacks(square, occupied) | fileAttacks(square, occupied);
    }

    static long bishopAttacks(final Square square, final long occupied) {
        return diagAttacks(square, occupied) | adiagAttacks(square, occupied);
    }

    private static long rankAttacks(final Square square, final long occupied) {
        final var rayMask = Bitboard.rank(square.rank());
        final var occupied6Bits
                = (occupied & rayMask & ~Bitboard.file(7))
                >>> (1 + Long.numberOfTrailingZeros(rayMask));
        return rankAttacksBySquareAndOccupation[square.ordinal()][(int) occupied6Bits];
    }

    private static long fileAttacks(final Square square, final long occupied) {
        var occupied6Bits = occupied & Bitboard.file(square.file());
        occupied6Bits >>>= square.file();
        occupied6Bits *= Bitboard.antidiagonal(7); // mirror along diag
        occupied6Bits &= ~Bitboard.file(7);
        occupied6Bits >>>= 57;
        return fileAttacksBySquareAndOccupation[square.ordinal()][(int) occupied6Bits];
    }

    private static long diagAttacks(final Square square, final long occupied) {
        var occupied6Bits = occupied & Bitboard.diagonal(square.diagonal());
        occupied6Bits *= Bitboard.file(0); // project onto horizontal
        occupied6Bits &= ~Bitboard.file(7);
        occupied6Bits >>>= 57;
        return diagonalAttacksBySquareAndOccupation[square.ordinal()][(int) occupied6Bits];
    }

    private static long adiagAttacks(final Square square, final long occupied) {
        var occupied6Bits = occupied & Bitboard.antidiagonal(square.antidiagonal());
        occupied6Bits *= Bitboard.file(0); // project onto horizontal
        occupied6Bits &= ~Bitboard.file(7);
        occupied6Bits >>>= 57;
        return antidiagonalAttacksBySquareAndOccupation[square.ordinal()][(int) occupied6Bits];
    }

    private static final long[][] rankAttacksBySquareAndOccupation = new long[Square.count()][64];
    private static final long[][] fileAttacksBySquareAndOccupation = new long[Square.count()][64];
    private static final long[][] diagonalAttacksBySquareAndOccupation = new long[Square.count()][64];
    private static final long[][] antidiagonalAttacksBySquareAndOccupation = new long[Square.count()][64];
    static {
        // byteAttacks contains 1D attack rays of files/ranks/diagonals when flattened to the bottom rank.
        // The first index is the attacker's position. The second index is the occupancy of the middle 6 positions.
        final long[][] byteAttacks = new long[8][64];
        for (var attacker = 0; attacker < 8; attacker++) {
            for (var occupied6Bits = 0; occupied6Bits < 64; occupied6Bits++) {
                final var occupied8Bits = occupied6Bits << 1;
                var ray = 0L;

                // slide right
                for (var cursor = attacker + 1; cursor < 8; cursor++) {
                    final var cursorMask = 1L << cursor;
                    ray |= cursorMask;
                    if ((cursorMask & occupied8Bits) != 0) break;
                }

                // slide left
                for (var cursor = attacker - 1; cursor >= 0; cursor--) {
                    final var cursorMask = 1L << cursor;
                    ray |= cursorMask;
                    if ((cursorMask & occupied8Bits) != 0) break;
                }

                byteAttacks[attacker][occupied6Bits] = ray;
            }
        }

        for (final var sq : Square.valueList()) {
            for (var occupied6Bits = 0; occupied6Bits < 64; occupied6Bits++) {
                rankAttacksBySquareAndOccupation[sq.ordinal()][occupied6Bits]
                        = byteAttacks[sq.file()][occupied6Bits] << 8 * sq.rank();

                fileAttacksBySquareAndOccupation[sq.ordinal()][occupied6Bits]
                        = (byteAttacks[7 - sq.rank()][occupied6Bits] * Bitboard.antidiagonal(7) & Bitboard.file(7))
                        >>> (7 - sq.file());

                diagonalAttacksBySquareAndOccupation[sq.ordinal()][occupied6Bits]
                        = byteAttacks[sq.file()][occupied6Bits] * Bitboard.file(0) & Bitboard.diagonal(sq.diagonal());

                antidiagonalAttacksBySquareAndOccupation[sq.ordinal()][occupied6Bits]
                        = byteAttacks[sq.file()][occupied6Bits] * Bitboard.file(0) & Bitboard.antidiagonal(sq.antidiagonal());
            }
        }
    }

    //endregion
}
