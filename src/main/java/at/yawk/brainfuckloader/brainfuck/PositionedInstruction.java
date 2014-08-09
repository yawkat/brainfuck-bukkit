package at.yawk.brainfuckloader.brainfuck;

import lombok.Value;

/**
 * @author yawkat
 */
@Value
class PositionedInstruction {
    private final Instruction instruction;
    private final int line;
    private final int charInLine;
}
