package chess.basictypes;

public class CastlingDefinition {
    private static final CastlingDefinition[][] cache = new CastlingDefinition[Color.count()][CastlingDirection.count()];
    static {
        cache[Color.WHITE.ordinal()][CastlingDirection.OO.ordinal()]
            = new CastlingDefinition(Square.E1, Square.G1, Square.H1, Square.F1, 0x70L, 0x60L);
        cache[Color.WHITE.ordinal()][CastlingDirection.OOO.ordinal()]
            = new CastlingDefinition(Square.E1, Square.C1, Square.A1, Square.D1, 0x1cL, 0x0eL);
        cache[Color.BLACK.ordinal()][CastlingDirection.OO.ordinal()]
            = new CastlingDefinition(Square.E8, Square.G8, Square.H8, Square.F8, 0x70L << 56, 0x60L << 56);
        cache[Color.BLACK.ordinal()][CastlingDirection.OOO.ordinal()]
            = new CastlingDefinition(Square.E8, Square.C8, Square.A8, Square.D8, 0x1cL << 56, 0x0eL << 56);
    }

    public static CastlingDefinition of(final Color color, final CastlingDirection direction) {
        return cache[color.ordinal()][direction.ordinal()];
    }

    private final Square kingFrom;
    private final Square kingTo;
    private final Square rookFrom;
    private final Square rookTo;
    private final long cannotBeAttackedBB;
    private final long cannotBeOccupiedBB;

    private CastlingDefinition(
        final Square kingFrom,
        final Square kingTo,
        final Square rookFrom,
        final Square rookTo,
        final long cannotBeAttackedBB,
        final long cannotBeOccupiedBB
    ) {
        this.kingFrom = kingFrom;
        this.kingTo = kingTo;
        this.rookFrom = rookFrom;
        this.rookTo = rookTo;
        this.cannotBeAttackedBB = cannotBeAttackedBB;
        this.cannotBeOccupiedBB = cannotBeOccupiedBB;
    }

    public Square getKingFrom() {
        return kingFrom;
    }

    public Square getKingTo() {
        return kingTo;
    }

    public Square getRookFrom() {
        return rookFrom;
    }

    public Square getRookTo() {
        return rookTo;
    }

    public long getCannotBeAttackedBB() {
        return cannotBeAttackedBB;
    }

    public long getCannotBeOccupiedBB() {
        return cannotBeOccupiedBB;
    }
}
