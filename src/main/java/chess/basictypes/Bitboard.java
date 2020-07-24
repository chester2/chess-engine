package chess.basictypes;

import java.util.Iterator;

/**
 * A bitboard is a 64-bit integer that represents some aspect of a chess board. Square 0 corresponds to the first bit,
 * square 1 corresponds to the second bit, and so on.
 */
public class Bitboard {
    private Bitboard() {}

    public static long fromSquare(final Square square) { return 1L << square.ordinal(); }

    public static int bitScan(final long bitboard) { return Long.numberOfTrailingZeros(bitboard); }

    public static int bitCount(final long bitboard) { return Long.bitCount(bitboard); }

    public static Iterable<Square> iterSquares(final long bitboard) {
        return new Iterable<Square>() {
            @Override
            public Iterator<Square> iterator() {
                return new Iterator<Square>() {
                    private long bb = bitboard;

                    @Override
                    public boolean hasNext() {
                        return bb != 0;
                    }

                    @Override
                    public Square next() {
                        final var trailingZeros = Long.numberOfTrailingZeros(bb);
                        final var out = Square.valueList().get(trailingZeros);
                        bb ^= 1L << trailingZeros;
                        return out;
                    }
                };
            }
        };
    }

    public static long file(final int index) {
        if (index < 0 || index > 7)
            throw new IllegalArgumentException("File index must be between 0 and 7 inclusive");
        return 0x0101010101010101L << index;
    }

    public static long rank(final int index) {
        if (index < 0 || index > 7)
            throw new IllegalArgumentException("Rank index must be between 0 and 7 inclusive");
        return 0xffL << index * 8;
    }

    public static long diagonal(final int index) {
        if (index < 0 || index > 14)
            throw new IllegalArgumentException("Diagonal index must be between 0 and 14 inclusive");
        final long a8h1 = 0x0102040810204080L;
        return (index <= 7)
            ? a8h1 >>> (7 - index) * 8
            : a8h1 << (index - 7) * 8;
    }

    public static long antidiagonal(final int index) {
        if (index < 0 || index > 14)
            throw new IllegalArgumentException("Antidiagonal index must be between 0 and 14 inclusive");
        final long a1h8 = 0x8040201008040201L;
        return (index <= 7)
            ? a1h8 >>> (7 - index) * 8
            : a1h8 << (index - 7) * 8;
    }

    public static String visualize(final long bitboard) {
        var out = "";
        for (var rank = 7; rank >= 0; rank--) {
            for (var file = 0; file <= 7; file++) {
                if (0L == (bitboard & fromSquare(Square.at(file, rank))))
                    out += '0';
                else
                    out += '1';
            }
            out += '\n';
        }
        return out;
    }
}
