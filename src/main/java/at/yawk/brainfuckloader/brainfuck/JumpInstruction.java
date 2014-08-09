package at.yawk.brainfuckloader.brainfuck;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class JumpInstruction implements Instruction {
    private final int offset;
}
