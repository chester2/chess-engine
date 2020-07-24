package chess.move;

import chess.basictypes.*;
import chess.search.OrderedMoveList;

import java.util.*;
import java.util.stream.StreamSupport;

public class Movegen {
    public static boolean isAttacked(final Position position, final Square square, final Color attacker) {
        final var victim = attacker.invert();
        final var occupiedBB = position.getBB();
        return 0 != (Motions.pawnAttacks(victim, square) & position.getBB(attacker, Piece.Type.PAWN))
            || 0 != (Motions.knightAttacks(square) & position.getBB(attacker, Piece.Type.KNIGHT))
            || 0 != (Motions.bishopAttacks(square, occupiedBB) & position.getBB(attacker, Piece.Type.BISHOP))
            || 0 != (Motions.rookAttacks(square, occupiedBB) & position.getBB(attacker, Piece.Type.ROOK))
            || 0 != (Motions.queenAttacks(square, occupiedBB) & position.getBB(attacker, Piece.Type.QUEEN))
            || 0 != (Motions.kingAttacks(square) & position.getBB(attacker, Piece.Type.KING));
    }

    public static boolean currentKingIsAttacked(final Position position) {
        final var currentColor = position.getCurrentColor();
        final var kingSq = position.getKingSquare(currentColor);
        return isAttacked(position, kingSq, currentColor.invert());
    }

    public static boolean opponentKingIsAttacked(final Position position) {
        final var currentColor = position.getCurrentColor();
        final var kingSq = position.getKingSquare(currentColor.invert());
        return isAttacked(position, kingSq, currentColor);
    }

    public static List<Move> legal(final Position position) {
        final var moveList = pseudoLegal(position);
        for (var i = moveList.size() - 1; i >= 0; i--) {
            final var snapshot = Make.run(position, moveList.get(i));
            if (opponentKingIsAttacked(position))
                moveList.remove(i);
            Unmake.run(position, snapshot);
        }
        return moveList;
    }

    public static List<Move> pseudoLegal(final Position position) {
        final var moveList = new ArrayList<Move>();
        generatePawnMoves(position, moveList);
        generateKnightMoves(position, moveList);
        generateBishopMoves(position, moveList);
        generateRookMoves(position, moveList);
        generateQueenMoves(position, moveList);
        generateKingMoves(position, moveList);
        return moveList;
    }

    private static void generatePawnMoves(final Position position, final List<Move> moveList) {
        final var moveBuilder = new Move.Builder();

        final var currentColor = position.getCurrentColor();
        final var epSquare = position.getEpSquare();
        final var opponentBB = position.getBB(currentColor.invert());
        final var emptyBB = ~position.getBB();
        final var pawnBB = position.getBB(currentColor, Piece.Type.PAWN);
        final var promotionRank = Promotion.rank(currentColor);

        for (final var from : Bitboard.iterSquares(pawnBB)) {
            moveBuilder.setFrom(from);

            final var attacks = Motions.pawnAttacks(currentColor, from);
            final var singlePush = Motions.pawnSinglePush(currentColor, from);
            final var doublePush = Motions.pawnDoublePush(currentColor, from);

            if (epSquare != null) {
                final var epBB = Bitboard.fromSquare(epSquare);
                if (0 != (attacks & epBB))
                    moveList.add(
                        moveBuilder
                            .setTo(epSquare)
                            .markEnpassantCapture().build()
                    );
            }

            var toBB = singlePush & emptyBB;
            if (toBB != 0L)
                toBB |= doublePush & emptyBB;
            toBB |= attacks & opponentBB;

            for (final var to : Bitboard.iterSquares(toBB)) {
                moveBuilder.setTo(to);
                if (from.rank() == promotionRank) {
                    for (final var type : Promotion.promotableTypes())
                        moveList.add(moveBuilder.setPromotionType(type).build());
                } else {
                    moveList.add(moveBuilder.clearSpecial().build());
                }
            }
        }
    }

    private static void generateKingMoves(final Position position, final List<Move> moveList) {
        final var currentColor = position.getCurrentColor();
        final var kingSq = position.getKingSquare(currentColor);
        final var moveBuilder = new Move.Builder().setFrom(kingSq);

        final var toBB = Motions.kingAttacks(kingSq) & ~position.getBB(currentColor);
        for (final var to : Bitboard.iterSquares(toBB))
            moveList.add(moveBuilder.setTo(to).build());

        for (final var dir : CastlingDirection.valueList()) {
            final var castlingDefinition = position.getCastlingRights().getDefinition(currentColor, dir);
            if (castlingDefinition == null) continue;
            if (0 != (position.getBB() & castlingDefinition.getCannotBeOccupiedBB())) continue;
            if (StreamSupport
                .stream(Bitboard.iterSquares(castlingDefinition.getCannotBeAttackedBB()).spliterator(), false)
                .anyMatch(sq -> isAttacked(position, sq, currentColor.invert()))
            ) continue;
            moveList.add(
                moveBuilder
                    .setTo(castlingDefinition.getKingTo())
                    .markCastling()
                    .build()
            );
        }
    }

    private static void generateKnightMoves(final Position position, final List<Move> moveList) {
        final var moveBuilder = new Move.Builder();
        final var currentColor = position.getCurrentColor();
        final var knightBB = position.getBB(currentColor, Piece.Type.KNIGHT);
        final var notCurrentBB = ~position.getBB(currentColor);

        for (final var from : Bitboard.iterSquares(knightBB)) {
            moveBuilder.setFrom(from);
            final var toBB = Motions.knightAttacks(from) & notCurrentBB;
            for (final var to : Bitboard.iterSquares(toBB))
                moveList.add(moveBuilder.setTo(to).build());
        }
    }

    private static void generateBishopMoves(final Position position, final List<Move> moveList) {
        final var moveBuilder = new Move.Builder();
        final var currentColor = position.getCurrentColor();
        final var occupiedBB = position.getBB();
        final var bishopBB = position.getBB(currentColor, Piece.Type.BISHOP);
        final var notCurrentBB = ~position.getBB(currentColor);

        for (final var from : Bitboard.iterSquares(bishopBB)) {
            moveBuilder.setFrom(from);
            final var toBB = Motions.bishopAttacks(from, occupiedBB) & notCurrentBB;
            for (final var to : Bitboard.iterSquares(toBB))
                moveList.add(moveBuilder.setTo(to).build());
        }
    }

    private static void generateRookMoves(final Position position, final List<Move> moveList) {
        final var moveBuilder = new Move.Builder();
        final var currentColor = position.getCurrentColor();
        final var occupiedBB = position.getBB();
        final var rookBB = position.getBB(currentColor, Piece.Type.ROOK);
        final var notCurrentBB = ~position.getBB(currentColor);

        for (final var from : Bitboard.iterSquares(rookBB)) {
            moveBuilder.setFrom(from);
            final var toBB = Motions.rookAttacks(from, occupiedBB) & notCurrentBB;
            for (final var to : Bitboard.iterSquares(toBB))
                moveList.add(moveBuilder.setTo(to).build());
        }
    }

    private static void generateQueenMoves(final Position position, final List<Move> moveList) {
        final var moveBuilder = new Move.Builder();
        final var currentColor = position.getCurrentColor();
        final var occupiedBB = position.getBB();
        final var queenBB = position.getBB(currentColor, Piece.Type.QUEEN);
        final var notCurrentBB = ~position.getBB(currentColor);

        for (final var from : Bitboard.iterSquares(queenBB)) {
            moveBuilder.setFrom(from);
            final var toBB = Motions.queenAttacks(from, occupiedBB) & notCurrentBB;
            for (final var to : Bitboard.iterSquares(toBB))
                moveList.add(moveBuilder.setTo(to).build());
        }
    }
}
