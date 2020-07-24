package chess.move;

import chess.basictypes.Position;
import org.junit.jupiter.api.Test;

import java.text.NumberFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerftTest {
    private static final long MAX_DEPTH = 5;

    private static void testSet(final String initialFen, final long[] expectedNodesByDepth) {
        final var intFormatter = NumberFormat.getIntegerInstance();
        final var perftRunner = new Perft(new Position().applyFen(initialFen));

        System.out.println("[depth] actual / expected");
        System.out.println("-------------------------");

        for (var i = 0; i <= Math.min(MAX_DEPTH, expectedNodesByDepth.length - 1); i++) {
            final var expected = expectedNodesByDepth[i];
            final var actual = perftRunner.perft(i);
            System.out.println(
                "[" + i + "] "
                    + intFormatter.format(actual) + " / "
                    + intFormatter.format(expected)
            );
            assertEquals(expected, actual);
        }
    }


    //region https://www.chessprogramming.org/Perft_Results

    @Test
    public void startpos() {
        testSet(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            new long[] { 1L, 20L, 400L, 8902L, 197281L, 4865609L, 119060324L, 3195901860L }
        );
    }

    @Test
    public void kiwipete() {
        testSet(
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
            new long[] { 1L, 48L, 2039L, 97862L, 4085603L, 193690690L, 8031647685L }
        );
    }

    @Test
    public void position3() {
        testSet(
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            new long[] { 1L, 14L, 191L, 2812L, 43238L, 674624L, 11030083L, 178633661L, 3009794393L }
        );
    }

    @Test
    public void position4() {
        testSet(
            "r2q1rk1/pP1p2pp/Q4n2/bbp1p3/Np6/1B3NBn/pPPP1PPP/R3K2R b KQ - 0 1",
            new long[] { 1L, 6L, 264L, 9467L, 422333L, 15833292L, 706045033L }
        );
    }

    @Test
    public void position5() {
        testSet(
            "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
            new long[] { 1L, 44L, 1486L, 62379L, 2103487L, 89941194L }
        );
    }

    @Test
    public void byStevenEdwards() {
        testSet(
            "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10",
            new long[] { 1L, 46L, 2079L, 89890L, 3894594L, 164075551L, 6923051137L, 287188994746L, 11923589843526L, 490154852788714L }
        );
    }

    //endregion
}