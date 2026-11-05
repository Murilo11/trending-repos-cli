package dev.murilo.trending;

public class TreandingException extends RuntimeException {
    private final int exitCode;
    public TreandingException(String message) {
        super(message);
        this.exitCode = 1;


    }

    public TreandingException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
