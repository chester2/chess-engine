package chess.search;

import chess.basictypes.Piece;
import chess.basictypes.Position;
import chess.move.Move;
import chess.move.Movegen;

import java.util.*;

public class OrderedMoveList implements Iterable<Move> {
    private final ArrayList<Move> moves = new ArrayList<>();
    private final ArrayList<Integer> scores = new ArrayList<>();

    public OrderedMoveList(final Position position, final List<Move> moveList) {
        for (final var move : moveList)
            add(move, position);
    }

    public static OrderedMoveList pseudoLegal(final Position position) {
        return new OrderedMoveList(position, Movegen.pseudoLegal(position));
    }

    public List<Move> getMoves() { return Collections.unmodifiableList(moves); }

    @Override
    public Iterator<Move> iterator() {
        return new Iterator<Move>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < moves.size();
            }

            @Override
            public Move next() {
                var bestIndex = i;
                for (var j = i + 1; j < scores.size(); j++)
                    if (scores.get(j) > bestIndex)
                        bestIndex = j;
                swap(i, bestIndex);
                return moves.get(i++);
            }
        };
    }

    private int mvvlva(final Move move, final Position position) {
        if (move.isEnpassantCapture() || position.getPieceAt(move.getTo()) != null) {
            final var attackerVal = position.getPieceAt(move.getFrom()).type().ordinal();
            final var victimVal = move.isEnpassantCapture()
                ? Piece.Type.PAWN.ordinal()
                : position.getPieceAt(move.getTo()).type().ordinal();
            return victimVal * 10 + Piece.Type.count() - attackerVal;
        }

        return 0;
    }

    public int indexOf(final Move move) {
        return moves.indexOf(move);
    }

    public void setScore(final int index, final int score) {
        scores.set(index, score);
    }

    public void add(final Move move, final Position position) {
        moves.add(Objects.requireNonNull(move));
        scores.add(mvvlva(move, position));
    }

    public Move remove(final int index) {
        scores.remove(index);
        return moves.remove(index);
    }

    public void swap(final int i, final int j) {
        final var tempMove = moves.get(i);
        moves.set(i, moves.get(j));
        moves.set(j, tempMove);

        final var tempScore = scores.get(i);
        scores.set(i, scores.get(j));
        scores.set(j, tempScore);
    }
}
