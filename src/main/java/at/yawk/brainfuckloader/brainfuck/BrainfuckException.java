package at.yawk.brainfuckloader.brainfuck;

/**
 * @author yawkat
 */
public class BrainfuckException extends Error {
    public BrainfuckException() {
    }

    public BrainfuckException(String message) {
        super(message);
    }

    public BrainfuckException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrainfuckException(Throwable cause) {
        super(cause);
    }
}
