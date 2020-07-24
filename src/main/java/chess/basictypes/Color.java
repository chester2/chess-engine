package chess.basictypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Color {
    WHITE, BLACK;

    private static final List<Color> valueList = Collections.unmodifiableList(Arrays.asList(values()));

    public Color invert() { return this == WHITE ? BLACK : WHITE; }

    public static List<Color> valueList() { return valueList; }

    public static int count() { return valueList.size(); }
}
