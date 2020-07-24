package chess.basictypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum CastlingDirection {
    OO, OOO;

    private static final List<CastlingDirection> valueList = Collections.unmodifiableList(Arrays.asList(CastlingDirection.values()));

    public static List<CastlingDirection> valueList() { return valueList; }

    public static int count() { return valueList.size(); }
}
