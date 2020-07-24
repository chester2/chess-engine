package chess.search;

import chess.basictypes.Position;
import chess.basictypes.Zobrist;
import chess.move.Make;
import chess.move.Move;
import chess.move.Unmake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PrincipleVariationTable {
    private final HashMap<Long, Move> map = new HashMap<>();

    public Move put(final Long key, final Move value) {
        return map.put(key, value);
    }

    public Move get(final Long key) {
        return map.get(key);
    }

    public List<Move> getLine(final Position position, final int targetDepth) {
        final var out = new ArrayList<Move>();
        final var snapshots = new ArrayList<Position.Snapshot>();
        var depth = 0;
        for (; depth < targetDepth; depth++) {
            final var key = Zobrist.hash(position);
            final var pv = map.get(key);
            if (pv == null) break;
            out.add(pv);
            snapshots.add(Make.run(position, pv));
        }

        assert depth == snapshots.size();
        while (depth > 0)
            Unmake.run(position, snapshots.remove(--depth));

        return out;
    }

    public void clear() {
        map.clear();
    }
}
