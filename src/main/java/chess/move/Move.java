package chess.move;

import chess.basictypes.CastlingDirection;
import chess.basictypes.Piece;
import chess.basictypes.Position;
import chess.basictypes.Square;

import java.util.Objects;

public class Move {
    private final Square from;
    private final Square to;
    private final Piece.Type promotionType;
    private final boolean isEnpassantCapture;
    private final boolean isCastling;

    public Move(
        final Square from,
        final Square to,
        final Piece.Type promotionType,
        final boolean isEnpassantCapture,
        final boolean isCastling
    ) {
        this.from = from;
        this.to = to;
        this.promotionType = promotionType;
        this.isEnpassantCapture = isEnpassantCapture;
        this.isCastling = isCastling;
    }

    public Square getFrom() {
        return from;
    }

    public Square getTo() {
        return to;
    }

    public Piece.Type getPromotionType() {
        return promotionType;
    }

    public boolean isEnpassantCapture() {
        return isEnpassantCapture;
    }

    public boolean isCastling() {
        return isCastling;
    }

    public CastlingDirection getCastlingDirection() {
        if (!isCastling) throw new IllegalStateException("Move is not a castling move");
        return from.ordinal() < to.ordinal() ? CastlingDirection.OO : CastlingDirection.OOO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return isEnpassantCapture == move.isEnpassantCapture &&
            isCastling == move.isCastling &&
            from == move.from &&
            to == move.to &&
            promotionType == move.promotionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, promotionType, isEnpassantCapture, isCastling);
    }

    @Override
    public String toString() {
        var out = from.name().toLowerCase() + to.name().toLowerCase();
        if (promotionType == null) return out;
        switch (promotionType) {
            case KNIGHT: out += 'n'; break;
            case BISHOP: out += 'b'; break;
            case ROOK: out += 'r'; break;
            case QUEEN: out += 'q'; break;
        }
        return out;
    }

    public static class Builder {
        private Square from;
        private Square to;
        private Piece.Type promotionType;
        private boolean isEnpassantCapture;
        private boolean isCastling;

        public Move build() {
            if (from == null) throw new IllegalStateException("Field 'from' is null");
            if (to == null) throw new IllegalStateException("Field 'to' is null");
            if (promotionType != null) {
                if (isEnpassantCapture || isCastling)
                    throw new IllegalStateException("Move cannot be promotion and en passant capture / castling at the same time");
            } else if (isEnpassantCapture && isCastling) {
                throw new IllegalStateException("Move cannot be en passant capture and castling at the same time");
            }
            return new Move(from, to, promotionType, isEnpassantCapture, isCastling);
        }

        public Builder setFrom(Square from) { this.from = Objects.requireNonNull(from); return this; }

        public Builder setTo(Square to) { this.to = Objects.requireNonNull(to); return this; }

        public Builder clearSpecial() {
            promotionType = null;
            isEnpassantCapture = false;
            isCastling = false;
            return this;
        }

        public Builder setPromotionType(final Piece.Type promotionType) {
            clearSpecial();
            if (!Promotion.promotableTypes().contains(promotionType))
                throw new IllegalArgumentException("Provided piece type not a valid promotion target");
            this.promotionType = promotionType;
            return this;
        }

        public Builder markEnpassantCapture() {
            clearSpecial();
            this.isEnpassantCapture = true;
            return this;
        }

        public Builder markCastling() {
            clearSpecial();
            this.isCastling = true;
            return this;
        }
    }
}
