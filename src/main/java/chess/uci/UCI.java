package chess.uci;

import chess.basictypes.Color;
import chess.basictypes.Position;
import chess.search.Search;
import chess.search.SearchConfig;

import java.util.Arrays;
import java.util.Scanner;

public class UCI {
    private final Position position = new Position().applyFen(Position.STARTPOS_FEN);
    private final SearchConfig config = new SearchConfig();
    private final Search search = new Search(position, config);
    private final Scanner stdin = new Scanner(System.in);

    private void cmdPosition(final String[] args) {
        var i = 2;
        var j = 2;
        if (args[1].equals("startpos")) {
            position.applyFen(Position.STARTPOS_FEN);
        } else {
            for (; j < args.length && !args[j].equals("moves"); j++) {}
            final var fen = String.join(" ", Arrays.copyOfRange(args, i, j));
            position.applyFen(fen);
        }

        if (j < args.length) {
            for (var k = j + 1; k < args.length; k++) {
                search.make(args[k]);
            }
        }
    }

    private void cmdGo(final String[] args) {
        final var argList = Arrays.asList(args);

        var depth = 0;
        var movesToGo = 30;
        var movetime = -1L;
        var time = 0L;
        var inc = 0L;

        if (!argList.contains("infinite")) {
            int i;
            if ((i = argList.indexOf("winc")) != -1 && position.getCurrentColor() == Color.WHITE
                || (i = argList.indexOf("binc")) != -1 && position.getCurrentColor() == Color.BLACK
            )
                inc = Long.valueOf(argList.get(i + 1));

            if ((i = argList.indexOf("wtime")) != -1 && position.getCurrentColor() == Color.WHITE
                || (i = argList.indexOf("btime")) != -1 && position.getCurrentColor() == Color.BLACK
            )
                time = Long.valueOf(argList.get(i + 1));

            if ((i = argList.indexOf("movestogo")) != -1)
                movesToGo = Integer.valueOf(argList.get(i + 1));

            if ((i = argList.indexOf("movetime")) != -1)
                movetime = Long.valueOf(argList.get(i + 1));

            if ((i = argList.indexOf("depth")) != -1)
                depth = Integer.valueOf(argList.get(i + 1));
        }

        if (movetime != -1L) {
            time = movetime;
            movesToGo = 1;
        }

        config.setMaxDepth(depth);
        config.setSearchTime(time / movesToGo + inc);
        search.search();
    }

    public void loop() {
        while (true) {
            final var args = stdin.nextLine().strip().split("\\s+");
            if (args.length == 0) continue;
            switch (args[0]) {
                case "quit":
                    return;
                case "stop":
                    config.stop();
                    break;
                case "uci":
                    System.out.println("id name chess");
                    System.out.println("id author Chester Wu");
                    System.out.println("uciok");
                    break;
                case "isready": System.out.println("readyok"); break;
                case "ucinewgame": cmdPosition(new String[] { "position", "startpos" }); break;
                case "position": cmdPosition(args); break;
                case "go": cmdGo(args); break;
                case "d": position.print(); break;
                default: System.out.println("Unknown command"); break;
            }
        }
    }
}
