package at.yawk.brainfuckloader.brainfuck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author yawkat
 */
public class BrainfuckFile {
    @Getter(AccessLevel.PACKAGE) private final List<PositionedInstruction> instructions;

    BrainfuckFile(List<PositionedInstruction> instructions) {
        this.instructions = Collections.unmodifiableList(new ArrayList<>(instructions));
    }
}
