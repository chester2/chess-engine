package chess.basictypes;

public class CastlingRights {
    public static int count() { return 16; }

    private static CastlingRights[] cache = new CastlingRights[count()];
    static {
        for (var i = 0; i < count(); i++)
            cache[i] = new CastlingRights(i);
    }

    public static final CastlingRights NONE_ALLOWED = cache[0];

    public static CastlingRights fromOrdinal(final int ordinal) {
        return cache[ordinal];
    }

    private final int ordinal;

    private CastlingRights(final int ordinal) {
        assert ordinal >= 0 && ordinal < 16;
        this.ordinal = ordinal;
    }

    public int getOrdinal() {
        return ordinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CastlingRights)) return false;
        CastlingRights rights = (CastlingRights)o;
        return ordinal == rights.ordinal;
    }

    @Override
    public int hashCode() {
        return ordinal;
    }

    private int mask(final Color color, final CastlingDirection direction) {
        return 1 << (color.ordinal() << 1 | direction.ordinal());
    }

    public CastlingDefinition getDefinition(final Color color, final CastlingDirection direction) {
        return (0 == (mask(color, direction) & ordinal)) ? null : CastlingDefinition.of(color, direction);
    }

    public boolean isAllowed(final Color color, final CastlingDirection direction) {
        return getDefinition(color, direction) != null;
    }

    public CastlingRights allow(final Color color, final CastlingDirection direction) {
        return fromOrdinal(ordinal | mask(color, direction));
    }

    public CastlingRights disallow(final Color color, final CastlingDirection direction) {
        return fromOrdinal(ordinal & ~mask(color, direction));
    }

    public CastlingRights disallow(final Color color) {
        final var mask = 0b11 << color.ordinal() * 2;
        return fromOrdinal(ordinal & ~mask);
    }
}
