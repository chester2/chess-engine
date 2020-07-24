package chess.basictypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Piece {
    WP, WN, WB, WR, WQ, WK,
    BP, BN, BB, BR, BQ, BK;

    private static final List<Piece> valueList = Collections.unmodifiableList(Arrays.asList(values()));
    private static final List<List<Piece>> valueListByColor;
    static {
        var temp = new ArrayList<List<Piece>>(2);
        temp.add(Collections.unmodifiableList(valueList.subList(0, 6)));
        temp.add(Collections.unmodifiableList(valueList.subList(6, 12)));
        valueListByColor = Collections.unmodifiableList(temp);
    }

    public Color color() { return ordinal() <= WK.ordinal() ? Color.WHITE : Color.BLACK; }

    public Type type() { return Type.valueList.get(ordinal() % Type.count()); }

    public static Piece of(final Color color, final Type type) {
        return valueList.get(color.ordinal() * Type.count() + type.ordinal());
    }

    public static List<Piece> valueList() { return valueList; }

    public static List<Piece> valueList(final Color color) { return valueListByColor.get(color.ordinal()); }

    public static int count() { return valueList.size(); }

    public enum Type {
        PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;

        private static final List<Type> valueList = Collections.unmodifiableList(Arrays.asList(values()));

        public static List<Type> valueList() { return valueList; }

        public static int count() { return valueList.size(); }
    }
}
