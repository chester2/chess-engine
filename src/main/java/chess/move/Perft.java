package chess.move;

import chess.basictypes.Piece;
import chess.basictypes.Position;
import chess.basictypes.Square;

import java.util.Objects;

public class Perft {
    private final Position position;

    public Perft(final Position position) {
        this.position = Objects.requireNonNull(position);
    }

    public Position.Snapshot make(final String move) {
        return Make.run(position, move, Movegen.pseudoLegal(position));
    }

    public long perft(final int depth) {
        if (depth <= 0) return 1L;

        var nodes = 0L;
        for (final var move : Movegen.pseudoLegal(position)) {
            final var snapshot = Make.run(position, move);
            if (!Movegen.opponentKingIsAttacked(position))
                nodes += perft(depth - 1);
            Unmake.run(position, snapshot);
        }
        return nodes;
    }

    public void divide(final int depth) {
        var nodes = 0L;
        for (final var move : Movegen.pseudoLegal(position)) {
            System.out.print(move + ": ");
            final var snapshot = Make.run(position, move);
            if (Movegen.opponentKingIsAttacked(position)) {
                System.out.println("Illegal");
            } else {
                final var perftResults = perft(depth - 1);
                System.out.println(perftResults);
                nodes += perftResults;
            }
            Unmake.run(position, snapshot);
        }
        System.out.println("\nNodes searched: " + nodes);
    }
}
