package at.yawk.brainfuckloader.brainfuck;

/**
 * @author yawkat
 */
public interface IOHandler {
    void out(byte value);

    byte in();
}
