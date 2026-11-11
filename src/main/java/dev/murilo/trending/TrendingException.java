package dev.murilo.trending;

public class TrendingException extends RuntimeException {
    private final int exitCode;
    public TrendingException(String message) {
        super(message);
        this.exitCode = 1;


    }

    public TrendingException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
