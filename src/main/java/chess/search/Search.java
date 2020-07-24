package chess.search;

import chess.basictypes.Position;
import chess.basictypes.Zobrist;
import chess.evaluation.Evaluator;
import chess.move.Make;
import chess.move.Move;
import chess.move.Movegen;
import chess.move.Unmake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Search {
    public static final int MAX_DEPTH = 64;

    private final Position position;
    private final SearchConfig config;
    private final ArrayList<Position.Snapshot> history = new ArrayList<>();
    private final Move[][] killers = new Move[MAX_DEPTH + 1][2];
    private final PrincipleVariationTable pvTable = new PrincipleVariationTable();

    private int nodesSearched;

    public Search(final Position position, final SearchConfig config) {
        this.position = Objects.requireNonNull(position);
        this.config = Objects.requireNonNull(config);
    }

    public Position getPosition() {
        return position;
    }

    public SearchConfig getConfig() {
        return config;
    }

    private void reset() {
        pvTable.clear();
        for (final var inner : killers)
            Arrays.fill(inner, null);
        nodesSearched = 0;
    }

    public void make(final String move) {
        history.add(Make.run(position, move, Movegen.pseudoLegal(position)));
    }

    public boolean isRepetition() {
        final var key = Zobrist.hash(position);
        for (var i = history.size() - 2;
             i >= history.size() - position.getHalfmoveClock();
             i -= 2
        )
            if (key == history.get(i).getKey()) return true;
        return false;
    }

    private int qsearch(int alpha, final int beta) {
        if ((nodesSearched & 2047) == 0) {
            if (System.currentTimeMillis() > config.getStopTime()) config.stop();
        }

        nodesSearched++;
        if (isRepetition() || position.getHalfmoveClock() >= 100) return Evaluator.DRAW_VALUE;
        var score = Evaluator.run(position);
        if (score >= beta) return beta;
        if (score > alpha) alpha = score;
        if (history.size() >= config.getMaxDepth()) return score;

        final var moveList = Movegen.pseudoLegal(position)
            .stream()
            .filter(x -> x.isEnpassantCapture() || position.getPieceAt(x.getTo()) != null)
            .collect(Collectors.toList());

        var pvFound = false;
        var legalCount = 0;
        Move bestMove = null;
        for (final var move : new OrderedMoveList(position, moveList)) {
            final var snapshot = Make.run(position, move);
            if (Movegen.opponentKingIsAttacked(position)) {
                Unmake.run(position, snapshot);
                continue;
            }
            legalCount++;
            history.add(snapshot);
            score = -qsearch(-beta, -alpha);
            history.remove(history.size() - 1);
            Unmake.run(position, snapshot);

            if (config.isStopped()) return 0;

            if (score >= beta) return beta;
            if (score > alpha) {
                alpha = score;
                bestMove = move;
                pvFound = true;
            }
        }

        if (pvFound) pvTable.put(Zobrist.hash(position), bestMove);

        return alpha;
    }

    private int alphaBeta(final int depth, final int targetDepth, int alpha, final int beta) {
        nodesSearched++;

        if (depth >= targetDepth) return qsearch(alpha, beta);
        if ((nodesSearched & 2047) == 0) {
            if (System.currentTimeMillis() > config.getStopTime()) config.stop();
        }

        if (isRepetition() || position.getHalfmoveClock() >= 100) return Evaluator.DRAW_VALUE;

        final var key = Zobrist.hash(position);
        final var moveList = OrderedMoveList.pseudoLegal(position);

        final var pvMoveIndex = moveList.indexOf(pvTable.get(key));
        if (pvMoveIndex != -1) {
            moveList.setScore(pvMoveIndex, 1000);
        }

        for (var i = 0; i < 2; i++) {
            final var index = moveList.indexOf(killers[depth][i]);
            if (index != -1) moveList.setScore(index, 100 + 2 - i);
        }

        var pvFound = false;
        var legalCount = 0;
        Move bestMove = null;
        for (final var move : moveList) {
            final var snapshot = Make.run(position, move);
            if (Movegen.opponentKingIsAttacked(position)) {
                Unmake.run(position, snapshot);
                continue;
            }
            legalCount++;
            history.add(snapshot);
            final var score = -alphaBeta(depth + 1, targetDepth, -beta, -alpha);
            history.remove(history.size() - 1);
            Unmake.run(position, snapshot);

            if (config.isStopped()) return 0;

            if (score >= beta) {
                if (!move.isEnpassantCapture() && position.getPieceAt(move.getTo()) == null) {
                    killers[depth][1] = killers[depth][0];
                    killers[depth][0] = move;
                }
                return beta;
            }
            if (score > alpha) {
                alpha = score;
                bestMove = move;
                pvFound = true;
            }
        }

        if (legalCount == 0) {
            return (Movegen.currentKingIsAttacked(position))
                ? Evaluator.MATE_VALUE + depth
                : Evaluator.DRAW_VALUE;
        }

        if (pvFound) pvTable.put(key, bestMove);

        return alpha;
    }

    public Move search() {
        reset();
        config.init();

        var bestScore = 0;
        Move bestMove = null;
        for (var i = 1; i <= config.getMaxDepth(); i++) {
            bestScore = alphaBeta(0, i, Evaluator.WORST_VALUE, Evaluator.BEST_VALUE);

            if (config.isStopped()) break;

            final var pvLine = pvTable.getLine(position, i);
            bestMove = pvLine.get(0);
            System.out.println(
                String.format(
                    "info score cp %d depth %d nodes %d time %d pv %s",
                    bestScore, i, nodesSearched, System.currentTimeMillis() - config.getStartTime(), pvLine
                )
            );
        }
        System.out.println("bestmove " + bestMove);
        return bestMove;
    }
}
