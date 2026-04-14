package sa.examples.matrix.model;

public class MatrixResult {

    private int size;
    private double trace;
    private long elapsedMs;

    public MatrixResult(int size, double trace, long elapsedMs) {
        this.size = size;
        this.trace = trace;
        this.elapsedMs = elapsedMs;
    }

    public int getSize() {
        return size;
    }

    public double getTrace() {
        return trace;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }
}
