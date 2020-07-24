package chess.move;

import chess.basictypes.*;

public class Unmake {
    private static void common(
            final Position position,
            final Square from,
            final Square to,
            final Piece moved,
            final Piece captured
    ) {
        final var fromBB = Bitboard.fromSquare(from);
        final var toBB = Bitboard.fromSquare(to);
        final var motionBB = fromBB | toBB;

        position.xorBB(moved, motionBB);
        position.setPieceAt(from, moved);
        position.setPieceAt(to, captured);

        if (captured != null)
            position.xorBB(captured, toBB);
    }

    public static void run(final Position position, final Position.Snapshot snapshot) {
        final var opponentColor = position.getCurrentColor();
        final var currentColor = opponentColor.invert();

        position.setCurrentColor(currentColor);
        position.setCastlingRights(snapshot.getCastlingRights());
        position.setEpSquare(snapshot.getEpSquare());
        position.setHalfmoveClock(snapshot.getHalfmoveClock());
        if (currentColor == Color.BLACK)
            position.setFullmoveCounter(position.getFullmoveCounter() - 1);

        final var move = snapshot.getMove();
        final var from = move.getFrom();
        final var to = move.getTo();
        final var captured = snapshot.getCapturedPiece();
        final Piece moved;

        if (move.getPromotionType() == null) {
            moved = position.getPieceAt(to);
        } else {
            moved = Piece.of(currentColor, Piece.Type.PAWN);
            final var promotion = Piece.of(currentColor, move.getPromotionType());
            final var toBB = Bitboard.fromSquare(to);
            position.xorBB(promotion, toBB);
            position.xorBB(moved, toBB);
            position.setPieceAt(to, moved);
        }

        common(position, from, to, moved, captured);
        if (moved.type() == Piece.Type.KING && move.isCastling()) {
            final var rook = Piece.of(currentColor, Piece.Type.ROOK);
            final var castlingDef = CastlingDefinition.of(currentColor, move.getCastlingDirection());
            final var rookFrom = castlingDef.getRookFrom();
            final var rookTo = castlingDef.getRookTo();
            final var rookMotionBB
                    = Bitboard.fromSquare(rookFrom)
                    | Bitboard.fromSquare(rookTo);
            position.xorBB(rook, rookMotionBB);
            position.setPieceAt(rookFrom, rook);
            position.setPieceAt(rookTo, null);
        } else if (moved.type() == Piece.Type.PAWN && move.isEnpassantCapture()) {
            final var actualCaptured = Piece.of(opponentColor, Piece.Type.PAWN);
            final var capturedSq = Square.at(to.file(), from.rank());
            final var capturedBB = Bitboard.fromSquare(capturedSq);
            position.xorBB(actualCaptured, capturedBB);
            position.setPieceAt(capturedSq, actualCaptured);
        }

        assert position.validate() == null : position.validate();
    }
}
