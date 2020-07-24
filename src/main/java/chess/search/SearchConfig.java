package chess.search;

public class SearchConfig {
    private int maxDepth;
    private long searchTime;
    private long startTime;
    private long stopTime;
    private boolean stopped;

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = (maxDepth <= 0 || maxDepth > Search.MAX_DEPTH) ? Search.MAX_DEPTH : maxDepth;
    }

    public long getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(long searchTime) {
        final var inf = 24L * 60 * 60 * 1000;
        this.searchTime = (searchTime <= 0 || searchTime > inf) ? inf : searchTime;
        this.searchTime = Math.max(1, this.searchTime - 50);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stop() {
        stopped = true;
    }

    public void init() {
        startTime = System.currentTimeMillis();
        stopTime = System.currentTimeMillis() + searchTime;
        stopped = false;
    }
}
