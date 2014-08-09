package at.yawk.brainfuckloader.brainfuck;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
class DebugInstruction implements Instruction {
    private final String name;
}
