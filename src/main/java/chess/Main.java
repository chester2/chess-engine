package chess;

import chess.uci.UCI;

public class Main {
    public static void main(String[] args) {
        var uci = new UCI();
        uci.loop();
    }
}
