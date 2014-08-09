package at.yawk.brainfuckloader;

import java.util.function.Function;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
class Lambda {
    private final Function<Object[], Object> function;
    private final int argumentCount;
}
