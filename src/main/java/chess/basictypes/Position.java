package chess.basictypes;

import chess.move.Move;

import java.util.Arrays;
import java.util.Objects;

public class Position {
    public static String STARTPOS_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    //region Fields/Constructors/Accessors

    private Color currentColor = Color.WHITE;
    private CastlingRights castlingRights = CastlingRights.NONE_ALLOWED;
    private Square epSquare;
    private int halfmoveClock;
    private int fullmoveCounter;

    private final long[] pieceBBs = new long[Piece.count()];
    private final Piece[] pieceArray = new Piece[Square.count()];

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(final Color currentColor) {
        this.currentColor = Objects.requireNonNull(currentColor);
    }

    public CastlingRights getCastlingRights() {
        return castlingRights;
    }

    public void setCastlingRights(final CastlingRights castlingRights) {
        this.castlingRights = Objects.requireNonNull(castlingRights);
    }

    public Square getEpSquare() {
        return epSquare;
    }

    public void setEpSquare(final Square epSquare) {
        if (epSquare != null && epSquare.rank() != 2 && epSquare.rank() != 5)
            throw new IllegalArgumentException("En passant square must be on rank 3 or 6");
        this.epSquare = epSquare;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public void setHalfmoveClock(final int halfmoveClock) {
        if (halfmoveClock < 0) throw new IllegalArgumentException("Halfmove clock cannot be negative");
        this.halfmoveClock = halfmoveClock;
    }

    public int getFullmoveCounter() {
        return fullmoveCounter;
    }

    public void setFullmoveCounter(final int fullmoveCounter) {
        if (fullmoveCounter < 1) throw new IllegalArgumentException("Full move counter cannot be less than 1");
        this.fullmoveCounter = fullmoveCounter;
    }

    public long getBB(final Piece piece) {
        return pieceBBs[piece.ordinal()];
    }

    public long getBB(final Color color, final Piece.Type type) {
        return getBB(Piece.of(color, type));
    }

    public long getBB(final Color color) {
        return Piece.Type.valueList().stream()
            .map(t -> pieceBBs[Piece.of(color, t).ordinal()])
            .reduce(0L, (x, y) -> x | y);
    }

    public long getBB() {
        return Arrays.stream(pieceBBs).reduce(0L, (x, y) -> x | y);
    }

    public void xorBB(final Piece piece, final long bitboard) {
        pieceBBs[piece.ordinal()] ^= bitboard;
    }

    public Piece getPieceAt(final Square square) {
        return pieceArray[square.ordinal()];
    }

    public void setPieceAt(final Square square, final Piece piece) {
        pieceArray[square.ordinal()] = piece;
    }

    public Square getKingSquare(final Color color) {
        return Square.valueList().get(
            Bitboard.bitScan(
                pieceBBs[Piece.of(color, Piece.Type.KING).ordinal()]
            )
        );
    }

    //endregion

    //region General

    public Position applyFen(final String fen) {
        var i = 0;

        Arrays.fill(pieceBBs, 0L);
        Arrays.fill(pieceArray, null);
        for (
            int c, file = 0, rank = 7
            ; (c = fen.charAt(i)) != ' ' && rank >= 0
            ; i++
        ) {
            if (c == '/') {
                rank--;
                file = 0;
            } else if ("12345678".indexOf(c) != -1) {
                file += c - '0';
            } else if ("PNBRQKpnbrqk".indexOf(c) != -1) {
                final var color = Color.valueList().get(c >>> 5 & 1);
                final Piece.Type type;
                switch (Character.toUpperCase(c)) {
                    case 'P':
                        type = Piece.Type.PAWN;
                        break;
                    case 'N':
                        type = Piece.Type.KNIGHT;
                        break;
                    case 'B':
                        type = Piece.Type.BISHOP;
                        break;
                    case 'R':
                        type = Piece.Type.ROOK;
                        break;
                    case 'Q':
                        type = Piece.Type.QUEEN;
                        break;
                    default:
                        type = Piece.Type.KING;
                        break;
                }
                final var piece = Piece.of(color, type);
                final var sq = Square.at(file++, rank);
                final var sqbb = Bitboard.fromSquare(sq);
                pieceBBs[piece.ordinal()] |= sqbb;
                pieceArray[sq.ordinal()] = piece;
            } else {
                throw new IllegalArgumentException("FEN error at " + i + ": '" + c + "'");
            }
        }
        i++;

        switch (fen.charAt(i)) {
            case 'w':
                currentColor = Color.WHITE;
                break;
            case 'b':
                currentColor = Color.BLACK;
                break;
            default:
                throw new IllegalArgumentException("FEN error at " + i + ": '" + fen.charAt(i) + "'");
        }
        i += 2;

        castlingRights = CastlingRights.NONE_ALLOWED;
        for (char c; (c = fen.charAt(i)) != ' '; i++) {
            if (c == '-') {
                i++;
                break;
            }
            final Color color;
            final CastlingDirection direction;
            switch (c) {
                case 'K':
                    color = Color.WHITE;
                    direction = CastlingDirection.OO;
                    break;
                case 'Q':
                    color = Color.WHITE;
                    direction = CastlingDirection.OOO;
                    break;
                case 'k':
                    color = Color.BLACK;
                    direction = CastlingDirection.OO;
                    break;
                case 'q':
                    color = Color.BLACK;
                    direction = CastlingDirection.OOO;
                    break;
                default:
                    throw new IllegalArgumentException("FEN error at " + i + ": '" + c + "'");
            }
            castlingRights = castlingRights.allow(color, direction);
        }
        i++;

        if (fen.charAt(i) == '-') {
            epSquare = null;
        } else {
            final var file = fen.charAt(i) - 'a';
            final var rank = fen.charAt(++i) - '1';
            if (file < 0 || file > 7 || rank < 0 || rank > 7)
                throw new IllegalArgumentException("FEN error: bad EP Square");
            epSquare = Square.at(file, rank);
        }
        i += 2;

        halfmoveClock = 0;
        for (char c; (c = fen.charAt(i)) != ' '; i++) {
            if (!Character.isDigit(c))
                throw new IllegalArgumentException("FEN error at " + i + ": '" + c + "'");
            halfmoveClock *= 10;
            halfmoveClock += c - '0';
        }
        i++;

        fullmoveCounter = 0;
        for (; i < fen.length(); i++) {
            final var c = fen.charAt(i);
            if (!Character.isDigit(c))
                throw new IllegalArgumentException("FEN error at " + i + ": '" + c + "'");
            fullmoveCounter *= 10;
            fullmoveCounter += c - '0';
        }

        final var errorMsg = validate();
        if (errorMsg != null)
            throw new IllegalStateException(errorMsg);

        return this;
    }

    public String toFen() {
        final var sb = new StringBuilder();

        for (var rank = 7; rank >= 0; rank--) {
            var skip = 0;
            for (var file = 0; file <= 7; file++) {
                final var sq = Square.at(file, rank);
                final var piece = pieceArray[sq.ordinal()];
                if (piece == null) {
                    skip++;
                } else {
                    if (skip > 0) sb.append(skip);
                    skip = 0;
                    char label;
                    switch (piece.type()) {
                        case PAWN:
                            label = 'P';
                            break;
                        case KNIGHT:
                            label = 'N';
                            break;
                        case BISHOP:
                            label = 'B';
                            break;
                        case ROOK:
                            label = 'R';
                            break;
                        case QUEEN:
                            label = 'Q';
                            break;
                        default:
                            label = 'K';
                            break;
                    }
                    sb.append(piece.color() == Color.WHITE ? label : Character.toUpperCase(label));
                }
            }
            if (skip > 0) sb.append(skip);
            sb.append(rank == 0 ? ' ' : '/');
        }

        sb.append(currentColor == Color.WHITE ? 'w' : 'b');
        sb.append(' ');

        if (castlingRights.equals(CastlingRights.NONE_ALLOWED)) {
            sb.append('-');
        } else {
            if (castlingRights.isAllowed(Color.WHITE, CastlingDirection.OO)) sb.append('K');
            if (castlingRights.isAllowed(Color.WHITE, CastlingDirection.OOO)) sb.append('Q');
            if (castlingRights.isAllowed(Color.BLACK, CastlingDirection.OO)) sb.append('k');
            if (castlingRights.isAllowed(Color.BLACK, CastlingDirection.OOO)) sb.append('q');
        }
        sb.append(' ');

        sb.append(epSquare == null ? '-' : epSquare.name().toLowerCase());
        sb.append(' ');

        sb.append(halfmoveClock);
        sb.append(' ');

        sb.append(fullmoveCounter);

        return sb.toString();
    }

    public void print() {
        final var rowDivider = "  +---+---+---+---+---+---+---+---+";

        System.out.println(rowDivider);
        for (var rank = 7; rank >= 0; rank--) {
            System.out.print(rank + 1);
            for (var file = 0; file < 8; file++) {
                final var piece = pieceArray[Square.at(file, rank).ordinal()];
                if (piece == null) {
                    System.out.print(" | -");
                } else {
                    var label = piece.name().charAt(1);
                    if (piece.color() == Color.BLACK) label = Character.toLowerCase(label);
                    System.out.print(" | " + label);
                }
            }
            System.out.println(" |\n" + rowDivider);
        }
        System.out.println("    a   b   c   d   e   f   g   h\n\n");
        System.out.println(toFen());
    }

    public String validate() {
        if (currentColor == null)
            return "currentColor is null";

        for (final var sq : Square.valueList()) {
            final var sqbb = Bitboard.fromSquare(sq);
            final var piece = pieceArray[sq.ordinal()];
            if (piece == null) {
                if (0 != (getBB() & sqbb))
                    return "boardArray does not match pieceBBs at square " + sq.name();
            } else if (0 == (getBB(piece) & sqbb)) {
                return "boardArray does not match pieceBBs at square " + sq.name();
            }
        }

        return null;
    }

    //endregion

    public static class Snapshot {
        private final Move move;
        private final Piece capturedPiece;
        private final long key;
        private final CastlingRights castlingRights;
        private final Square epSquare;
        private final int halfmoveClock;

        public Snapshot(
            final Move move,
            final Piece capturedPiece,
            final long key,
            final CastlingRights castlingRights,
            final Square epSquare,
            final int halfmoveClock
        ) {
            assert !move.isEnpassantCapture() || capturedPiece == null;
            this.move = move;
            this.capturedPiece = capturedPiece;
            this.key = key;
            this.castlingRights = Objects.requireNonNull(castlingRights);
            this.epSquare = epSquare;
            this.halfmoveClock = halfmoveClock;
        }

        public Move getMove() {
            return move;
        }

        public Piece getCapturedPiece() {
            return capturedPiece;
        }

        public long getKey() {
            return key;
        }

        public CastlingRights getCastlingRights() {
            return castlingRights;
        }

        public Square getEpSquare() {
            return epSquare;
        }

        public int getHalfmoveClock() {
            return halfmoveClock;
        }
    }
}
