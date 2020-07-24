package chess.move;

import chess.basictypes.*;

import java.util.List;

public class Make {
    private static int common(
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
        position.setPieceAt(from, null);
        position.setPieceAt(to, moved);

        if (captured == null)
            return moved.type() == Piece.Type.PAWN ? 0 : position.getHalfmoveClock() + 1;

        final var otherColor = position.getCurrentColor().invert();
        position.xorBB(captured, toBB);
        if (captured.type() == Piece.Type.ROOK)
            for (final var dir : CastlingDirection.valueList())
                if (to == CastlingDefinition.of(otherColor, dir).getRookFrom())
                    position.setCastlingRights(
                        position.getCastlingRights().disallow(otherColor, dir)
                    );
        return 0;
    }

    public static Position.Snapshot run(final Position position, final String move, final List<Move> moveList) {
        final Square from = Square.valueOf(move.substring(0, 2).toUpperCase());
        final Square to = Square.valueOf(move.substring(2, 4).toUpperCase());
        final Piece.Type promotion;
        if (move.length() <= 4) {
            promotion = null;
        } else {
            switch (Character.toLowerCase(move.charAt(4))) {
                case 'n':
                    promotion = Piece.Type.KNIGHT;
                    break;
                case 'b':
                    promotion = Piece.Type.BISHOP;
                    break;
                case 'r':
                    promotion = Piece.Type.ROOK;
                    break;
                case 'q':
                    promotion = Piece.Type.QUEEN;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid move string");
            }
        }

        for (final var m : moveList) {
            if (from == m.getFrom()
                && to == m.getTo()
                && promotion == m.getPromotionType()
            ) {
                final var snapshot = Make.run(position, m);
                if (!Movegen.opponentKingIsAttacked(position))
                    return snapshot;
                Unmake.run(position, snapshot);
                throw new IllegalArgumentException("Illegal move");

            }
        }

        throw new IllegalArgumentException("Unavailable move");
    }

    public static Position.Snapshot run(final Position position, final Move move) {
        final var currentColor = position.getCurrentColor();
        final var from = move.getFrom();
        final var to = move.getTo();
        final var moved = position.getPieceAt(from);
        final var captured = position.getPieceAt(to);

        final var snapshot = new Position.Snapshot(
            move,
            captured,
            Zobrist.hash(position),
            position.getCastlingRights(),
            position.getEpSquare(),
            position.getHalfmoveClock()
        );

        position.setEpSquare(null);
        position.setHalfmoveClock(common(position, from, to, moved, captured));
        switch (moved.type()) {
            case ROOK:
                for (final var dir : CastlingDirection.valueList()) {
                    if (from == CastlingDefinition.of(currentColor, dir).getRookFrom()) {
                        position.setCastlingRights(
                            position.getCastlingRights().disallow(currentColor, dir)
                        );
                        break;
                    }
                }
                break;
            case KING:
                position.setCastlingRights(
                    position.getCastlingRights().disallow(currentColor)
                );
                if (move.isCastling()) {
                    final var castlingDef = CastlingDefinition.of(currentColor, move.getCastlingDirection());
                    final var rookFrom = castlingDef.getRookFrom();
                    final var rookTo = castlingDef.getRookTo();
                    final var rookMotionBB
                        = Bitboard.fromSquare(rookFrom)
                        | Bitboard.fromSquare(rookTo);
                    final var rook = Piece.of(currentColor, Piece.Type.ROOK);
                    position.xorBB(rook, rookMotionBB);
                    position.setPieceAt(rookFrom, null);
                    position.setPieceAt(rookTo, rook);
                }
                break;
            case PAWN:
                if (move.isEnpassantCapture()) {
                    final var actualCaptured = Piece.of(currentColor.invert(), Piece.Type.PAWN);
                    final var capturedSq = Square.at(to.file(), from.rank());
                    final var capturedBB = Bitboard.fromSquare(capturedSq);
                    position.xorBB(actualCaptured, capturedBB);
                    position.setPieceAt(capturedSq, null);
                } else if (move.getPromotionType() != null) {
                    final var promotionPiece = Piece.of(currentColor, move.getPromotionType());
                    final var toBB = Bitboard.fromSquare(to);
                    position.xorBB(moved, toBB);
                    position.xorBB(promotionPiece, toBB);
                    position.setPieceAt(to, promotionPiece);
                } else if (Math.abs(from.rank() - to.rank()) == 2) {
                    position.setEpSquare(
                        Square.at(
                            from.file(),
                            (from.rank() + to.rank()) / 2
                        )
                    );
                }
                break;
        }

        position.setCurrentColor(currentColor.invert());
        if (currentColor == Color.BLACK)
            position.setFullmoveCounter(position.getFullmoveCounter() + 1);

        assert position.validate() == null : position.validate();

        return snapshot;
    }
}
